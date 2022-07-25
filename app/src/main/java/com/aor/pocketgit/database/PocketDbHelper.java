package com.aor.pocketgit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PocketDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "PocketGit.db";
    public static final int DATABASE_VERSION = 2;

    public PocketDbHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 2);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE projects (id INTEGER PRIMARY KEY,name VARCHAR,url VARCHAR,local_path VARCHAR,authentication INTEGER,username VARCHAR,password VARCHAR,privatekey VARCHAR,state VARCHAR)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE projects ADD COLUMN state VARCHAR");
            db.execSQL("UPDATE PROJECTS SET state = 'UNKNOWN'");
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
