package com.meanz.simplesql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Ref: oblig3tips.pd
 * Created by Meanz on 04/03/2015.
 */
public class AndroidSQLHelper extends SQLiteOpenHelper {

    private String dbName;
    private int dbVersion;

    public AndroidSQLHelper(Context context, String dbName, int dbVersion)
    {
        super(context, dbName, null, dbVersion);
        this.dbName = dbName;
        this.dbVersion = dbVersion;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.err.println("ASQLHelper onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.err.println("ASQLHelper onUpgrade");
    }
}
