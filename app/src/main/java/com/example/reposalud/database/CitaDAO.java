package com.example.reposalud.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CitaDAO {
    // la parte del cap
    private DataBaseHelper dbHelper;

    public CitaDAO(Context context) {
        dbHelper = new DataBaseHelper(context);
        crearTablaHistorialesSiNoExiste();
    }

    private void crearTablaHistorialesSiNoExiste() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "CREATE TABLE IF NOT EXISTS historiales (" +
                "id INTEGER PRIMARY KEY, " +
                "paciente_id INTEGER, " +
                "fecha TEXT, " +
                "hora TEXT, " +
                "doctor_nombre TEXT, " +
                "especialidad_nombre TEXT, " +
                "diagnostico TEXT, " +
                "receta TEXT, " +
                "notas TEXT" +
                ");";
        db.execSQL(query);
    }

    public void limpiarCitasLocales(int usuarioId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("citas", "usuario_id = ?", new String[]{String.valueOf(usuarioId)});
        db.close();
    }

    public boolean insertarCita(int usuarioId, int doctorId, String fecha, String hora, String estado) {
        // la parte del cap
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usuario_id", usuarioId);
        values.put("doctor_id", doctorId);
        values.put("fecha", fecha);
        values.put("hora", hora);
        values.put("estado", estado);
        long resultado = db.insert("citas", null, values);
        db.close();
        return resultado != -1;
    }

    public Cursor obtenerUltimaCita(int usuarioId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM citas WHERE usuario_id=? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(usuarioId)}
        );
    }

    public Cursor obtenerEspecialidades() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM especialidades", null);
    }

    public Cursor obtenerDoctoresPorEspecialidad(int especialidadId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT d.*, e.nombre as especialidad_nombre " +
                        "FROM doctores d " +
                        "JOIN especialidades e ON d.especialidad_id = e.id " +
                        "WHERE d.especialidad_id = ?", 
                new String[]{String.valueOf(especialidadId)});
    }

    public Cursor obtenerTodosLosDoctores() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT d.*, e.nombre as especialidad_nombre " +
                        "FROM doctores d " +
                        "JOIN especialidades e ON d.especialidad_id = e.id", null);
    }

    public Cursor obtenerDoctorPorId(int doctorId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT d.*, e.nombre as especialidad_nombre " +
                        "FROM doctores d " +
                        "JOIN especialidades e ON d.especialidad_id = e.id " +
                        "WHERE d.id = ?", 
                new String[]{String.valueOf(doctorId)});
    }

    public void actualizarRatingDoctor(int doctorId, float nuevoRating) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE doctores SET rating = (rating * reviews + ?) / (reviews + 1), reviews = reviews + 1 WHERE id = ?",
                new Object[]{nuevoRating, doctorId});
        db.close();
    }

    public boolean existeDoctorPorNombre(String nombre) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM doctores WHERE nombre = ?", new String[]{nombre});
        boolean existe = (cursor.getCount() > 0);
        cursor.close();
        return existe;
    }

    public int obtenerOInsertarDoctor(String nombre, int especialidadId, String imagen, int backendId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM doctores WHERE nombre = ?", new String[]{nombre});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            db.execSQL("UPDATE doctores SET backend_id = ? WHERE id = ?", new Object[]{backendId, id});
            return id;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("backend_id", backendId);
        values.put("nombre", nombre);
        values.put("especialidad_id", especialidadId);
        values.put("horario", "Pendiente");
        values.put("biografia", "Información profesional del especialista no disponible por el momento.");
        values.put("rating", 5.0f);
        values.put("experiencia", 0);
        values.put("reviews", 0);
        values.put("imagen", imagen);
        long newId = db.insert("doctores", null, values);
        return (int) newId;
    }

    public void limpiarDoctores() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("doctores", "backend_id != -1", null);
        db.close();
    }

    public void limpiarDuplicadosOffline() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("doctores", "backend_id != -1 AND nombre NOT LIKE 'Dr. %' AND nombre NOT LIKE 'Dra. %'", null);
        db.close();
    }

    public int obtenerIdEspecialidad(String nombre) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM especialidades WHERE nombre = ?", new String[]{nombre});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        
        if (id == -1) {
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            long newId = db.insert("especialidades", null, values);
            id = (int) newId;
        }
        return id;
    }

    public void insertarDoctor(String nombre, int especialidadId, String imagen, int backendId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("backend_id", backendId);
        values.put("nombre", nombre);
        values.put("especialidad_id", especialidadId);
        values.put("horario", "Pendiente");
        values.put("biografia", "Información profesional del especialista no disponible por el momento.");
        values.put("rating", 5.0f);
        values.put("experiencia", 0);
        values.put("reviews", 0);
        values.put("imagen", imagen);
        db.insert("doctores", null, values);
        db.close();
    }

    public void actualizarDatosDoctorSync(String nombre, String nuevaImagen, int backendId, int especialidadId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE doctores SET imagen = ?, backend_id = ?, especialidad_id = ? WHERE nombre = ?", new Object[]{nuevaImagen, backendId, especialidadId, nombre});
        db.close();
    }

    // --- MÉTODOS PARA HISTORIAL MÉDICO ---
    
    public void limpiarHistorialLocal(int pacienteId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("historiales", "paciente_id = ?", new String[]{String.valueOf(pacienteId)});
        db.close();
    }

    public void insertarHistorialOffline(int pacienteId, String fecha, String hora, String doctorNombre, String especialidadNombre, String diagnostico, String receta, String notas) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("paciente_id", pacienteId);
        values.put("fecha", fecha);
        values.put("hora", hora);
        values.put("doctor_nombre", doctorNombre);
        values.put("especialidad_nombre", especialidadNombre);
        values.put("diagnostico", diagnostico);
        values.put("receta", receta);
        values.put("notas", notas);
        db.insert("historiales", null, values);
        db.close();
    }

    public Cursor obtenerHistorialOffline(int pacienteId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM historiales WHERE paciente_id = ? ORDER BY fecha DESC, hora DESC", new String[]{String.valueOf(pacienteId)});
    }
}
