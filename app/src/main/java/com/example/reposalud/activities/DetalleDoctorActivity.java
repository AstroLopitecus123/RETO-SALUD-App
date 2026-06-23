package com.example.reposalud.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.reposalud.R;
import com.example.reposalud.database.CitaDAO;
import java.util.Locale;

public class DetalleDoctorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_doctor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_detalle_doctor), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            toolbar.setPadding(0, systemBars.top, 0, 0);
            int actionBarHeight = 0;
            android.util.TypedValue tv = new android.util.TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = android.util.TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }
            toolbar.getLayoutParams().height = actionBarHeight + systemBars.top;
            return insets;
        });

        int doctorId = getIntent().getIntExtra("doctor_id", -1);
        String doctorNombre = getIntent().getStringExtra("doctor_nombre");

        CitaDAO citaDAO = new CitaDAO(this);
        String especialidadDb = "Especialista";
        String biografia = "";
        float rating = 0;
        int experiencia = 0;
        int reviews = 0;
        String imagenDb = "logo_solo";

        Cursor cursor = citaDAO.obtenerDoctorPorId(doctorId);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                especialidadDb = cursor.getString(cursor.getColumnIndexOrThrow("especialidad_nombre"));
                biografia = cursor.getString(cursor.getColumnIndexOrThrow("biografia"));
                rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
                experiencia = cursor.getInt(cursor.getColumnIndexOrThrow("experiencia"));
                reviews = cursor.getInt(cursor.getColumnIndexOrThrow("reviews"));

                try {
                    String img = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                    if (img != null && !img.isEmpty()) {
                        imagenDb = img;
                    }
                } catch (Exception ignored) {
                }
            }
            cursor.close();
        }

        TextView tvName = findViewById(R.id.tvDoctorNameDetail);
        TextView tvSpecialty = findViewById(R.id.tvDoctorSpecialtyDetail);
        TextView tvAbout = findViewById(R.id.tvDoctorAbout);
        TextView tvRating = findViewById(R.id.tvDoctorRating);
        TextView tvExp = findViewById(R.id.tvDoctorExp);
        TextView tvReviews = findViewById(R.id.tvDoctorReviews);
        ImageView ivDoctor = findViewById(R.id.ivDoctorImage);

        tvName.setText(doctorNombre);
        tvSpecialty.setText(especialidadDb);
        tvAbout.setText(biografia);
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating));
        tvExp.setText(String.format(Locale.getDefault(), "+%d", experiencia));
        tvReviews.setText(String.valueOf(reviews));

        // Cargar imagen del doctor
        if (imagenDb != null && imagenDb.startsWith("http")) {
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


        findViewById(R.id.btnAgendarCitaConDoctor).setOnClickListener(v -> {
            Intent intent = new Intent(this, AgendarCitaActivity.class);
            intent.putExtra("doctor_id", doctorId);
            intent.putExtra("doctor_nombre", doctorNombre);
            startActivity(intent);
        });

        android.widget.RatingBar ratingBar = findViewById(R.id.ratingBarDoctor);
        findViewById(R.id.btnSubmitReview).setOnClickListener(v -> {
            float userRating = ratingBar.getRating();
            if (userRating > 0) {
                citaDAO.actualizarRatingDoctor(doctorId, userRating);
                android.widget.Toast.makeText(this, "¡Gracias por tu reseña!", android.widget.Toast.LENGTH_SHORT).show();
                ratingBar.setRating(0);
                finish();
                startActivity(getIntent());
            } else {
                android.widget.Toast.makeText(this, "Por favor selecciona una calificación", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
