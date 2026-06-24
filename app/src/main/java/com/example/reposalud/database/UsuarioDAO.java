package com.example.reposalud.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UsuarioDAO {
    // la parte del cap
    private DataBaseHelper dbHelper;

    public UsuarioDAO(Context context) {
        dbHelper = new DataBaseHelper(context);
    }

    public boolean insertarUsuario(String nombre, String correo, String password) {
        // la parte del cap
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("password", password);
        long resultado = db.insert("usuarios", null, values);
        db.close();
        return resultado != -1;
    }

    public boolean loginUsuario(String correo, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM usuarios WHERE correo=? AND password=?",
                new String[]{correo, password}
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public String obtenerNombreUsuario(String correo, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nombre FROM usuarios WHERE correo=? AND password=?",
                new String[]{correo, password}
        );
        String nombre = null;
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return nombre;
    }

    public int obtenerIdUsuario(String correo, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM usuarios WHERE correo=? AND password=?",
                new String[]{correo, password}
        );
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return id;
    }

    public boolean existeCorreo(String correo) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM usuarios WHERE correo=?",
                new String[]{correo}
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public String registrarOloginGoogle(String nombre, String correo) {
        // la parte del cap
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre FROM usuarios WHERE correo = ?", new String[]{correo});

        if (cursor.moveToFirst()) {
            String nombreExistente = cursor.getString(0);
            cursor.close();
            return nombreExistente;
        }
        cursor.close();

        SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo);
        values.put("password", "google_auth_linked");
        dbWrite.insert("usuarios", null, values);
        dbWrite.close();

        return nombre;
    }

    public boolean actualizarNombreUsuario(String nuevoNombre, String correo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nuevoNombre);
        int filasAfectadas = db.update("usuarios", values, "correo=?", new String[]{correo});
        db.close();
        return filasAfectadas > 0;
    }
}
