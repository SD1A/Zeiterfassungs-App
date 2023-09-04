package com.example.zeiterfassung.bindingAdapter;

import android.widget.EditText;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;

public class IntegerToStringAdapter {
    @BindingAdapter("android:text")


    public static void setText(EditText view, int value) {
        String stringValue = value == 0 ?"" : Integer.toString(value);
        if (stringValue.equals(view.getText().toString())){
            return;
        }
        view.setText(Integer.toString(value));
    }

    @InverseBindingAdapter(attribute = "android:text")
    public static int getText(EditText view) {
        if (view.getText() == null) {
            return 0;
        }

        String stringValue = view.getText().toString();
        if (stringValue.isEmpty()) {
            return 0;
        }

        int intValue = 0;
        try {
            intValue = Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            // Keine Nummer
            e.printStackTrace();
        }

        return intValue;
    }
}


