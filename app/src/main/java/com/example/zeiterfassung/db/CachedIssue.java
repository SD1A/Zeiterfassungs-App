package com.example.zeiterfassung.db;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues")
public class CachedIssue {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    public int id;

    @Nullable
    @ColumnInfo(name = "title")
    public String title;

    @Nullable
    @ColumnInfo(name = "prio")
    public String priority;

    @Nullable
    @ColumnInfo(name = "state")
    public String status;
}
