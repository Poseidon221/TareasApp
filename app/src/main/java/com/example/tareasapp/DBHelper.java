package com.example.tareasapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "tasks.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE tasks (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "titulo TEXT," +
                        "descripcion TEXT," +
                        "estado TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }

    // ➕ INSERTAR
    public void insertTask(String titulo, String descripcion, String estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("titulo", titulo);
        values.put("descripcion", descripcion);
        values.put("estado", estado);

        db.insert("tasks", null, values);
    }

    // 📋 LEER TODO
    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM tasks", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Task(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    // 🔍 LEER POR ID
    public Task getTaskById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM tasks WHERE id=" + id,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            return new Task(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
        }

        return null;
    }

    // ✏️ ACTUALIZAR
    public void updateTask(int id, String titulo, String descripcion, String estado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("titulo", titulo);
        values.put("descripcion", descripcion);
        values.put("estado", estado);

        db.update("tasks", values, "id=?", new String[]{String.valueOf(id)});
    }

    // ❌ ELIMINAR
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "id=?", new String[]{String.valueOf(id)});
    }
}
