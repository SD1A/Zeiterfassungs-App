package com.example.zeiterfassung;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zeiterfassung.dialogs.ChangeDateDialog;
import com.example.zeiterfassung.dialogs.ChangeTimeDialog;
import com.example.zeiterfassung.dialogs.IChangeDateTime;

import java.text.DateFormat;
import java.util.Calendar;

import com.example.zeiterfassung.db.WorkTime;

public class EditDataActivity extends AppCompatActivity implements IChangeDateTime {
    public static final String ID_KEY = "WorkTimeId";
    private static final String _START_DATE_TIME = "Key_StartDateTime";
    private static final String _END_DATE_TIME = "Key_EndDateTime";
    private static final String _PAUSE = "Key_Pause";
    private static final String _COMMENT = "Key_Comment";
    private int _workTimeId = -1;
    private boolean _isRestored = false;
    private WorkTime _workTime = new WorkTime();
    private EditText _startDateValue;
    private EditText _startTimeValue;
    private EditText _endDateValue;
    private EditText _endTimeValue;
    private EditText _pauseValue;
    private EditText _commentValue;
    private DateFormat _dateFormatter;
    private DateFormat _timeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dataa);

        // UI Elemente auslesen
        _startDateValue = findViewById(R.id.StartDateValue);
        _startTimeValue = findViewById(R.id.StartTimeValue);
        _endDateValue = findViewById(R.id.EndDateValue);
        _endTimeValue = findViewById(R.id.EndTimeValue);
        _pauseValue = findViewById(R.id.PauseValue);
        _commentValue = findViewById(R.id.CommentValue);

        // Initializierung Datum / Uhrzeit Formatierung
        _dateFormatter = android.text.format.DateFormat.getDateFormat(this);
        _timeFormatter = android.text.format.DateFormat.getTimeFormat(this);

        // Auslesen der übergebenen ID
        _workTimeId = getIntent().getIntExtra(
                ID_KEY, // Key
                -1); // Standardwert

        // Prüfung für Wiederherstellung
        _isRestored = savedInstanceState != null
                && savedInstanceState.containsKey(_START_DATE_TIME);

        // Deaktivieren der Tastatureingaben
        _startDateValue.setKeyListener(null);
        _startTimeValue.setKeyListener(null);
        _endDateValue.setKeyListener(null);
        _endTimeValue.setKeyListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Keine Daten laden, wenn diese Wiederhergestellt wurden
        if (_isRestored) {
            return;
        }

        getApp().getExecutors().diskIO().execute(() -> {
            _workTime = getApp().getDb().workTimeDato().getById(_workTimeId);
            updateUi();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        _startDateValue.setOnClickListener(v -> openDateDialog(DateType.START, true));
        _startDateValue.setOnFocusChangeListener((v, hasFocus) -> openDateDialog(DateType.START, hasFocus));

        _startTimeValue.setOnClickListener(v -> openTimeDialog(DateType.START, true));
        _startTimeValue.setOnFocusChangeListener((v, hasFocus) -> openTimeDialog(DateType.START, hasFocus));

        _endDateValue.setOnClickListener(v -> openDateDialog(DateType.END, true));
        _endDateValue.setOnFocusChangeListener((v, hasFocus) -> openDateDialog(DateType.END, hasFocus));

        _endTimeValue.setOnClickListener(v -> openTimeDialog(DateType.END, true));
        _endTimeValue.setOnFocusChangeListener((v, hasFocus) -> openTimeDialog(DateType.END, hasFocus));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _startDateValue.setOnClickListener(null);
        _startDateValue.setOnFocusChangeListener(null);

        _startTimeValue.setOnClickListener(null);
        _startTimeValue.setOnFocusChangeListener(null);

        _endDateValue.setOnClickListener(null);
        _endDateValue.setOnFocusChangeListener(null);

        _endTimeValue.setOnClickListener(null);
        _endTimeValue.setOnFocusChangeListener(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(_START_DATE_TIME, _workTime.startTime.getTimeInMillis());
        if (_workTime.endTime != null) {
            outState.putLong(_END_DATE_TIME, _workTime.endTime.getTimeInMillis());
        }
        int pause = readPauseAsInt();
        outState.putInt(_PAUSE, pause);
        String comment = _commentValue.getText().toString();
        if(!comment.isEmpty()){
            outState.putString(_COMMENT, comment);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long startMillis = savedInstanceState.getLong(_START_DATE_TIME, 0L);
        if (startMillis > 0) {
            _workTime.id = _workTimeId;
            _workTime.startTime.setTimeInMillis(startMillis);
        }

        long endMillis = savedInstanceState.getLong(_END_DATE_TIME, 0L);
        if (endMillis > 0) {
            _workTime.endTime = Calendar.getInstance();
            _workTime.endTime.setTimeInMillis(endMillis);
        }
        int pause = savedInstanceState.getInt(_PAUSE, 0);
        _workTime.setPause(pause);
        _workTime.comment = savedInstanceState.getString(_COMMENT, "");
        updateUi();
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

    private void saveWorkTime() {
        _workTime.comment = _commentValue.getText().toString();
        _workTime.setPause(readPauseAsInt());
        getApp().getExecutors().diskIO().execute(() -> {
            getApp().getDb().workTimeDato().update(_workTime);
        });
    }

    private int readPauseAsInt(){
        String pauseString = _pauseValue.getText().toString();
        if(pauseString.isEmpty()){
            return 0;
        }
        try {
            return Integer.parseInt(pauseString);
        } catch (NumberFormatException e){
            // Fehler bei Konvertierung
            Log.e("EditDataActivity", "Fehler bei Konvertierung", e);
            return 0;
        }
    }

    private void updateUi() {
        getApp().getExecutors().mainThread().execute(() -> {
            _startDateValue.setText(_dateFormatter.format(_workTime.startTime.getTime()));
            _startTimeValue.setText(_timeFormatter.format(_workTime.startTime.getTime()));
            if (_workTime.endTime == null) {
                _endDateValue.setText("");
                _endTimeValue.setText("");
            } else {
                _endDateValue.setText(_dateFormatter.format(_workTime.endTime.getTime()));
                _endTimeValue.setText(_timeFormatter.format(_workTime.endTime.getTime()));
            }
            _pauseValue.setText(String.valueOf(_workTime.getPause()));
            if (_workTime.comment != null && !_workTime.comment.isEmpty()) {
                _commentValue.setText(_workTime.comment);
            } else {
                _commentValue.setText("");
            }
        });
    }

    private TimeTrackingApp getApp() {
        return (TimeTrackingApp) getApplication();
    }

    private void openDateDialog(DateType type, boolean isFocused) {
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

    @Override
    public Calendar getDate(DateType dateType) {
        if (dateType == DateType.START) {
            return _workTime.startTime;
        }
        return _workTime.endTime == null
                ? Calendar.getInstance()
                : _workTime.endTime;
    }

    @Override
    public Calendar getTime(DateType dateType) {
        if (dateType == DateType.START) {
            return _workTime.startTime;
        }
        return _workTime.endTime == null
                ? Calendar.getInstance()
                : _workTime.endTime;
    }

    @Override
    public void updateDate(DateType dateType, int year, int month, int day) {
        if (dateType == DateType.START) {
            _workTime.startTime.set(year, month, day);
        } else {

            if (_workTime.endTime == null) {
                _workTime.endTime = Calendar.getInstance();
            }

            _workTime.endTime.set(year, month, day);
        }
        updateUi();
    }

    @Override
    public void updateTime(DateType dateType, int hours, int minutes) {
        if (dateType == DateType.START) {
            _workTime.startTime.set(Calendar.HOUR_OF_DAY, hours);
            _workTime.startTime.set(Calendar.MINUTE, minutes);
        } else {
            if (_workTime.endTime == null) {
                _workTime.endTime = Calendar.getInstance();
            }

            _workTime.endTime.set(Calendar.HOUR_OF_DAY, hours);
            _workTime.endTime.set(Calendar.MINUTE, minutes);
        }
        updateUi();
    }
}