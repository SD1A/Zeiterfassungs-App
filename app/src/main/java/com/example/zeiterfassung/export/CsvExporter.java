package com.example.zeiterfassung.export;
import android.os.Environment;
import android.view.View;
import com.example.zeiterfassung.ListDataActivity;
import com.example.zeiterfassung.TimeTrackingApp;
import android.widget.ProgressBar;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.io.IOException;

import com.example.zeiterfassung.db.WorkTime;


public class CsvExporter {
    private final ListDataActivity _activity;
    private ProgressBar _exportProgress;

    public CsvExporter(ListDataActivity activity, ProgressBar exportProgress) {
        _activity = activity;
        _exportProgress = exportProgress;
    }

    public void executeExport() {
        TimeTrackingApp app = (TimeTrackingApp) _activity.getApplication();
        app.getExecutors().diskIO().execute(() -> {
            // Fortschritt sichtbar machen
            app.getExecutors().mainThread().execute(() -> _exportProgress.setVisibility(View.VISIBLE));

            // Laden der Daten aus der Datenbank
            List<WorkTime> allData = app.getDb().workTimeDato().getAll();

            // Anzahl der Elemente setzen
            final int itemsCount = allData.size();
            app.getExecutors().mainThread().execute(() -> _exportProgress.setMax(itemsCount));

            DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY);

            // Export-Ordner (Dokument-Ordner des Benutzers)
            File docsDirectory = _activity.getFilesDir();
            File exportFile = new File(docsDirectory, "WorkTimesExport.csv");

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(exportFile));

                // Speichern der Spalten als erste Zeile
                StringBuilder line = new StringBuilder();
                line.append("id;start_time;end_time;pause;comment");
                writer.append(line);

                int counter = 0;
                // Speichern der Werte
                for (WorkTime workItem : allData) {
                    counter++;
                    // Zeile leeren
                    line.delete(0, line.length());
                    // Neue Zeile
                    line.append(workItem.id)
                            .append(";")
                            .append(dateTimeFormatter.format(workItem.startTime.getTime()))
                            .append(";");
                    if (workItem.endTime == null) {
                        line.append(";");
                    } else {
                        line.append(dateTimeFormatter.format(workItem.endTime.getTime()))
                                .append(";");
                    }
                    line.append(workItem.getPause())
                            .append(";");
                    if (workItem.comment != null && !workItem.comment.isEmpty()) {
                        line.append('"')
                                .append(workItem.comment.replace("\"", "\"\""))
                                .append('"');
                    }

                    // Export verlangsamen
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Schreiben der Zeile in die Datei
                    writer.newLine();
                    writer.append(line);

                    // Aktuellen Datensatz ausgeben
                    final int currentCounter = counter;
                    app.getExecutors().mainThread().execute(() -> _exportProgress.setProgress(currentCounter));
                }
            } catch (IOException e) {
                // Fehler beim Zugriff auf IO
                e.printStackTrace();
            } finally {
                if (writer == null) {
                    return;
                }

                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // Fehler beim Schließen des Writers
                    e.printStackTrace();
                }

                // Fortschritt ausblenden
                app.getExecutors().mainThread().execute(() -> _exportProgress.setVisibility(View.GONE));
            }
        });
    }
}
