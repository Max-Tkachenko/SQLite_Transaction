package com.example.max.sqlite_transaction;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.Statement;

import javax.xml.transform.Result;

public class MainActivity extends AppCompatActivity {

    private static final String DB_NAME = "MySQLiteDatabase";
    private static final String TABLE_NAME = "MyTable";
    private SQLiteDatabase database;
    TextView tvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDB(); // инициализация базы данных
        tvTime = findViewById(R.id.tvTime);
    }

    public void insert_simple(View view) {
        long startInsert = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("FirstNumber", i);
            contentValues.put("SecondNumber", i);
            contentValues.put("Result", i * i);
            database.insert(TABLE_NAME, null, contentValues);
        }
        long finishInsert = System.currentTimeMillis() - startInsert;

        tvTime.setText("Time: " + finishInsert + " ms");
    } // вставка записей методом database.insert();

    public void insert_transaction(View view) {
        long startInsert = System.currentTimeMillis();

        database.beginTransaction();
        try {
            for (int i = 0; i < 1000; i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("FirstNumber", i);
                contentValues.put("SecondNumber", i);
                contentValues.put("Result", i * i);
                database.insert(TABLE_NAME, null, contentValues);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        long finishInsert = System.currentTimeMillis() - startInsert;
        tvTime.setText("Time: " + finishInsert + " ms");
    } // вставка записей через транзакцию

    public void insert_transactionStatement(View view) {
        long startInsert = System.currentTimeMillis();

        String sqlQuery = "INSERT INTO " + TABLE_NAME + " VALUES (?,?,?)";
        SQLiteStatement statement = database.compileStatement(sqlQuery);
        database.beginTransaction();
        try {
            for (int i = 0; i < 1000; i++) {
                statement.clearBindings();
                statement.bindLong(1, i);
                statement.bindLong(2, i);
                statement.bindLong(3, i * i);
                statement.execute();
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        long finishInsert = System.currentTimeMillis() - startInsert;
        tvTime.setText("Time: " + finishInsert + " ms");
    } // вставка записей через транзакцию с использованием Statement

    public void deleteRows(View view) {
        database.delete(TABLE_NAME, null, null);
    }

    public void showCountInfo(View view) {
        int count = database.rawQuery("SELECT * FROM " + TABLE_NAME, null).getCount();
        String message = count + " rows in databse";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initDB() {
        database = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(FirstNumber INT, SecondNumber INT, Result INT);");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
