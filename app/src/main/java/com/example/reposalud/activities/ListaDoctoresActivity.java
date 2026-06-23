package com.example.reposalud.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.reposalud.R;
import com.example.reposalud.database.CitaDAO;
import java.util.Locale;

public class ListaDoctoresActivity extends AppCompatActivity {

    private CitaDAO citaDAO;
    private LinearLayout containerDoctores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lista_doctores); // <--- HELIAN CAMBIÓ AQUI

        com.example.reposalud.utils.NavigationHelper.setupBottomNavigation(this);
        citaDAO = new CitaDAO(this);

        //-----HELIAN CAMBIÓ ESTO
        containerDoctores = findViewById(R.id.containerDoctores);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nuestros Especialistas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());


        cargarTodosLosDoctores();
        obtenerMedicosDeLaWeb();
        //
    }

    private void cargarTodosLosDoctores() {
        containerDoctores.removeAllViews();
        Cursor cursor = citaDAO.obtenerTodosLosDoctores();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                
                View card = LayoutInflater.from(this).inflate(R.layout.item_doctor_card, containerDoctores, false);
                TextView tvName = card.findViewById(R.id.tvDoctorName);
                tvName.setText(nombre);
                
                try {
                    String espName = cursor.getString(cursor.getColumnIndexOrThrow("especialidad_nombre"));
                    float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
                    int exp = cursor.getInt(cursor.getColumnIndexOrThrow("experiencia"));

                    TextView tvEsp = card.findViewById(R.id.tvDoctorSpecialty);
                    if (tvEsp != null) tvEsp.setText(espName);

                    TextView tvRating = card.findViewById(R.id.tvDoctorRatingItem);
                    if (tvRating != null) tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating));

                    TextView tvExp = card.findViewById(R.id.tvDoctorExpItem);
                    if (tvExp != null) tvExp.setText(String.format(Locale.getDefault(), "+%d años exp.", exp));

                    android.widget.ImageView ivDoctor = card.findViewById(R.id.ivDoctorItem);
                    String imagenDb = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                    if (imagenDb == null || imagenDb.isEmpty()) {
                        ivDoctor.setImageResource(R.drawable.logo_solo);
                    } else if (imagenDb.startsWith("http")) {
                        com.bumptech.glide.Glide.with(this)
                            .load(imagenDb)
                            .placeholder(R.drawable.logo_solo)
                            .error(R.drawable.logo_solo)
                            .into(ivDoctor);
                    } else {
                        int resId = getResources().getIdentifier(imagenDb, "drawable", getPackageName());
                        if (resId != 0) {
                            ivDoctor.setImageResource(resId);
                        } else {
                            ivDoctor.setImageResource(R.drawable.logo_solo);
                        }
                    }

                } catch (Exception ignored) {}

                card.setOnClickListener(v -> {
                    Intent intent = new Intent(this, DetalleDoctorActivity.class);
                    intent.putExtra("doctor_id", id);
                    intent.putExtra("doctor_nombre", nombre);
                    startActivity(intent);
                });

                containerDoctores.addView(card);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void obtenerMedicosDeLaWeb() {
        com.example.reposalud.network.RetrofitClient.getApiService().obtenerMedicos().enqueue(new retrofit2.Callback<java.util.List<com.example.reposalud.network.ApiService.MedicoResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.reposalud.network.ApiService.MedicoResponse>> call, retrofit2.Response<java.util.List<com.example.reposalud.network.ApiService.MedicoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean nuevosInsertados = false;
                    for (com.example.reposalud.network.ApiService.MedicoResponse m : response.body()) {
                        String prefijo = "Dr. "; // O Dra., pero mantendremos Dr. genérico
                        String nombreCompleto = prefijo + m.usuario.nombre + " " + m.usuario.apellido;
                        
                        if (!citaDAO.existeDoctorPorNombre(nombreCompleto)) {
                            int espId = m.especialidad != null ? citaDAO.obtenerIdEspecialidad(m.especialidad.nombre) : 1;
                            String imagenA_Guardar = (m.usuario.fotoUrl != null && !m.usuario.fotoUrl.isEmpty()) ? m.usuario.fotoUrl : "logo_solo";
                            citaDAO.insertarDoctor(nombreCompleto, espId, imagenA_Guardar, m.id);
                            nuevosInsertados = true;
                        } else {
                            String imagenNueva = (m.usuario.fotoUrl != null && !m.usuario.fotoUrl.isEmpty()) ? m.usuario.fotoUrl : "logo_solo";
                            int espId = m.especialidad != null ? citaDAO.obtenerIdEspecialidad(m.especialidad.nombre) : 1;
                            citaDAO.actualizarDatosDoctorSync(nombreCompleto, imagenNueva, m.id, espId);
                            nuevosInsertados = true;
                        }
                    }
                    if (nuevosInsertados) {
                        cargarTodosLosDoctores();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.reposalud.network.ApiService.MedicoResponse>> call, Throwable t) {
                // Fallo de red: los doctores locales ya se mostraron
            }
        });
    }
}
