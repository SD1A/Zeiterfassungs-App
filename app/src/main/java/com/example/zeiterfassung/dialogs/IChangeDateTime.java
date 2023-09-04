package com.example.zeiterfassung.dialogs;

import java.util.Calendar;

public interface IChangeDateTime {
    enum DateType{
        START,
        END
    }
    Calendar getDate (DateType dateType);
    Calendar getTime (DateType dateType);
    void updateDate(DateType dateType, int year, int month, int day);

    void updateTime(DateType dateType, int hours, int minutes);
}
