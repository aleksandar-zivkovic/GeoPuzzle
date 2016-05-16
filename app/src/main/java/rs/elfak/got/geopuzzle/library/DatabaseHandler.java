package rs.elfak.got.geopuzzle.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.HashMap;

/**
 * Created by Milan on 14.5.2016..
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "geopuzzle";
    private static final String TABLE_LOGIN = "login";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + Cons.KEY_ID + " INTEGER PRIMARY KEY,"
                + Cons.KEY_FIRSTNAME + " TEXT,"
                + Cons.KEY_LASTNAME + " TEXT,"
                + Cons.KEY_EMAIL + " TEXT UNIQUE,"
                + Cons.KEY_USERNAME + " TEXT,"
                + Cons.KEY_PHONE_NUMBER + " TEXT,"
                + Cons.KEY_UID + " TEXT,"
                + Cons.KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        onCreate(db);
    }

    // Storing user details in database
    public void addUser(String firstName, String lastName, String email, String username, String phoneNumber, String userId, String createdAt) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Cons.KEY_FIRSTNAME, firstName);
        values.put(Cons.KEY_LASTNAME, lastName);
        values.put(Cons.KEY_EMAIL, email);
        values.put(Cons.KEY_USERNAME, username);
        values.put(Cons.KEY_PHONE_NUMBER, phoneNumber);
        values.put(Cons.KEY_UID, userId);
        values.put(Cons.KEY_CREATED_AT, createdAt);

        // Inserting Row
        db.insert(TABLE_LOGIN, null, values);
        db.close(); // Closing database connection
    }

    // Getting user data from database
    public HashMap getUserDetails() {
        HashMap user = new HashMap();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0) {
            user.put(Cons.KEY_FIRSTNAME, cursor.getString(1));
            user.put(Cons.KEY_LASTNAME, cursor.getString(2));
            user.put(Cons.KEY_EMAIL, cursor.getString(3));
            user.put(Cons.KEY_USERNAME, cursor.getString(4));
            user.put(Cons.KEY_PHONE_NUMBER, cursor.getString(5));
            user.put(Cons.KEY_UID, cursor.getString(6));
            user.put(Cons.KEY_CREATED_AT, cursor.getString(7));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

    // Getting user login status
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    // Re create database
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }
}