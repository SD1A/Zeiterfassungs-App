package com.example.zeiterfassung;

import android.app.Application;
import com.example.zeiterfassung.db.WorkTimeDatabase;


public class TimeTrackingApp extends Application {
    private Application _executors;

    @Override
    public void onCreate() {

        super.onCreate();
        _executors = new AppExecutors();
    }

    public AppExecutors getExecutors(){
        return (AppExecutors) _executors;
    }
    public WorkTimeDatabase getDb() {
        return WorkTimeDatabase.getInstance(this.getApplicationContext());
    }




}