// Path: app/src/main/java/com/example/uts/DatabaseHelper.java
package com.example.aplikasi_pahlantara;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pahlawan.db";
    private static final int DATABASE_VERSION = 6; // Naikkan versi database!

    // Tabel Users (Hanya ini yang akan dikelola oleh DatabaseHelper ini)
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id"; // Ini ID lokal (int)
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_ACC_PENERBIT = "acc_penerbit";

    // SQL untuk membuat tabel users
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_ROLE + " TEXT,"
            + COLUMN_ACC_PENERBIT + " INTEGER DEFAULT 0" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        // Tambahkan admin default ke DB lokal untuk bootstrapping
        addAdminUser(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // Hapus hanya tabel users
        onCreate(db);
    }

    // --- Operasi CRUD untuk Tabel Users (Ini akan disederhanakan) ---
    // Sebagian besar operasi user akan pindah ke API,
    // DatabaseHelper ini hanya akan menyimpan admin default jika diperlukan.

    // Menambahkan user admin default (tetap di lokal)
    private void addAdminUser(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_USERNAME + "=?",
                    new String[]{"admin"}, // Username admin default
                    null, null, null, null);

            if (cursor == null || cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_USERNAME, "admin");
                values.put(COLUMN_PASSWORD, "admin123"); // Password admin default
                values.put(COLUMN_ROLE, "admin");
                values.put(COLUMN_ACC_PENERBIT, 1); // Admin otomatis disetujui
                db.insert(TABLE_USERS, null, values);
                Log.d("DatabaseHelper", "Admin user added: admin/admin123");
            } else {
                Log.d("DatabaseHelper", "Admin user already exists.");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Metode untuk otentikasi user lokal (hanya untuk admin default jika tidak ada API)
    public User getUserLocal(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_ROLE, COLUMN_ACC_PENERBIT},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))), // ID lokal sebagai String
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACC_PENERBIT)) == 1);
            cursor.close();
        }
        db.close();
        return user;
    }

    // Metode ini mungkin tidak lagi digunakan jika semua user dari API
    public boolean checkUsernameExistsLocal(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

}