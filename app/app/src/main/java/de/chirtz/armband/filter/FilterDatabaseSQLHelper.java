package de.chirtz.armband.filter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.chirtz.armband.filter.filter_properties.FilterProperty;


class FilterDatabaseSQLHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String ID = "_id";
    private static final String TABLE_DROP = "DROP TABLE IF EXISTS " + FilterDatabase.TABLE_NAME;
    private static final String TABLE_CREATE =
            "CREATE TABLE " + FilterDatabase.TABLE_NAME + " (" +
                    ID + " INTEGER, " +
                    Filter.FILTER_POSITION + " INTEGER, " +
                    FilterProperty.FILTER_CONDITION_PACKAGE + " TEXT, " +
                    FilterProperty.FILTER_CONDITION_CONTENT + " TEXT, " +
                    FilterProperty.FILTER_CONDITION_TITLE + " TEXT, " +
                    FilterProperty.FILTER_CONDITION_PEOPLE + " TEXT, " +
                    FilterProperty.FILTER_CONDITION_CONTENT_FILTER_ENABLED + " INTEGER, " +
                    FilterProperty.FILTER_CONDITION_NOTIFICATION_PRIORITY + " INTEGER, " +
                    FilterProperty.FILTER_EFFECT_SHOW_ON_DISPLAY + " INTEGER, " +
                    Filter.FILTER_SYSTEM + " INTEGER, " +
                    Filter.FILTER_ENABLED + " INTEGER, " +
                    FilterProperty.FILTER_EFFECT_ACCUMULATE_TEXT + " INTEGER);";


    FilterDatabaseSQLHelper(Context context) {
        super(context, FilterDatabase.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    private void recreateTable(SQLiteDatabase db) {
        db.execSQL(TABLE_DROP);
        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreateTable(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreateTable(db);
    }
}
