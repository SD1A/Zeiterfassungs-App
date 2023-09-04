package com.example.zeiterfassung.db;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface WorkTimeDao {

    @Query("SELECT * FROM  time_data ORDER BY start_time DESC")
    List<WorkTime> getAll();


    @Query("SELECT * FROM time_data WHERE _id = :id")
    WorkTime getById(int id);


    @Query("SELECT * FROM time_data " +
            "WHERE IFNULL(end_time, '') = '' " +
            "ORDER BY _id DESC")
    WorkTime getOpened();



    @Update
    void update(WorkTime workTime);

    @Insert
    void add(WorkTime workTime);

    @Delete
    void delete(WorkTime workTime);


}
