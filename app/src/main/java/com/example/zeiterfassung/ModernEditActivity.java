package com.example.zeiterfassung;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.zeiterfassung.databinding.ActivityBindingEditDataBinding;
import com.example.zeiterfassung.dialogs.ChangeDateDialog;
import com.example.zeiterfassung.dialogs.ChangeTimeDialog;
import com.example.zeiterfassung.dialogs.IChangeDateTime;
import com.example.zeiterfassung.viewmodels.EditViewModel;

import java.util.Calendar;


public class ModernEditActivity extends AppCompatActivity implements IChangeDateTime {
    public static final String ID_KEY = "WorkTimeId";
    private static final String _START_DATE_TIME = "Key_StartDateTime";
    private EditViewModel _vm = null;
    private boolean _isRestored = false;
    ActivityBindingEditDataBinding _binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisierung von Binding
        _binding =
                DataBindingUtil.setContentView(this,
                        R.layout.activity_binding_edit_data);

        // Auslesen der übergebenen ID
        int workTimeId = getIntent().getIntExtra(
                ID_KEY, // Key
                -1); // Standardwert

        // View Model initialisieren
        _vm = new EditViewModel(getApp().getDb().workTimeDato(), workTimeId);
        _binding.setVm(_vm);

        // Prüfung für Wiederherstellung
        _isRestored = savedInstanceState != null
                && savedInstanceState.containsKey(_START_DATE_TIME);

        // Deaktivieren der Tastatureingaben
        _binding.StartDateValue.setKeyListener(null);
        _binding.StartTimeValue.setKeyListener(null);
        _binding.EndDateValue.setKeyListener(null);
        _binding.EndTimeValue.setKeyListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Keine Daten laden, wenn diese Wiederhergestellt wurden
        if (_isRestored) {
            return;
        }

        getApp().getExecutors().diskIO().execute(() -> {
            _vm.loadFromDb();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        _binding.StartDateValue.setOnClickListener(v -> openDateDialog(IChangeDateTime.DateType.START, true));
        _binding.StartDateValue.setOnFocusChangeListener((v, hasFocus) -> openDateDialog(IChangeDateTime.DateType.START, hasFocus));

        _binding.StartTimeValue.setOnClickListener(v -> openTimeDialog(IChangeDateTime.DateType.START, true));
        _binding.StartTimeValue.setOnFocusChangeListener((v, hasFocus) -> openTimeDialog(IChangeDateTime.DateType.START, hasFocus));

        _binding.EndDateValue.setOnClickListener(v -> openDateDialog(IChangeDateTime.DateType.END, true));
        _binding.EndDateValue.setOnFocusChangeListener((v, hasFocus) -> openDateDialog(IChangeDateTime.DateType.END, hasFocus));

        _binding.EndTimeValue.setOnClickListener(v -> openTimeDialog(IChangeDateTime.DateType.END, true));
        _binding.EndTimeValue.setOnFocusChangeListener((v, hasFocus) -> openTimeDialog(IChangeDateTime.DateType.END, hasFocus));
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
        _binding.StartDateValue.setOnClickListener(null);
        _binding.StartDateValue.setOnFocusChangeListener(null);
*/
        _binding.StartTimeValue.setOnClickListener(null);
        _binding.StartTimeValue.setOnFocusChangeListener(null);

        _binding.EndDateValue.setOnClickListener(null);
        _binding.EndDateValue.setOnFocusChangeListener(null);

        _binding.EndTimeValue.setOnClickListener(null);
        _binding.EndTimeValue.setOnFocusChangeListener(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        _vm.saveInBundle(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        _vm.loadFromState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveWorkTime();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveWorkTime();
                // Kein return oder break
                // damit Android den Zurück-Button verarbeiten kann
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Calendar getDate(DateType dateType) {
        if (dateType == DateType.START) {
            return _vm.getStartTime();
        }

        return _vm.getEndTime() == null
                ? Calendar.getInstance()
                : _vm.getEndTime();
    }

    @Override
    public Calendar getTime(DateType dateType) {
        if (dateType == DateType.START) {
            return _vm.getStartTime();
        }

        return _vm.getEndTime() == null
                ? Calendar.getInstance()
                : _vm.getEndTime();
    }

    @Override
    public void updateDate(DateType dateType, int year, int month, int day) {
        if (dateType == DateType.START) {
            Calendar startTime = _vm.getStartTime();
            startTime.set(year, month, day);
            _vm.setStartTime(startTime);
        } else {
            Calendar endTime = _vm.getEndTime();
            if (endTime == null) {
                endTime = Calendar.getInstance();
            }

            endTime.set(year, month, day);
            _vm.setEndTime(endTime);
        }
    }

    @Override
    public void updateTime(DateType dateType, int hours, int minutes) {
        if (dateType == DateType.START) {
            Calendar startTime = _vm.getStartTime();
            startTime.set(Calendar.HOUR_OF_DAY, hours);
            startTime.set(Calendar.MINUTE, minutes);
            _vm.setStartTime(startTime);
        } else {
            Calendar endTime = _vm.getEndTime();
            if (endTime == null) {
                endTime = Calendar.getInstance();
            }

            endTime.set(Calendar.HOUR_OF_DAY, hours);
            endTime.set(Calendar.MINUTE, minutes);
            _vm.setEndTime(endTime);
        }
    }

    private void saveWorkTime() {
        getApp().getExecutors().diskIO().execute(() -> {
            _vm.save();
        });
    }

    public void openDateDialog(DateType type, boolean isFocused) {
        if (!isFocused) {
            return;
        }
        ChangeDateDialog dialog = new ChangeDateDialog();
        dialog.show(getSupportFragmentManager(), type.toString());
    }

    private void openTimeDialog(DateType type, boolean isFocused) {
        if (!isFocused) {
            return;
        }
        ChangeTimeDialog dialog = new ChangeTimeDialog();
        dialog.show(getSupportFragmentManager(), type.toString());
    }

    private TimeTrackingApp getApp() {
        return (TimeTrackingApp) getApplication();
    }
}
