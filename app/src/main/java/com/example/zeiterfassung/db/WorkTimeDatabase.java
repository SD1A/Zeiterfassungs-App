package com.example.zeiterfassung.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {WorkTime.class, CachedIssue.class}, version = 4, exportSchema = false)
@TypeConverters({CalendarConverter.class})

public abstract class WorkTimeDatabase extends RoomDatabase {
    public abstract WorkTimeDao workTimeDato();

    public abstract CachedIssueDao cachedIssueDao();

    private static WorkTimeDatabase _instance;

    public static WorkTimeDatabase getInstance(final Context context) {
        // Check condition
        if (_instance == null) {
            synchronized (WorkTimeDatabase.class) {
                //When database is null
                //Initialize database
                if (_instance == null) {
                    _instance = Room.databaseBuilder(
                            context.getApplicationContext(),/* Context */
                            WorkTimeDatabase.class, //Datenbank
                            "Zeiterfassung.com.example.zeiterfassung.db"//Dateiname
                    ).addMigrations(_MIGRATION_1_2, _MIGRATION_2_3, _MIGRATION_3_4)//Migrationen
                     .build();
                }
            }
        }

        return _instance;

    }

    private final static Migration _MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE time_data ADD COLUMN pause INTEGER NOT NULL DEFAULT 0");
        }
    };

    private final static Migration _MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE time_data ADD COLUMN comment TEXT");
        }
    };

    private final static Migration _MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE \"issues\" (\n" +
                    "\t`_id`\tINTEGER NOT NULL,\n" +
                    "\t`title`\tTEXT,\n" +
                    "\t`prio`\tTEXT,\n" +
                    "\t`state`\tTEXT,\n" +
                    "\tPRIMARY KEY(`_id` AUTOINCREMENT)\n" +
                    ")");
        }
    };
}
