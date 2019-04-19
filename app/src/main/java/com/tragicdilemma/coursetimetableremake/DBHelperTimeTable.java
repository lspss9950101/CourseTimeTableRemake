package com.tragicdilemma.coursetimetableremake;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBHelperTimeTable extends SQLiteOpenHelper {

    public static final String DB_NAME = "CourseTimeTable.db";
    public static final String DB_TABLE = "Course";
    public static final String KEY_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ROOM = "room";
    public static final String COLUMN_COLOR = "color";
    public static final int DB_VERSION = 9;
    public static final int COURSE_COUNT = 13*7;
    private static DBHelperTimeTable instance;

    public static final String DB_TABLE_PREF = "Pref";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ATTR = "attr";
    public static final ArrayList<String> PREF_TITLE = new ArrayList<String>(Arrays.asList(new String[]{         "contextSize", "contextSizeRoom", "darkMode", "widgetTitleSize", "widgetTitleBgColor",                                                "widgetTitleTextColor", "widgetContextSize", "widgetContextSizeRoom", "widgetContextBgColor",                                              "widgetContextTextColor"}));
    public static final ArrayList<String> PREF_DEFAULT_ATTR = new ArrayList<String>(Arrays.asList(new String[]{"0",           "0",               "true",     "0",                String.valueOf(Color.HSVToColor(new float[]{232, 0.698f, 0.624f})), "false",                "0",                  "0",                     String.valueOf(Color.HSVToColor(100, new float[]{0, 0, 1})), "false"}));
    public static final String PREF_CONTEXT_SIZE = PREF_TITLE.get(0),
            PREF_CONTEXT_SIZE_ROOM = PREF_TITLE.get(1),
            PREF_DARK_MODE = PREF_TITLE.get(2),
            PREF_WIDGET_TITLE_SIZE = PREF_TITLE.get(3),
            PREF_WIDGET_TITLE_BACKGROUND = PREF_TITLE.get(4),
            PREF_WIDGET_TITLE_COLOR = PREF_TITLE.get(5),
            PREF_WIDGET_CONTEXT_SIZE = PREF_TITLE.get(6),
            PREF_WIDGET_CONTEXT_SIZE_ROOM = PREF_TITLE.get(7),
            PREF_WIDGET_CONTEXT_BACKGROUND = PREF_TITLE.get(8),
            PREF_WIDGET_CONTEXT_COLOR = PREF_TITLE.get(9);


    public DBHelperTimeTable(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static DBHelperTimeTable getInstance(Context context){
        if(instance == null)instance = new DBHelperTimeTable(context, DB_NAME, null, DB_VERSION);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE  TABLE " + DB_TABLE +
                "(" + KEY_ID + " INTEGER PRIMARY KEY  NOT NULL , " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_ROOM + " TEXT, " +
                COLUMN_COLOR + " INTEGER)");
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, "");
        cv.put(COLUMN_ROOM, "");
        cv.put(COLUMN_COLOR, Color.HSVToColor(new float[]{340,0.749f,1}));
        for(int i = 0; i < COURSE_COUNT; i++)sqLiteDatabase.insert(DB_TABLE, null, cv);

        sqLiteDatabase.execSQL("CREATE  TABLE " + DB_TABLE_PREF +
                "(" + KEY_ID + " INTEGER PRIMARY KEY  NOT NULL , " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_ATTR + " TEXT)");
        for(int i = 0; i < PREF_TITLE.size(); i++)sqLiteDatabase.insert(DB_TABLE_PREF, null, getContentValue(PREF_TITLE.get(i), PREF_DEFAULT_ATTR.get(i)));
    }

    private ContentValues getContentValue(String title, String attr){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_ATTR, attr);
        return cv;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_PREF);
        onCreate(sqLiteDatabase);
        Log.e("Version", String.valueOf(i1));
    }

    public void setData(List<ContentValues> list){
        for (ContentValues cv : list) {
            int id = cv.getAsInteger("id");
            cv.remove("id");
            String where = KEY_ID + "=" + id;
            getWritableDatabase().update(DB_TABLE, cv, where,null);
        }
    }

    public Cursor getCursor(){
        Cursor cursor = getReadableDatabase().query(DB_TABLE, null, null, null, null, null, null, null);
        return cursor;
    }

    public List<ContentValues> getData(){
        Cursor cursor = getReadableDatabase().query(DB_TABLE, null, null, null, null, null, null, null);
        List<ContentValues> result = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                cv.put(COLUMN_ROOM, cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)));
                cv.put(COLUMN_COLOR, cursor.getString(cursor.getColumnIndex(COLUMN_COLOR)));
                result.add(cv);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public ContentValues getSingleData(int id){
        String where = KEY_ID + "=" + id;
        Cursor cursor = getReadableDatabase().query(DB_TABLE, null, where, null, null, null, null, null);
        ContentValues cv = null;
        if(cursor.moveToFirst()){
            cv = new ContentValues();
            cv.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            cv.put(COLUMN_ROOM, cursor.getString(cursor.getColumnIndex(COLUMN_ROOM)));
            cv.put(COLUMN_COLOR, cursor.getString(cursor.getColumnIndex(COLUMN_COLOR)));
        }
        cursor.close();
        return cv;
    }

    public String getPref(String title){
        String where = KEY_ID + "=" + (PREF_TITLE.indexOf(title) + 1);
        Cursor cursor = getReadableDatabase().query(DB_TABLE_PREF, null, where, null, null, null, null, null);
        String attr = null;
        if(cursor.moveToFirst())attr = cursor.getString(cursor.getColumnIndex(COLUMN_ATTR));
        cursor.close();
        return attr;
    }

    public void setPref(String title, String attr){
        String where = KEY_ID + "=" + (PREF_TITLE.indexOf(title) + 1);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, title);
        cv.put(COLUMN_ATTR, attr);
        getWritableDatabase().update(DB_TABLE_PREF, cv, where,null);
    }
}
