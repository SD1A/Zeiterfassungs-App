package com.example.zeiterfassung.bindingAdapter;

import android.widget.EditText;

import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import java.text.DateFormat;
import java.util.Calendar;

public class CalendarToStringAdapter {
    @BindingAdapter("app:date")
    public static void setDate(EditText view, Calendar dateTime){
        String dateString = "";

        if(dateTime != null){
            // Formatierung intialisieren
            DateFormat formatter = android.text.format.DateFormat.getDateFormat(view.getContext());
            dateString = formatter.format(dateTime.getTime());
        }
        view.setText(dateString);
    }

    @BindingAdapter("app:time")
    public static void setTime(EditText view, Calendar dateTime){
        String timeString = "";

        if(dateTime != null){
            // Formatierung initialisieren
            DateFormat formatter = android.text.format.DateFormat.getTimeFormat(view.getContext());
            timeString = formatter.format(dateTime.getTime());
        }

        view.setText(timeString);
    }
}
