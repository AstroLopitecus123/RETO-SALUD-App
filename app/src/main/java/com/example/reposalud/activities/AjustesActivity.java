package com.example.reposalud.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.reposalud.R;

public class AjustesActivity extends BaseActivity {

    private Switch switchNotificaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        switchNotificaciones = findViewById(R.id.switchNotificaciones);
        Switch switchMantenerSesion = findViewById(R.id.switchMantenerSesion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajustes");
        }
        toolbar.setNavigationOnClickListener(v -> finish());



        SharedPreferences prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE);
        boolean isNotifEnabled = prefs.getBoolean("notificaciones", true);
        boolean isKeepSession = prefs.getBoolean("mantener_sesion", false);

        switchNotificaciones.setChecked(isNotifEnabled);
        switchMantenerSesion.setChecked(isKeepSession);

        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notificaciones", isChecked).apply();
            String msg = isChecked ? "Notificaciones activadas" : "Notificaciones desactivadas";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        switchMantenerSesion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("mantener_sesion", isChecked).apply();
            String msg = isChecked ? "Sesión se mantendrá iniciada" : "Solicitaremos login al volver a entrar";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


