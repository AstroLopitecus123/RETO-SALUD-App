package com.example.reposalud.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.reposalud.R;
import com.example.reposalud.database.CitaDAO;
import java.util.Locale;

public class DetalleEspecialidadActivity extends BaseActivity {

    private int especialidadId;
    private String especialidadNombre;
    private CitaDAO citaDAO;
    private LinearLayout containerDoctores;
    private LinearLayout containerServicios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_especialidad);

        citaDAO = new CitaDAO(this);
        especialidadId = getIntent().getIntExtra("especialidad_id", -1);
        especialidadNombre = getIntent().getStringExtra("especialidad_nombre");

        setupUI();
        cargarDoctores();
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvTitulo = findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcion = findViewById(R.id.tvDescripcionDetalle);
        ImageView ivHeader = findViewById(R.id.ivEspecialidadHeader);
        containerDoctores = findViewById(R.id.containerDoctoresDetalle);
        containerServicios = findViewById(R.id.containerServicios);

        tvTitulo.setText(especialidadNombre);

        // Configuración según especialidad
        String[] servicios;
        if (especialidadNombre != null) {
            if (especialidadNombre.contains("Pediatría")) {
                tvDescripcion.setText(R.string.desc_pediatria);
                ivHeader.setImageResource(R.drawable.pediatra);
                servicios = new String[]{"Control Niño Sano", "Vacunas", "Emergencias"};
            } else if (especialidadNombre.contains("Cardiología")) {
                tvDescripcion.setText(R.string.desc_cardiologia);
                ivHeader.setImageResource(R.drawable.cardiologia);
                servicios = new String[]{"Electrocardiograma", "Ecocardiograma", "Presión Arterial"};
            } else if (especialidadNombre.contains("Medicina General")) {
                tvDescripcion.setText(R.string.desc_medicina_general);
                ivHeader.setImageResource(R.drawable.medicina_general);
                servicios = new String[]{"Chequeo Anual", "Recetas", "Certificados"};
            } else if (especialidadNombre.contains("Ginecología")) {
                tvDescripcion.setText(R.string.desc_ginecologia);
                ivHeader.setImageResource(R.drawable.ginecologia);
                servicios = new String[]{"Control Prenatal", "Papanicolaou", "Ecografías"};
            } else if (especialidadNombre.contains("Nutrición")) {
                tvDescripcion.setText(R.string.desc_nutricion);
                ivHeader.setImageResource(R.drawable.nutricion);
                servicios = new String[]{"Plan de Dieta", "Control de Peso", "Bioimpedancia"};
            } else if (especialidadNombre.contains("Psicología")) {
                tvDescripcion.setText(R.string.desc_psicologia);
                ivHeader.setImageResource(R.drawable.psicologia);
                servicios = new String[]{"Terapia Individual", "Terapia de Pareja", "Orientación"};
            } else {
                tvDescripcion.setText("Especialistas altamente calificados para tu bienestar integral.");
                servicios = new String[]{"Consulta", "Seguimiento"};
            }
            configurarServicios(servicios);
        }

        findViewById(R.id.btnAgendarAhora).setOnClickListener(v -> {
            Intent intent = new Intent(this, AgendarCitaActivity.class);
            intent.putExtra("especialidad_id", especialidadId);
            intent.putExtra("especialidad_nombre", especialidadNombre);
            startActivity(intent);
        });
    }

    private void configurarServicios(String[] servicios) {
        containerServicios.removeAllViews();
        for (String servicio : servicios) {
            TextView tvPill = new TextView(this);
            tvPill.setText(servicio);
            tvPill.setTextColor(getResources().getColor(R.color.primary_green));
            tvPill.setBackgroundResource(R.drawable.bg_pill_unselected);
            tvPill.setPadding(32, 16, 32, 16);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            tvPill.setLayoutParams(params);
            
            containerServicios.addView(tvPill);
        }
    }

    private void cargarDoctores() {
        containerDoctores.removeAllViews();
        Cursor cursor = citaDAO.obtenerDoctoresPorEspecialidad(especialidadId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                String imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));

                View card = LayoutInflater.from(this).inflate(R.layout.item_doctor_card, containerDoctores, false);
                TextView tvName = card.findViewById(R.id.tvDoctorName);
                ImageView ivDoctor = card.findViewById(R.id.ivDoctorItem);

                tvName.setText(nombre);

                if (imagen != null && !imagen.isEmpty()) {
                    if (imagen.startsWith("http")) {
                        com.bumptech.glide.Glide.with(this)
                            .load(imagen)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.logo_solo)
                            .error(R.drawable.logo_solo)
                            .into(ivDoctor);
                    } else {
                        int resId = getResources().getIdentifier(imagen, "drawable", getPackageName());
                        if (resId != 0) {
                            ivDoctor.setImageResource(resId);
                        } else {
                            ivDoctor.setImageResource(R.drawable.logo_solo);
                        }
                    }
                } else {
                    ivDoctor.setImageResource(R.drawable.logo_solo);
                }

                try {
                    float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
                    int exp = cursor.getInt(cursor.getColumnIndexOrThrow("experiencia"));

                    TextView tvEsp = card.findViewById(R.id.tvDoctorSpecialty);
                    if (tvEsp != null) tvEsp.setText(especialidadNombre);

                    TextView tvRating = card.findViewById(R.id.tvDoctorRatingItem);
                    if (tvRating != null) tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating));

                    TextView tvExp = card.findViewById(R.id.tvDoctorExpItem);
                    if (tvExp != null) tvExp.setText(String.format(Locale.getDefault(), "+%d años exp.", exp));
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
}

