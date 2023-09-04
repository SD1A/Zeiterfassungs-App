package com.example.zeiterfassung.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class ChangeDateDialog extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {
    private IChangeDateTime _ChangeListener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        if(!(getActivity() instanceof  IChangeDateTime)){
            throw new UnsupportedOperationException("Activity muss 'IchangeDateTime' interface implemetieren");
        }
        _ChangeListener =(IChangeDateTime) getActivity();
        IChangeDateTime.DateType type= IChangeDateTime.DateType.valueOf(getTag());
        Calendar date = _ChangeListener.getDate(type);
        return new DatePickerDialog(getContext(),
        this, //Callback fürs Setzen
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){
    IChangeDateTime.DateType type = IChangeDateTime.DateType.valueOf(getTag());
    _ChangeListener.updateDate(type, year, month, dayOfMonth);
    }

}
