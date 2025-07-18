// Path: app/src/main/java/com/example/uts/PahlawanDbHelper.java
package com.example.aplikasi_pahlantara;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PahlawanDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pahlawan_app.db";
    private static final int DATABASE_VERSION = 2; // Naikkan versi database jika ada perubahan skema

    // Tabel Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role"; // "admin", "penerbit", "user"

    // SQL untuk membuat tabel users
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT UNIQUE," +
                    COLUMN_PASSWORD + " TEXT," +
                    COLUMN_ROLE + " TEXT DEFAULT 'user')"; // Default role 'user'

    public PahlawanDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_TABLE);
        // Tambahkan user default jika diperlukan
        addDefaultUsers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Buat tabel baru lagi
        onCreate(db);
    }

    private void addDefaultUsers(SQLiteDatabase db) {
        // Admin
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USERNAME, "admin");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_ROLE, "admin");
        db.insert(TABLE_USERS, null, adminValues);

        // Penerbit
        ContentValues penerbitValues = new ContentValues();
        penerbitValues.put(COLUMN_USERNAME, "penerbit");
        penerbitValues.put(COLUMN_PASSWORD, "penerbit123");
        penerbitValues.put(COLUMN_ROLE, "penerbit");
        db.insert(TABLE_USERS, null, penerbitValues);

        // User
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USERNAME, "user");
        userValues.put(COLUMN_PASSWORD, "user123");
        userValues.put(COLUMN_ROLE, "user");
        db.insert(TABLE_USERS, null, userValues);

        Log.d("PahlawanDbHelper", "Default users added.");
    }

    // --- Metode untuk Manajemen User ---

    public long addUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public String getUserRole(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String role = null;
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ROLE},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            cursor.close();
        }
        db.close();
        return role;
    }

    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }
}