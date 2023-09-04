package com.example.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.util.Calendar;

import com.example.zeiterfassung.db.CalendarConverter;
import com.example.zeiterfassung.db.WorkTime;

public class MainActivity extends AppCompatActivity {
    //Klassenvariablen
    private EditText _startDateTime;
    private EditText _endDateTime;
    private Button _startCommand;
    private Button _endCommand;
    private DateFormat _dateFormatter;
    private DateFormat _timeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // "Suchen" der UI Elemente
        _startDateTime = findViewById(R.id.StartDateTime);
        _endDateTime = findViewById(R.id.EndDateTime);
        _startCommand = findViewById(R.id.StartCommand);
        _endCommand = findViewById(R.id.EndCommand);

        // Deaktivieren der Tastatureingaben
        _startDateTime.setKeyListener(null);
        _endDateTime.setKeyListener(null);

        // Initializierung Datum / Uhrzeit Formatierung
        _dateFormatter = android.text.format.DateFormat.getDateFormat(this);
        _timeFormatter = android.text.format.DateFormat.getTimeFormat(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initFromDb();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Listener registrieren
        _startCommand.setOnClickListener(onStartClicked());
        _endCommand.setOnClickListener(onEndClicked());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Listener deregistrieren
        _startCommand.setOnClickListener(null);
        _endCommand.setOnClickListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.MenuItemListData:
                // Impliziter Intent
                // Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://google.de"));
                // startActivity(googleIntent);
                // Expliziter Intent
                Intent listDataIntent = new Intent(
                        this, // Context für Klassenkontext
                        ListDataActivity.class); // Activity-Klasse
                startActivity(listDataIntent);
                return true;

            case R.id.MenuItemInfo:
                Intent infoIntent = new Intent(this, InfoActivity.class);
                startActivity(infoIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private TimeTrackingApp getApp() {
        return (TimeTrackingApp) getApplication();
    }

    private void setStartTimeForUI(String startTime) {
        getApp().getExecutors().mainThread().execute(() -> {
            _startCommand.setEnabled(false);
            _startDateTime.setText(startTime);
            _endCommand.setEnabled(true);
        });
    }

    private void setEndTimeForUI(String endTime) {
        getApp().getExecutors().mainThread().execute(() -> {
            _endCommand.setEnabled(false);
            _endDateTime.setText(endTime);
            _startCommand.setEnabled(true);
        });
    }

    private void resetStartEnd() {
        getApp().getExecutors().mainThread().execute(() -> {
            _startDateTime.setText("");
            _endDateTime.setText("");
            _startCommand.setEnabled(true);
        });
    }

    private void initFromDb() {
        // Deaktivieren der beiden Buttons
        _startCommand.setEnabled(false);
        _endCommand.setEnabled(false);

        // Laden eines offenen Datensatzes
        getApp().getExecutors().diskIO().execute(() -> {
            WorkTime openWorkTile = getApp().getDb().workTimeDato().getOpened();
            if (openWorkTile == null) {
                // Kein offener Datensatz
                resetStartEnd();
            } else {
                // Offener Datensatz
                setStartTimeForUI(formatForUI(openWorkTile.startTime));
            }
        });
    }

    private View.OnClickListener onStartClicked() {
        return v -> {
            _startCommand.setEnabled(false);

            // In Datenbank speichern
            getApp().getExecutors().diskIO().execute(() -> {
                WorkTime workTime = new WorkTime();
                getApp().getDb().workTimeDato().add(workTime);

                setStartTimeForUI(formatForUI(workTime.startTime));
            });
        };
    }

    private View.OnClickListener onEndClicked() {
        return v -> {
            _endCommand.setEnabled(false);

            getApp().getExecutors().diskIO().execute(() -> {
                WorkTime startedWorkTime = getApp().getDb().workTimeDato().getOpened();
                if (startedWorkTime == null) {
                    // Kein Datensatz mit offenem Ende gefunden
                    resetStartEnd();
                } else {
                    Calendar currentTime = Calendar.getInstance();
                    startedWorkTime.endTime = currentTime;
                    getApp().getDb().workTimeDato().update(startedWorkTime);

                    setEndTimeForUI(formatForUI(currentTime));
                }
            });
        };
    }

    private String formatForUI(Calendar currentTime) {
        return String.format(
                "%s %s", // String für Formatierung
                _dateFormatter.format(currentTime.getTime()), // Datum formatiert
                _timeFormatter.format(currentTime.getTime()) // Zeit formatiert
        );
    }
}