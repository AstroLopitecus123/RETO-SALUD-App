package com.example.reposalud.activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.reposalud.R;
import com.example.reposalud.database.CitaDAO;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialMedicoActivity extends BaseActivity {

    private LinearLayout containerHistorial;
    private TextView tvEmptyState;
    private CitaDAO citaDAO;
    private int pacienteId = -1;
    private String apiToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_medico);

        containerHistorial = findViewById(R.id.containerHistorial);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        citaDAO = new CitaDAO(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Historial Clínico");
        }
        toolbar.setNavigationOnClickListener(v -> finish());



        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        pacienteId = prefs.getInt("id_usuario", -1);
        apiToken = prefs.getString("api_token", "");

        if (pacienteId == -1) {
            tvEmptyState.setText("No se pudo identificar al usuario.");
            return;
        }

        cargarHistorialOffline();
        obtenerHistorialDeLaWeb();
    }

    private void cargarHistorialOffline() {
        containerHistorial.removeAllViews();
        Cursor cursor = citaDAO.obtenerHistorialOffline(pacienteId);

        if (cursor != null && cursor.moveToFirst()) {
            tvEmptyState.setVisibility(View.GONE);
            do {
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"));
                String hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"));
                String doctorNombre = cursor.getString(cursor.getColumnIndexOrThrow("doctor_nombre"));
                String especialidadNombre = cursor.getString(cursor.getColumnIndexOrThrow("especialidad_nombre"));
                String diagnostico = cursor.getString(cursor.getColumnIndexOrThrow("diagnostico"));
                String receta = cursor.getString(cursor.getColumnIndexOrThrow("receta"));
                String notas = cursor.getString(cursor.getColumnIndexOrThrow("notas"));

                agregarTarjetaHistorial(fecha + " • " + hora, "Dr. " + doctorNombre + " - " + especialidadNombre, diagnostico, receta, notas);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Buscando historial...");
        }
    }

    private void obtenerHistorialDeLaWeb() {
        String authHeader = "Bearer " + apiToken;
        RetrofitClient.getApiService().obtenerHistorialPorPaciente(authHeader, pacienteId).enqueue(new Callback<List<ApiService.HistorialResponse>>() {
            @Override
            public void onResponse(Call<List<ApiService.HistorialResponse>> call, Response<List<ApiService.HistorialResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiService.HistorialResponse> historiales = response.body();
                    
                    if (historiales.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Aún no tienes historias clínicas registradas.");
                        containerHistorial.removeAllViews();
                        return;
                    }

                    citaDAO.limpiarHistorialLocal(pacienteId);

                    for (ApiService.HistorialResponse h : historiales) {
                        if (h.cita != null) {
                            String doctorNombre = h.cita.medico != null && h.cita.medico.usuario != null 
                                    ? h.cita.medico.usuario.nombre + " " + h.cita.medico.usuario.apellido 
                                    : "Desconocido";
                            String especialidad = h.cita.especialidad != null ? h.cita.especialidad.nombre : "General";

                            String fechaFinal = h.cita.fecha;
                            String horaFinal = h.cita.hora;

                            if (fechaFinal != null && fechaFinal.contains("T")) {
                                String[] parts = fechaFinal.split("T");
                                fechaFinal = parts[0];
                                if (parts.length > 1) {
                                    horaFinal = parts[1];
                                    if (horaFinal.length() > 5) {
                                        horaFinal = horaFinal.substring(0, 5); // Tomar solo HH:mm
                                    }
                                }
                            }

                            if (horaFinal == null || horaFinal.equals("null")) {
                                horaFinal = "";
                            }

                            citaDAO.insertarHistorialOffline(pacienteId, fechaFinal, horaFinal, doctorNombre, especialidad, h.diagnostico, h.receta, h.notas);
                        }
                    }
                    // Recargar vista desde SQLite
                    cargarHistorialOffline();
                } else {
                    if (containerHistorial.getChildCount() == 0) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        if (response.code() == 401 || response.code() == 403) {
                            tvEmptyState.setText("Acceso denegado (401). Tu sesión expiró o no tienes permisos, por favor vuelve a iniciar sesión en la app.");
                        } else if (response.code() == 404) {
                            tvEmptyState.setText("No se encontraron registros clínicos (404) en el servidor.");
                        } else {
                            tvEmptyState.setText("No se pudo cargar el historial de la web (Error HTTP: " + response.code() + ").");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.HistorialResponse>> call, Throwable t) {
                if (containerHistorial.getChildCount() == 0) {
                    tvEmptyState.setText("Sin conexión. No se encontró historial offline.");
                } else {
                    Toast.makeText(HistorialMedicoActivity.this, "Mostrando versión offline", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void agregarTarjetaHistorial(String fechaHora, String doctorEsp, String diagnostico, String receta, String notas) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_historial_card, containerHistorial, false);
        
        TextView tvFechaHora = card.findViewById(R.id.tvFechaHora);
        TextView tvDoctorEsp = card.findViewById(R.id.tvDoctorEsp);
        TextView tvDiagnostico = card.findViewById(R.id.tvDiagnostico);
        TextView tvReceta = card.findViewById(R.id.tvReceta);

        // Limpiar el texto de fecha/hora si viene null
        if (fechaHora != null) {
            fechaHora = fechaHora.replace(" • null", "").replace("null • ", "");
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
        }

        tvFechaHora.setText(fechaHora != null ? fechaHora : "Fecha no disponible");
        tvDoctorEsp.setText(doctorEsp != null ? doctorEsp : "Detalles no disponibles");
        
        tvDiagnostico.setText((diagnostico != null && !diagnostico.isEmpty()) ? diagnostico : "Sin diagnóstico registrado.");
        
        String textoReceta = (receta != null && !receta.isEmpty()) ? receta : "Sin receta registrada.";
        if (notas != null && !notas.isEmpty()) {
            textoReceta += "\n\nNotas: " + notas;
        }
        tvReceta.setText(textoReceta);

        containerHistorial.addView(card);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


