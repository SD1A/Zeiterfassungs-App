package com.example.zeiterfassung.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CachedIssueDao {
    @Query("SELECT * FROM issues ORDER BY _id")
    List<CachedIssue> getAll();

    @Query("DELETE FROM issues")
    void deleteAll();

    @Insert
    void addRange(List<CachedIssue> issues);
}
