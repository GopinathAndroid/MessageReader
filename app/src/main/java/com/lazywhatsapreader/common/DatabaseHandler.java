package com.lazywhatsapreader.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lazywhatsapreader.activities.SpeechMainActivity;
import com.lazywhatsapreader.interfaces.UpdateInterface;
import com.lazywhatsapreader.pogo.Message;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "messagedb";
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    public static final String MESSAGE_TABLE_NAME = "messagetable";
    public static final String MESSAGE_COLUMN_MSG1 = "MSG1"; //full copied message
    public static final String MESSAGE_COLUMN_MSG2 = "MSG2"; //removed whtasapp date using regex
    public static final String MESSAGE_COLUMN_MSG3 = "MSG3";
    public static final String MESSAGE_COLUMN_MSG4 = "MSG4"; //first 150 char
    public static final String MESSAGE_COLUMN_TIME = "TIME"; //created time
    public static final String MESSAGE_COLUMN_OPEN = "OPEN"; //message opened or not
    public static final String MESSAGE_COLUMN_SID = "SID";
    private static DatabaseHandler instance = null;


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* Static 'instance' method */
    public static DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        String CREATE_MESSAGE_TABLE = "CREATE TABLE IF NOT EXISTS " + MESSAGE_TABLE_NAME + "("
                + MESSAGE_COLUMN_MSG1 + " TEXT," + MESSAGE_COLUMN_MSG2 + " TEXT," + MESSAGE_COLUMN_MSG3
                + " TEXT," + MESSAGE_COLUMN_MSG4
                + " TEXT," + MESSAGE_COLUMN_TIME + " TEXT," + MESSAGE_COLUMN_OPEN + " INTEGER NOT NULL," +
                MESSAGE_COLUMN_SID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT"
                + ")";

        //	db.execSQL("CREATE TABLE IF NOT EXISTS messagetable(MSG1 VARCHAR,MSG2 VARCHAR,MSG3 VARCHAR,MSG4 VARCHAR,TIME VARCHAR,OPEN INTEGER NOT NULL,SID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT);");
        db.execSQL(CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    //////////////////////////////////////////////////////

    // Adding single new message
    public void addMessage(Message messages) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MESSAGE_COLUMN_MSG1, messages.getMsg1());
        values.put(MESSAGE_COLUMN_MSG2, messages.getMsg2());
        values.put(MESSAGE_COLUMN_MSG3, messages.getMsg3());
        values.put(MESSAGE_COLUMN_MSG4, messages.getMsg4());
        values.put(MESSAGE_COLUMN_OPEN, messages.getOpen());
        values.put(MESSAGE_COLUMN_TIME, messages.getTime());

        // Inserting Row
        db.insert(MESSAGE_TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }



    //getting all messages using cursor
    public Cursor loadAllMessages() {
        //   String selectQuery = "SELECT  rowid _id,* FROM " + MESSAGE_TABLE_NAME +" ORDER BY "+MESSAGE_COLUMN_SID+" DESC"; //reverse
        // Select All Query
        String selectQuery = "SELECT  rowid _id,* FROM " + MESSAGE_TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor;


    }

    // Updating single contact id=1(played)
    public int updateMessage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MESSAGE_COLUMN_OPEN, 1);

        // updating row
        return db.update(MESSAGE_TABLE_NAME, values, MESSAGE_COLUMN_SID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public int updateEditedMessageMessage(int id,String msg1,String msg2,String msg3,String msg4) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MESSAGE_COLUMN_MSG1, msg1);
        values.put(MESSAGE_COLUMN_MSG2, msg2);
        values.put(MESSAGE_COLUMN_MSG3, msg3);
        values.put(MESSAGE_COLUMN_MSG4, msg4);

        // updating row
        return db.update(MESSAGE_TABLE_NAME, values, MESSAGE_COLUMN_SID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Deleting single contact
    public void deleteMessage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MESSAGE_TABLE_NAME, MESSAGE_COLUMN_SID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        UpdateInterface obj = new SpeechMainActivity();
        obj.messageDeleted();

    }



}
