package com.example.zeiterfassung.viewmodels;


import android.os.Bundle;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.zeiterfassung.BR;
import com.example.zeiterfassung.db.WorkTime;
import com.example.zeiterfassung.db.WorkTimeDao;

import java.util.Calendar;

public class EditViewModel extends BaseObservable {
    public static final String ID_KEY = "WorkTimeId";
    private static final String _START_DATE_TIME = "Key_StartDateTime";
    private static final String _END_DATE_TIME = "Key_EndDateTime";
    private static final String _PAUSE = "Key_Pause";
    private static final String _COMMENT = "Key_Comment";

    private String _comment;
    private int _pause;
    private Calendar _startTime;
    private Calendar _endTime;
    private WorkTimeDao _dao;
    private int _id = 0;

    public EditViewModel(WorkTimeDao dao, int id) {
        _dao = dao;
        _id = id;
    }

    @Bindable
    public String getComment() {
        return _comment;
    }

    public void setComment(String comment) {
        // Prüfung auf Änderungen
        if (_comment == null && comment == null) {
            return;
        }
        if (_comment != null && _comment.equals(comment)) {
            return;
        }

        // Änderung übernehmen
        _comment = comment;
        notifyPropertyChanged(BR.comment);
    }

    @Bindable
    public int getPause() {
        return _pause;
    }

    public void setPause(int pause) {
        if (_pause == pause) {
            return;
        }

        // Änderung übernehmen
        _pause = pause;
        notifyPropertyChanged(BR.pause);
    }

    @Bindable
    public Calendar getStartTime() {
        return _startTime;
    }

    public void setStartTime(Calendar startTime) {
        _startTime = startTime;
        notifyPropertyChanged(BR.startTime);
    }

    @Bindable
    public Calendar getEndTime() {
        return _endTime;
    }

    public void setEndTime(Calendar endTime) {
        _endTime = endTime;
        notifyPropertyChanged(BR.endTime);
    }

    public void loadFromDb() {
        WorkTime workTime = _dao.getById(_id);
        if (workTime == null) {
            return;
        }

        setStartTime(workTime.startTime);
        setEndTime(workTime.endTime);
        setPause(workTime.getPause());
        setComment(workTime.comment);
    }

    public void save() {
        WorkTime workTime = new WorkTime();
        workTime.id = _id;
        workTime.startTime = getStartTime();
        workTime.endTime = getEndTime();
        workTime.setPause(getPause());
        workTime.comment = getComment();

        if (workTime.id > 0) {
            _dao.update(workTime);
        } else {
            _dao.add(workTime);
        }
    }

    public void loadFromState(Bundle state) {
        long startMillis = state.getLong(_START_DATE_TIME, 0L);
        if (startMillis > 0) {
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(startMillis);
            setStartTime(startTime);
        }

        long endMillis = state.getLong(_END_DATE_TIME, 0L);
        if (endMillis > 0) {
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(endMillis);
            setEndTime(endTime);
        }
        int pause = state.getInt(_PAUSE, 0);
        setPause(pause);
        setComment(state.getString(_COMMENT, ""));
    }

    public void saveInBundle(Bundle state) {
        state.putLong(_START_DATE_TIME, getStartTime().getTimeInMillis());
        if (getEndTime() != null) {
            state.putLong(_END_DATE_TIME, getEndTime().getTimeInMillis());
        }
        state.putInt(_PAUSE, getPause());
        String comment = getComment();
        if (comment != null && !comment.isEmpty()) {
            state.putString(_COMMENT, comment);
        }
    }

    public boolean onShazaLongClick(View view, boolean hallo) {
        return false;
    }
}
