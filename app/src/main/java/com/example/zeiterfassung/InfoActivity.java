package com.example.zeiterfassung;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zeiterfassung.adapter.IssueAdapter;
import com.example.zeiterfassung.models.Issue;
import com.example.zeiterfassung.models.Response;
import com.example.zeiterfassung.db.CachedIssue;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class InfoActivity extends AppCompatActivity {

    private RecyclerView _issueList;
    private IssueAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Initialisierung der Elemente
        _issueList = findViewById(R.id.IssueList);
        _adapter = new IssueAdapter(this);
        _issueList.setLayoutManager(new LinearLayoutManager(this));
        _issueList.setAdapter(_adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Laden der Daten
        loadIssues();
    }

    private void loadIssues() {
        // Laden aus der Datenbank
        getApp().getExecutors().diskIO().execute(() ->{
            List<CachedIssue> cachedIssues = getApp().getDb().cachedIssueDao().getAll();
            showList(cachedIssues);
        });

        getApp().getExecutors().networkIO().execute(() -> {
            // Basis-URL
            String baseUrl = "https://api.bitbucket.org/2.0/repositories/webducerbooks/androidbook-changes/issues";
            String stateFilter = "(state=\"new\" OR state=\"on hold\" OR state=\"open\")";
            String componentFilter = "component!=\"395592\"";
            // Sonderzeichen kodieren
            String filter = Uri.encode(stateFilter + " AND " + componentFilter);
            try {
                // Finale URL
                URL filterUrl = new URL(baseUrl + "?q=" + filter);

                // Laden der Daten
                OkHttpClient client = new OkHttpClient();

                // Erstellen des Clients
                HttpsURLConnection connection = (HttpsURLConnection) filterUrl.openConnection();
                Request request = new Request.Builder()
                        .url(filterUrl)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                if(!response.isSuccessful()){
                    return;
                }

                String content = response.body().string();

                Issue[] fullIssues = parseFullWithGson(content);

                // Speichern der Daten in der Datenbank
                getApp().getExecutors().diskIO().execute(() -> {
                    // Konvertierung für Datenbank
                    List<CachedIssue> dbIssues = new ArrayList<>();
                    for (Issue issue: fullIssues) {
                        CachedIssue cachedIssue = new CachedIssue();
                        cachedIssue.id = issue.id;
                        cachedIssue.title = issue.title;
                        cachedIssue.priority = issue.priority;
                        cachedIssue.status = issue.state;
                        dbIssues.add(cachedIssue);
                    }
                    // Löschen der alten Daten
                    getApp().getDb().cachedIssueDao().deleteAll();
                    // Speichern der neuen Daten
                    getApp().getDb().cachedIssueDao().addRange(dbIssues);
                    // Neue Daten anzeigen
                    showList(dbIssues);
                });

                // Anzeige als HTML
            } catch (MalformedURLException e) {
                // String konnte nicht in URL umgewandelt werden
                e.printStackTrace();
            } catch (IOException e) {
                // Kein Internetverbindung
                e.printStackTrace();
            }
        });
    }

    private String[] parseIssueTitlesManuall(String json) {
        // Prüfen des Inhaltes
        if (json == null || json.isEmpty()) {
            return new String[0];
        }

        try {
            // JSON verarbeiten
            JSONObject response = new JSONObject(json);

            // Issues Liste auslesen
            JSONArray issueList = response.getJSONArray("values");
            if (issueList.length() == 0) {
                return new String[0];
            }

            // Titel auslesen
            List<String> issueTitleList = new ArrayList<>();
            for (int index = 0; index < issueList.length(); index++) {
                JSONObject issue = issueList.getJSONObject(index);
                String title = issue.getString("title");
                Log.d("ISSUE-TITLE: ", title);
                issueTitleList.add(title);
            }

            return issueTitleList.toArray(new String[0]);
        } catch (JSONException e) {
            // Fehler beim Lesen
            e.printStackTrace();
        }

        return new String[0];
    }

    private String[] parseWithGson(String json) {
        // Prüfen des Inhaltes
        if (json == null || json.isEmpty()) {
            return new String[0];
        }

        // Initialisierung der Bibliothek
        Gson gson = new Gson();

        // Deserialisierung von JSON
        Response response = gson.fromJson(json, Response.class);

        // Titel auslesen
        List<String> issueTitleList = new ArrayList<>();
        for (Issue issue : response.values) {
            Log.d("ISSUE-TITLE: ", issue.title);
            issueTitleList.add(issue.title);
        }

        return issueTitleList.toArray(new String[0]);
    }

    private Issue[] parseFullWithGson(String json) {
        // Prüfen des Inhaltes
        if (json == null || json.isEmpty()) {
            return new Issue[0];
        }

        // Initialisierung der Bibliothek
        Gson gson = new Gson();

        // Deserialisierung von JSON
        Response response = gson.fromJson(json, Response.class);

        return response.values;
    }

    private String generateHtml(Issue[] issues) {
        StringBuilder html = new StringBuilder();
        // Header der HTML-Datei
        html.append("<!DOCTYPE html>")
                .append("<html lang=\"en\">")
                .append("<head>")
                .append("<meta charset=\"utf-8\">")
                .append("<title>Fehlerliste</title>")
                .append("</head>");

        // Inhaltsbereich
        html.append("<body>");

        // Fehler ausgeben
        for (Issue issue : issues) {
            // Überschrift
            html.append("<h1>")
                    .append("#")
                    .append(issue.id)
                    .append(" - ")
                    .append(issue.title)
                    .append("</h1>");

            // Status
            html.append("<div>")
                    .append("Status: ")
                    .append(issue.state)
                    .append("</div>");

            // Priorität
            html.append("<div>")
                    .append("Priorität: ")
                    .append(issue.priority)
                    .append("</div>");

            // Inhalt
            html.append("<section>")
                    .append(issue.content.html)
                    .append("</section>");
        }

        // HTML finalisieren
        html.append("</body>")
                .append("</html>");

        return html.toString();
    }

    private void showHtml(String htmlContent) {
        getApp().getExecutors().mainThread().execute(() -> {
//      _webContent.loadDataWithBaseURL(
//          null, // Basis URL
//          htmlContent, // HTML
//          "text/html; charset=utf-8", // Mime-Type (Datentyp)
//          "utf-8", // Kodierung
//          null); // Historie
        });
    }

    private void showList(List<CachedIssue> issues) {
        getApp().getExecutors().mainThread().execute(() -> {
            _adapter.swapData(issues);
        });
    }

    private TimeTrackingApp getApp() {
        return (TimeTrackingApp) getApplication();
    }
}