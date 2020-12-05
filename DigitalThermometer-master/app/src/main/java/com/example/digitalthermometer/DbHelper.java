package com.example.digitalthermometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "MyReadings.db";
    public static final String READINGS_TABLE_NAME = "readings";
    public static final String READINGS_COLUMN_ID = "id";
    public static final String READINGS_COLUMN_TIME = "time";
    public static final String READINGS_COLUMN_TEMP = "temperature";
    public static final String READINGS_COLUMN_SYMPTOMS = "symptoms";

    private final String TIME_FORMAT = "HH:mm:ss MM/dd/yyyy";


    private final String DELETE_FROM_DB = "DELETE FROM readings WHERE id = ";
    private final String SORT_DB_BY = "SELECT * FROM readings ORDER BY ";

    public DbHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table readings" + "(id integer primary key, time text, temperature text, symptoms text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS readings");
        onCreate(db);
    }

    // WORKING
    public long insertReading(Reading reading){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        DateFormat df = new SimpleDateFormat(TIME_FORMAT);

        contentValues.put("time", df.format(reading.time));
        contentValues.put("temperature", reading.temp);
        contentValues.put("symptoms", reading.symptoms);

        return db.insert("readings", null, contentValues);
    }

    public Reading getReading(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from readings where id=" + id + "", null);
        res.moveToFirst();

        Reading mReading = new Reading();
        mReading.temp = res.getDouble(res.getColumnIndex(READINGS_COLUMN_TEMP));
        try {
            DateFormat format = new SimpleDateFormat(TIME_FORMAT);
            mReading.time = format.parse(res.getString(res.getColumnIndex(READINGS_COLUMN_TIME)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mReading.id = res.getInt(res.getColumnIndex(READINGS_COLUMN_ID));
        mReading.symptoms = res.getString(res.getColumnIndex(READINGS_COLUMN_SYMPTOMS));

        return mReading;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, READINGS_TABLE_NAME);
        return numRows;
    }

    public int updateReading(Reading newData, String oldId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        DateFormat df = new SimpleDateFormat(TIME_FORMAT);

        cv.put(READINGS_COLUMN_TEMP, String.valueOf(newData.temp));
        cv.put(READINGS_COLUMN_TIME, String.valueOf(df.format(newData.time)));
        cv.put(READINGS_COLUMN_SYMPTOMS, String.valueOf(newData.symptoms));

        return db.update(READINGS_TABLE_NAME, cv, "id = ?", new String[]{oldId});
    }

    // WORKING
    public void deleteReading (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(DELETE_FROM_DB + Integer.toString(id));

        /*return db.delete("readings",
                "id = " + Integer.toString(id), null); */
    }

    // WORKING
    public ArrayList<Reading> getAllReadings() {
        ArrayList<Reading> array_list = new ArrayList<Reading>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from readings", null);
        res.moveToFirst();

        while(res.isAfterLast() == false){

            Reading mReading = new Reading();
            mReading.temp = res.getDouble(res.getColumnIndex(READINGS_COLUMN_TEMP));
            try {
                DateFormat format = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
                mReading.time = format.parse(res.getString(res.getColumnIndex(READINGS_COLUMN_TIME)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mReading.id = res.getInt(res.getColumnIndex(READINGS_COLUMN_ID));
            mReading.symptoms = res.getString(res.getColumnIndex(READINGS_COLUMN_SYMPTOMS));

            array_list.add(mReading);
            res.moveToNext();
        }

        return array_list;
    }

    public ArrayList<Reading> sortReadings(String column, String type) {
        ArrayList<Reading> array_list = new ArrayList<Reading>();

        String castQuery;
        if (type != "Date")
            castQuery = "cast(" + column + " as " + type + ")";
        else
            castQuery = "convert(datetime, time, 103) ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(SORT_DB_BY + castQuery, null);
        res.moveToFirst();

        while(res.isAfterLast() == false){

            Reading mReading = new Reading();
            mReading.temp = res.getDouble(res.getColumnIndex(READINGS_COLUMN_TEMP));
            try {
                DateFormat format = new SimpleDateFormat(TIME_FORMAT);
                mReading.time = format.parse(res.getString(res.getColumnIndex(READINGS_COLUMN_TIME)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mReading.id = res.getInt(res.getColumnIndex(READINGS_COLUMN_ID));
            mReading.symptoms = res.getString(res.getColumnIndex(READINGS_COLUMN_SYMPTOMS));

            array_list.add(mReading);
            res.moveToNext();
        }

        return array_list;
    }


    // Currently exporting data to downloads folder
    // If -1 get export all readings
    public boolean exportDBtoCSV(int id) {
        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "");
        Log.e("DbHelper", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        if (!exportDir.exists()){
            exportDir.mkdir();
        }

        File file = new File(exportDir, "myReadings.csv");

        try {
            file.createNewFile();
            CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor curCSV;
            if (id == -1)
                curCSV = db.rawQuery("SELECT * FROM readings", null);
            else
                curCSV = db.rawQuery("SELECT * FROM readings WHERE id=" + id + "", null);
            csvWriter.writeNext(curCSV.getColumnNames());

            DateFormat df = new SimpleDateFormat(TIME_FORMAT);

            while(curCSV.moveToNext())
            {
                // Specify exporting columns here
                String arrStr[] ={curCSV.getString(0), String.valueOf(df.parse(curCSV.getString(1))), curCSV.getString(2), curCSV.getString(3)};
                csvWriter.writeNext(arrStr);
            }
            csvWriter.close();
            curCSV.close();

            return true;
        }
        catch (Exception ex)
        {
            Log.e("DbHelper", ex.getMessage(), ex);
            return false;
        }

    }

}
