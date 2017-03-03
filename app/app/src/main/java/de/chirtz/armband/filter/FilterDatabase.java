package de.chirtz.armband.filter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class FilterDatabase {

    public static final String DATABASE_NAME = "watch_db";
    public static final String TABLE_NAME = "filters";
    private final Context context;
    private final FilterDatabaseSQLHelper helper;
    private static final String EXTERNAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/watchconfig/" + DATABASE_NAME+"_backup.sqlite";
    private final static String DECREMENT_QUERY = "UPDATE " + TABLE_NAME + " SET " + Filter.FILTER_POSITION + " = " + Filter.FILTER_POSITION + "-1 WHERE "
            + Filter.FILTER_POSITION + "> ?";
    private final static String POSITION_QUERY = Filter.FILTER_POSITION + " = ?";
    private final static String SELECT_ALL_QUERY = "SELECT * FROM " + TABLE_NAME;
    private final static String FILTER_ORDER = Filter.FILTER_POSITION + " ASC";

    private final static String MOVE_QUERY_UP = "UPDATE " + TABLE_NAME + " SET " + Filter.FILTER_POSITION + " = " + Filter.FILTER_POSITION + "+1 WHERE "
            + Filter.FILTER_POSITION + ">= ?";

    public FilterDatabase(Context context) {
        this.context = context;
        helper = new FilterDatabaseSQLHelper(context);
    }

    public void addEntry(Filter f) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String countQuery = SELECT_ALL_QUERY;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        f.setPosition(count);
        db.insert(TABLE_NAME, "null", f.getSQLValues());
        db.close();
    }

    private void addEntry(Filter f, int position) {
        SQLiteDatabase db = helper.getWritableDatabase();
        f.setPosition(position);
        db.insert(TABLE_NAME, "null", f.getSQLValues());
        db.close();
    }

    public void updateEntry(Filter f) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(TABLE_NAME, f.getSQLValues(), POSITION_QUERY, new String[]{String.valueOf(f.getPosition())});
        db.close();
    }

    public void deleteEntry(int position) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME, POSITION_QUERY, new String[]{String.valueOf(position)});
        //noinspection StatementWithEmptyBody
        while (db.rawQuery(DECREMENT_QUERY, new String[]{String.valueOf(position)}, null).moveToNext()) {}
        db.close();
    }

    public LinkedList<Filter> getEntries() {
        SQLiteDatabase db = new FilterDatabaseSQLHelper(context).getReadableDatabase();
        Cursor dbCursor = db.query(TABLE_NAME, null, null, null, null, null, FILTER_ORDER);
        LinkedList<Filter> filters = new LinkedList<>();
        while (dbCursor.moveToNext()) {
            filters.add(Filter.fromCursor(dbCursor));
        }
        db.close();
        return filters;
    }

    public Filter getEntry(int position) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, POSITION_QUERY, new String[]{String.valueOf(position)}, null, null, null, null);
        c.moveToNext();
        Filter l = Filter.fromCursor(c);
        db.close();
        return l;
    }

    public void changeEntryPosition(int oldPos, int newPos) {
        if (newPos == oldPos)
            newPos = 0;
        Filter f = getEntry(oldPos);
        deleteEntry(oldPos);
        SQLiteDatabase db = helper.getWritableDatabase();
        //noinspection StatementWithEmptyBody
        while (db.rawQuery(MOVE_QUERY_UP, new String[]{String.valueOf(newPos)}, null).moveToNext()) {}
        db.close();
        addEntry(f, newPos);
    }


    private static boolean copy(String src, String dst) {
        try {
            File f = new File(dst);
            f.getParentFile().mkdirs();
            FileInputStream inStream = new FileInputStream(src);
            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
            return true;
        } catch (IOException e) {
            Log.d("X", e.getMessage());
            return false;
        }
    }

    public static boolean backupDBToExternal(Context context) {
        String databasePath = context.getApplicationInfo().dataDir + "/databases/" + DATABASE_NAME;
        return copy(databasePath, EXTERNAL_PATH);
    }

    public static boolean restoreDBFromExternal(Context context) {
        String databasePath = context.getApplicationInfo().dataDir + "/databases/" + DATABASE_NAME;
        return copy(EXTERNAL_PATH, databasePath);
    }


}
