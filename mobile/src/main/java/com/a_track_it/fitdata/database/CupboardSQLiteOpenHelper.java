package com.a_track_it.fitdata.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class CupboardSQLiteOpenHelper extends MultiThreadSQLiteOpenHelper {
    private static final String DATABASE_NAME = "a_track_it.db";
    private static final int DATABASE_VERSION = 1;

    static {
        // register our models
        cupboard().register(com.a_track_it.fitdata.common.model.WorkoutSet.class);
        cupboard().register(com.a_track_it.fitdata.common.model.Workout.class);
        cupboard().register(com.a_track_it.fitdata.common.model.Bodypart.class);
        cupboard().register(com.a_track_it.fitdata.common.model.Exercise.class);
    }

    public CupboardSQLiteOpenHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        cupboard().withDatabase(db).createTables();
        // add indexes and other database tweaks
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work
    }
}