package com.example.reposalud.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    // la parte del cap
    private static final String DATABASE_NAME = "citas.db";
    private static final int DATABASE_VERSION = 11;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // la parte del cap
        String tablaUsuarios = "CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "correo TEXT NOT NULL, " +
                "password TEXT NOT NULL, " +
                "telefono TEXT" +
                ");";

        String tablaEspecialidades = "CREATE TABLE especialidades (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL" +
                ");";

        String tablaDoctores = "CREATE TABLE doctores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "backend_id INTEGER DEFAULT -1, " +
                "nombre TEXT NOT NULL, " +
                "especialidad_id INTEGER, " +
                "horario TEXT, " +
                "biografia TEXT, " +
                "rating REAL DEFAULT 0.0, " +
                "experiencia INTEGER DEFAULT 0, " +
                "reviews INTEGER DEFAULT 0, " +
                "imagen TEXT, " +
                "FOREIGN KEY (especialidad_id) REFERENCES especialidades(id)" +
                ");";

        String tablaCitas = "CREATE TABLE citas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario_id INTEGER, " +
                "doctor_id INTEGER, " +
                "fecha TEXT, " +
                "hora TEXT, " +
                "estado TEXT, " +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id), " +
                "FOREIGN KEY (doctor_id) REFERENCES doctores(id)" +
                ");";

        db.execSQL(tablaUsuarios);
        db.execSQL(tablaEspecialidades);
        db.execSQL(tablaDoctores);
        db.execSQL(tablaCitas);

        // Insertar datos iniciales
        db.execSQL("INSERT INTO especialidades (nombre) VALUES ('Cardiología')");
        db.execSQL("INSERT INTO especialidades (nombre) VALUES ('Pediatría')");
        db.execSQL("INSERT INTO especialidades (nombre) VALUES ('Medicina General')");
        db.execSQL("INSERT INTO especialidades (nombre) VALUES ('Ginecología')");
        db.execSQL("INSERT INTO especialidades (nombre) VALUES ('Nutrición')");
        db.execSQL("INSERT INTO especialidades (nombre) VALUES ('Psicología')");

        // Cardiología (ID: 1)
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Alejandro Méndez', 1, '08:00 - 16:00', 'El Dr. Alejandro Méndez es un referente en cardiología intervencionista con más de 12 años de práctica clínica. Se especializa en el tratamiento de enfermedades coronarias complejas.', 5.0, 12, 240, 'alejandro_mendez')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Abran Lincon', 1, '09:00 - 17:00', 'El Dr. Abran Lincon destaca por su expertise en electrofisiología. Utiliza las técnicas más avanzadas para el tratamiento de arritmias.', 4.8, 10, 152, 'abran_lincon')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Bruno Diaz', 1, '10:00 - 18:00', 'El Dr. Bruno Diaz es especialista en cardiología preventiva. Su filosofía se basa en que la mejor medicina es un estilo de vida saludable.', 4.7, 9, 118, 'bruno_diaz')");

        // Pediatría (ID: 2)
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dra. Sofia Valdivia', 2, '10:00 - 18:00', 'La Dra. Sofía Valdivia es una apasionada de la salud infantil. Con 8 años de experiencia, se ha especializado en el desarrollo neurocognitivo.', 4.9, 8, 185, 'sofia_valdivia')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Jonny Cage', 2, '09:00 - 17:00', 'El Dr. Jonny Cage combina su experiencia en pediatría con una subespecialidad en medicina deportiva.', 4.9, 7, 134, 'jonny_cage')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Ernesto Pimentel', 2, '08:00 - 16:00', 'El Dr. Ernesto Pimentel es un pediatra con amplia experiencia en salud pública y enfoque preventivo.', 4.8, 11, 167, 'ernesto_pimentel')");

        // Medicina General (ID: 3)
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Ivan Panchano', 3, '08:00 - 16:00', 'El Dr. Ivan Panchano ofrece atención integral a pacientes adultos, con especial enfoque en enfermedades crónicas.', 4.6, 15, 89, 'ivan_panchano')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Junior Ruiz Ruiz', 3, '09:00 - 17:00', 'Comprometido con la medicina preventiva y el bienestar familiar.', 4.7, 5, 45, 'junior_ruiz_ruiz')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Kenny Quinonez', 3, '10:00 - 18:00', 'Médico general con amplia trayectoria en emergencias y atención primaria.', 4.5, 10, 76, 'quenny_quinonez')");

        // Ginecología (ID: 4)
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dra. Alysson Perez', 4, '10:00 - 18:00', 'Con más de 15 años de trayectoria, la Dra. Alysson es experta en salud integral femenina.', 4.9, 15, 312, 'alysson_perez')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dra. Sonya Blade', 4, '09:00 - 17:00', 'Especialista en obstetricia y cuidado prenatal de alto riesgo.', 4.8, 12, 198, 'sonya_blade')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Fabricio Farfan', 4, '08:00 - 16:00', 'Ginecólogo dedicado a la salud reproductiva y cirugía mínimamente invasiva.', 4.7, 14, 145, 'fabricio_farfan')");

        // Nutrición (ID: 5)
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Juan Pereira', 5, '08:00 - 16:00', 'Especialista en nutrición clínica y deportiva.', 4.8, 6, 88, 'juan_pereira')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Alexander Tineo', 5, '09:00 - 17:00', 'Experto en manejo metabólico y pérdida de peso saludable.', 4.7, 8, 92, 'alexander_tineo')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dra. Fiorella Rodriguez', 5, '10:00 - 18:00', 'Nutricionista enfocada en alimentación consciente y trastornos alimentarios.', 4.9, 7, 110, 'fiorella_rodriguez')");

        // Psicología (ID: 6)
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dra. Maria Pia', 6, '10:00 - 18:00', 'Psicóloga clínica especializada en terapia cognitivo-conductual.', 4.9, 10, 205, 'maria_pia')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Jhosep Augil', 6, '09:00 - 17:00', 'Experto en salud mental adolescente y resolución de conflictos.', 4.8, 9, 167, 'logo_solo')");
        db.execSQL("INSERT INTO doctores (nombre, especialidad_id, horario, biografia, rating, experiencia, reviews, imagen) VALUES " +
                "('Dr. Junior Cinturon', 6, '08:00 - 16:00', 'Psicólogo con enfoque en terapia familiar y de pareja.', 4.7, 12, 134, 'junior_cinturon')");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // la parte del cap
        db.execSQL("DROP TABLE IF EXISTS citas");
        db.execSQL("DROP TABLE IF EXISTS doctores");
        db.execSQL("DROP TABLE IF EXISTS especialidades");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }
}
