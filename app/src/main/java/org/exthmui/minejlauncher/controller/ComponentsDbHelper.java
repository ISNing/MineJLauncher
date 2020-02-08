package org.exthmui.minejlauncher.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.exthmui.minejlauncher.model.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComponentsDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "components.db";

    public static class ComponentEntry implements BaseColumns {
        public static final String TABLE_NAME = "components";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_DISPLAYVERSION = "version";
        public static final String COLUMN_NAME_VERSION = "version";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_INTRODUCTION = "introduction";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ComponentEntry.TABLE_NAME + " (" +
                    ComponentEntry._ID + " INTEGER PRIMARY KEY," +
                    ComponentEntry.COLUMN_NAME_STATUS + " INTEGER," +
                    ComponentEntry.COLUMN_NAME_PATH + " TEXT," +
                    ComponentEntry.COLUMN_NAME_ID + " TEXT NOT NULL UNIQUE," +
                    ComponentEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    ComponentEntry.COLUMN_NAME_TYPE + " INTEGER," +
                    ComponentEntry.COLUMN_NAME_DISPLAYVERSION + " TEXT," +
                    ComponentEntry.COLUMN_NAME_VERSION + " INTEGER," +
                    ComponentEntry.COLUMN_NAME_SIZE + " INTEGER," +
                    ComponentEntry.COLUMN_NAME_INTRODUCTION + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ComponentEntry.TABLE_NAME;

    public ComponentsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long addComponent(Component component) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ComponentEntry.COLUMN_NAME_STATUS, component.getPersistentStatus());
        values.put(ComponentEntry.COLUMN_NAME_PATH, component.getPath());
        values.put(ComponentEntry.COLUMN_NAME_ID, component.getId());
        //values.put(ComponentEntry.COLUMN_NAME_TIMESTAMP, component.getTimestamp());
        values.put(ComponentEntry.COLUMN_NAME_TYPE, component.getPackType());
        values.put(ComponentEntry.COLUMN_NAME_DISPLAYVERSION, component.getDisplayVersion());
        values.put(ComponentEntry.COLUMN_NAME_VERSION, component.getVersion());
        values.put(ComponentEntry.COLUMN_NAME_SIZE, component.getFileSize());
        values.put(ComponentEntry.COLUMN_NAME_INTRODUCTION, component.getIntroduction());
        return db.insert(ComponentEntry.TABLE_NAME, null, values);
    }

    public long addComponentWithOnConflict(Component component, int conflictAlgorithm) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ComponentEntry.COLUMN_NAME_STATUS, component.getPersistentStatus());
        values.put(ComponentEntry.COLUMN_NAME_PATH, component.getPath());
        values.put(ComponentEntry.COLUMN_NAME_ID, component.getId());
        //values.put(ComponentEntry.COLUMN_NAME_TIMESTAMP, component.getTimestamp());
        values.put(ComponentEntry.COLUMN_NAME_TYPE, component.getPackType());
        values.put(ComponentEntry.COLUMN_NAME_DISPLAYVERSION, component.getDisplayVersion());
        values.put(ComponentEntry.COLUMN_NAME_VERSION, component.getVersion());
        values.put(ComponentEntry.COLUMN_NAME_SIZE, component.getFileSize());
        values.put(ComponentEntry.COLUMN_NAME_INTRODUCTION, component.getIntroduction());
        return db.insertWithOnConflict(ComponentEntry.TABLE_NAME, null, values, conflictAlgorithm);
    }

    public boolean removeComponent(String downloadId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = ComponentEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {downloadId};
        return db.delete(ComponentEntry.TABLE_NAME, selection, selectionArgs) != 0;
    }

    public boolean removeComponent(long rowId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = ComponentEntry._ID + " = " + rowId;
        return db.delete(ComponentEntry.TABLE_NAME, selection, null) != 0;
    }

    public boolean changeComponentStatus(Component component) {
        String selection = ComponentEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {component.getId()};
        return changeComponentStatus(selection, selectionArgs, component.getPersistentStatus());
    }

    public boolean changeComponentStatus(long rowId, int status) {
        String selection = ComponentEntry._ID + " = " + rowId;
        return changeComponentStatus(selection, null, status);
    }

    private boolean changeComponentStatus(String selection, String[] selectionArgs,
                                       int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ComponentEntry.COLUMN_NAME_STATUS, status);
        return db.update(ComponentEntry.TABLE_NAME, values, selection, selectionArgs) != 0;
    }

    public Component getComponent(long rowId) {
        String selection = ComponentEntry._ID + " = " + rowId;
        return getComponent(selection, null);
    }

    public Component getComponent(String downloadId) {
        String selection = ComponentEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {downloadId};
        return getComponent(selection, selectionArgs);
    }

    private Component getComponent(String selection, String[] selectionArgs) {
        List<Component> components = getComponents(selection, selectionArgs);
        return components != null ? components.get(0) : null;
    }

    public List<Component> getComponents() {
        return getComponents(null, null);
    }

    public List<Component> getComponents(String selection, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ComponentEntry.COLUMN_NAME_PATH,
                ComponentEntry.COLUMN_NAME_ID,
                //ComponentEntry.COLUMN_NAME_TIMESTAMP,
                ComponentEntry.COLUMN_NAME_TYPE,
                ComponentEntry.COLUMN_NAME_DISPLAYVERSION,
                ComponentEntry.COLUMN_NAME_VERSION,
                ComponentEntry.COLUMN_NAME_STATUS,
                ComponentEntry.COLUMN_NAME_SIZE,
                ComponentEntry.COLUMN_NAME_INTRODUCTION,
        };
        String sort = ComponentEntry.COLUMN_NAME_TYPE + " DESC";
        Cursor cursor = db.query(ComponentEntry.TABLE_NAME, projection, selection, selectionArgs,
                null, null, sort);
        List<Component> components = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Component component = new Component();
                int index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_PATH);
                component.setFile(new File(cursor.getString(index)));
                component.setName(component.getFile().getName());
                index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_ID);
                component.setId(cursor.getString(index));
                //index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_TIMESTAMP);
                //component.setTimestamp(cursor.getString(index));
                index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_TYPE);
                component.setPackType(cursor.getInt(index));
                index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_DISPLAYVERSION);
                component.setDisplayVersion(cursor.getString(index));
                index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_VERSION);
                component.setVersion(cursor.getLong(index));
                index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_STATUS);
                component.setPersistentStatus(cursor.getInt(index));
                index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_SIZE);
                component.setFileSize(cursor.getLong(index));
                index = cursor.getColumnIndex(ComponentEntry.COLUMN_NAME_INTRODUCTION);
                component.setIntroduction(cursor.getString(index));
                components.add(component);
            }
            cursor.close();
        }
        return components;
    }
}
