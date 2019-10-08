package com.example.davidtruong.list;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//Use this class to make the database
public class databaseHelper extends SQLiteOpenHelper {
    //If we change the database schema, change the database version
    private static final int DATABASE_VERSION = 1;
    public static String DATABASE_NAME;
    final String DBHELPTAG = "Helper_database";
    public static final String tableName = "Maintable";
    public static final String dbcol1 = "Category";
    public static final String dbcol2 = "Total";
    private Context currContext;

    //These are the names of the columns for the underlying tables
    public static final String tableCol1 = "Detail";
    public static final String tableCol2 = "Amount";
    public static final String tableCol3 = "Date";

    //Change the constructor
    public databaseHelper (Context context) {
        //Used to create the actual database
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.currContext = context;
    }

    //Fill the database with the main table
    public void onCreate(SQLiteDatabase db) {
        //We don't pass in the string directly so that dbExec helps with our syntax
        String tester =
                "CREATE TABLE IF NOT EXISTS Maintable (" +
                "Category text PRIMARY KEY, " +
                "Total real DEFAULT 0" +
                ");"
                ;
        Log.d(DBHELPTAG, "Executing command: " + tester);
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS Maintable (" +
                "Category text PRIMARY KEY, " +
                "Total integer DEFAULT 0" +
                ");"
        );
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //When you change the database version, simply delete and remake the database
        //Currently causes it to crash
        Log.d(DBHELPTAG, "Upgrade starting." +
                "Old version: " + oldVersion + "\n" +
                "New version: " + newVersion
        );
        this.currContext.deleteDatabase(DATABASE_NAME);
        SQLiteDatabase newdb = getWritableDatabase();
        onCreate(newdb);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Do the same thing as upgrade: delete and remake the database
        onUpgrade(db, oldVersion, newVersion);
    }

    public void deleteDatabase(SQLiteDatabase db)
    {
        //Use to delete the database, one of the two options I can't decide between
        //Delete all the tables from the database, vacuum the leftover space
        db.execSQL(
                "Begin Transaction;" +
                        "PRAGMA writable_schema = 1; " +
                        "delete from sqlite_master where type in ('table', 'index', 'trigger');" +
                        "PRAGMA writable_schema = 0;" +
                        "COMMIT;" +
                        "VACUUM;"
        );
        Boolean isOk = db.isDatabaseIntegrityOk();
        if (!isOk)
        {
            Log.d(DBHELPTAG, "Database is corrupted. Exiting with error");
            System.exit(1);
        }
    }

}
