package com.example.zeiterfassung.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.zeiterfassung.R;
import com.example.zeiterfassung.TimeTrackingApp;
import com.example.zeiterfassung.db.WorkTime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExportService extends IntentService {
    private final static String _EXPORT_CHANNEL = "Export";
    private final static int _NOTIFICATION_ID = 500;

    public ExportService() {
        super("CSVExporter");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
     //Datei für den Export auslesen
        Uri exportFile = intent.getData();

        //kein Export ohne Datei
        if (exportFile == null){
            return;
        }
        //Grupper erzeugen, falls notwendig
        createChannel();

        // Benachrichtigung erzeugen
        NotificationCompat.Builder baseNotification = createBaseNotification();

        // Benachrichtigung anzeigen
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(_NOTIFICATION_ID, baseNotification.build());

        TimeTrackingApp app = (TimeTrackingApp) getApplication();
        app.getExecutors().diskIO().execute(() -> {
            // Laden der Daten aus der Datenbank
            List<WorkTime> allData = app.getDb().workTimeDato().getAll();

            // Anzahl der Datensetze setzen
            int itemsCount = allData.size();
            notificationManager.notify(_NOTIFICATION_ID,
                    baseNotification
                            .setProgress(itemsCount, 0, false).build());

            DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY);

            try {
                try (OutputStream os = getContentResolver().openOutputStream(exportFile);
                     OutputStreamWriter osw = new OutputStreamWriter(os);
                     BufferedWriter writer = new BufferedWriter(osw)) {
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
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Schreiben der Zeile in die Datei
                    writer.newLine();
                    writer.append(line);

                    // Aktuellen Stand melden
                    notificationManager.notify(_NOTIFICATION_ID,
                            baseNotification
                                    .setProgress(itemsCount, counter, false).build());
                    }
                 }
            } catch (IOException e) {
                // Fehler beim Zugriff auf IO
                e.printStackTrace();
            } finally {
                // Beendigung des Exportes melden
                notificationManager.notify(_NOTIFICATION_ID,
                        baseNotification
                                .setProgress(0, 0, false)
                                .setContentText(getString(R.string.ExportNotificationFinished))
                                .build()
                );
            }
        });
    }

    private void createChannel() {
        // Versionsweiche
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+, Channel erforderlich
            // Name
            CharSequence name = getString(R.string.ChannelName);
            // Beschreibung
            String description = getString(R.string.ChannelDescription);
            // Wichtigkeit
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(_EXPORT_CHANNEL, name, importance);
            channel.setDescription(description);
            // Channel registrieren
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder createBaseNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, _EXPORT_CHANNEL)
                .setSmallIcon(R.drawable.ic_export)
                .setContentTitle(getString(R.string.ExportNotificationTitle))
                .setContentText(getString(R.string.ExportNotificationMessage))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        return builder;
    }
}

