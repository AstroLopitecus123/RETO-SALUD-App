package com.example.reposalud.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reposalud.R;
import com.example.reposalud.database.CitaDAO;
// la parte del cap
import com.example.reposalud.utils.NavigationHelper;

public class HomeActivity extends AppCompatActivity {

    private CitaDAO citaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        citaDAO = new CitaDAO(this);
        TextView tvHolaUser = findViewById(R.id.tvHolaUser);

        String nombre = getIntent().getStringExtra("nombre_usuario");
        tvHolaUser.setText("Hola, " + (nombre != null ? nombre : "Usuario"));

        // Configurar botones de navegación
        findViewById(R.id.btnComenzar).setOnClickListener(v -> {
            Intent intent = new Intent(this, AgendarCitaActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvVerTodasEspecialidades).setOnClickListener(v -> {
            Intent intent = new Intent(this, EspecialidadesActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.tvExplorarDoctores).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaDoctoresActivity.class);
            startActivity(intent);
        });

        // Configurar clics en categorías
        findViewById(R.id.llCardiologia).setOnClickListener(v -> abrirDetalleEspecialidad(1, "Cardiología"));
        findViewById(R.id.llPediatria).setOnClickListener(v -> abrirDetalleEspecialidad(2, "Pediatría"));
        findViewById(R.id.llMedicinaGeneral).setOnClickListener(v -> abrirDetalleEspecialidad(3, "Medicina General"));
        findViewById(R.id.llGinecologia).setOnClickListener(v -> abrirDetalleEspecialidad(4, "Ginecología"));

        // Configurar clics en doctores destacados
        findViewById(R.id.cvDoctor1).setOnClickListener(v -> {
            Intent intent = new Intent(this, DetalleDoctorActivity.class);
            intent.putExtra("doctor_id", 1);
            startActivity(intent);
        });

        findViewById(R.id.cvDoctor2).setOnClickListener(v -> {
            Intent intent = new Intent(this, DetalleDoctorActivity.class);
            intent.putExtra("doctor_id", 4);
            startActivity(intent);
        });

        // Abrir Mis Citas Médicas
        findViewById(R.id.cardProximaCita).setOnClickListener(v -> {
            Intent intent = new Intent(this, MisCitasActivity.class);
            startActivity(intent);
        });

        // Configurar Bottom Navigation (desde el include)
        // la parte del cap
        NavigationHelper.setupBottomNavigation(this);
    }

    private void abrirDetalleEspecialidad(int id, String nombre) {
        Intent intent = new Intent(this, DetalleEspecialidadActivity.class);
        intent.putExtra("especialidad_id", id);
        intent.putExtra("especialidad_nombre", nombre);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProximaCita();
    }

    private void cargarProximaCita() {
        int usuarioId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("id_usuario", 1);
        Cursor cursor = citaDAO.obtenerUltimaCita(usuarioId);
        android.view.View cardProximaCita = findViewById(R.id.cardProximaCita);

        if (cursor != null && cursor.moveToFirst()) {
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"));
            String hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"));
            int doctorId = cursor.getInt(cursor.getColumnIndexOrThrow("doctor_id"));

            Cursor doctorCursor = citaDAO.obtenerDoctorPorId(doctorId);
            if (doctorCursor != null && doctorCursor.moveToFirst()) {
                String doctorNombre = doctorCursor.getString(doctorCursor.getColumnIndexOrThrow("nombre"));
                String especialidadNombre = doctorCursor.getString(doctorCursor.getColumnIndexOrThrow("especialidad_nombre"));

                TextView tvFecha = findViewById(R.id.tvProximaFecha);
                TextView tvDoctor = findViewById(R.id.tvProximoDoctor);
                TextView tvEspecialidad = findViewById(R.id.tvProximaEspecialidad);

                String fechaHora = fecha + " • " + hora;
                try {
                    String fechaLimpia = fechaHora.replace(" • ", " ").trim();
                    java.text.SimpleDateFormat originalFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                    java.util.Date date = originalFormat.parse(fechaLimpia);
                    
                    java.text.SimpleDateFormat niceFormat = new java.text.SimpleDateFormat("EEEE, d 'de' MMMM '•' hh:mm a", new java.util.Locale("es", "ES"));
                    String formattedDate = niceFormat.format(date);

                    formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
                    
                    fechaHora = formattedDate.replace("•", "• ").replace("  ", " ");
                } catch (Exception e) {
                }

                tvFecha.setText(fechaHora);
                tvDoctor.setText("Dr. " + doctorNombre);
                tvEspecialidad.setText(especialidadNombre);

                cardProximaCita.setVisibility(android.view.View.VISIBLE);
                doctorCursor.close();
            } else {
                cardProximaCita.setVisibility(android.view.View.GONE);
            }
            cursor.close();
        } else {
            cardProximaCita.setVisibility(android.view.View.GONE);
        }
    }
}
