package com.example.zeiterfassung.dialogs;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class ChangeTimeDialog extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener {
    private IChangeDateTime _ChangeListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (!(getActivity() instanceof IChangeDateTime)) {
            throw new UnsupportedOperationException("Activity muss 'IchangeDateTime' interface implemetieren");

        }
        _ChangeListener =(IChangeDateTime) getActivity();
        IChangeDateTime.DateType type= IChangeDateTime.DateType.valueOf(getTag());
        Calendar date = _ChangeListener.getDate(type);
        return new TimePickerDialog(getContext(),
                this, //Callback fürs Setzen
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                // Android Einstellung, ob 24h Format genutzt wird(boolean)
                android.text.format.DateFormat.is24HourFormat(getContext()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            IChangeDateTime.DateType type = IChangeDateTime.DateType.valueOf(getTag());
            _ChangeListener.updateTime(type, hourOfDay, minute);

    }


}

