package com.example.zeiterfassung;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zeiterfassung.db.WorkTime;
import com.example.zeiterfassung.adapter.WorkTimeDataAdapter;
import com.example.zeiterfassung.services.ExportService;

import java.util.List;


public class ListDataActivity extends AppCompatActivity {
    private WorkTimeDataAdapter _workTimeAdapter;
    // ID für SAF Dateiabfrage
    private final static int _SAF_CREATE_EXPORT_FILE = 200;
    private ProgressBar _exportProgress;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        // Liste suchen
        RecyclerView list = findViewById(R.id.DataList);
        list.setLayoutManager(new LinearLayoutManager(this));

        // Adapter
        _workTimeAdapter = new WorkTimeDataAdapter(this);
        list.setAdapter(_workTimeAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getApp().getExecutors().diskIO().execute(() -> {
            List<WorkTime> data = getApp().getDb().workTimeDato().getAll();
            getApp().getExecutors().mainThread().execute(() -> {
                _workTimeAdapter.swapData(data);
            });
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_data_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.MenuItemExport:
              selectFileForExport();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == _SAF_CREATE_EXPORT_FILE) {
            // Dateipfad auslesen
            Uri filePath = data.getData();
            exportAsCsv(filePath);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void selectFileForExport() {
        //Aktion, eine neue Datei anzulegen
        Intent fileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        //Kategorie, um diese Datei dann auch öffnen zu können
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //Dateityp(Mine Type)setzen, als Filer
        fileIntent.setType("text/csv");
        //Vorschlag für den Namen der Datei
        fileIntent.putExtra(Intent.EXTRA_TITLE, "export_saf.csv");
        // Starten des Intents
        startActivityForResult(fileIntent, _SAF_CREATE_EXPORT_FILE);
    }

    private void exportAsCsv(Uri filePath) {
        // Export über Intent-Service
        Intent exportService = new Intent(this, ExportService.class);
        //Dateipfad an den Service übergaben
        exportService.setData(filePath);
        startService(exportService);
    }

    private TimeTrackingApp getApp() {
        return (TimeTrackingApp) getApplication();
    }
}
