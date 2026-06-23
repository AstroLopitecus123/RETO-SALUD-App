package com.example.reposalud.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reposalud.R;
import com.example.reposalud.adapters.CitasAdapter;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisCitasActivity extends AppCompatActivity {

    private RecyclerView rvMisCitas;
    private CitasAdapter citasAdapter;
    private LinearLayout layoutVacio;
    private ProgressBar progressBarCitas;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        rvMisCitas = findViewById(R.id.rvMisCitas);
        layoutVacio = findViewById(R.id.layoutVacio);
        progressBarCitas = findViewById(R.id.progressBarCitas);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarMisCitas);

        toolbar.setNavigationOnClickListener(v -> finish());

        rvMisCitas.setLayoutManager(new LinearLayoutManager(this));
        citasAdapter = new CitasAdapter(this, new ArrayList<>());
        rvMisCitas.setAdapter(citasAdapter);

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        usuarioId = prefs.getInt("id_usuario", 1);

        cargarCitas();
    }

    private void cargarCitas() {
        progressBarCitas.setVisibility(View.VISIBLE);
        rvMisCitas.setVisibility(View.GONE);
        layoutVacio.setVisibility(View.GONE);

        String token = getSharedPreferences("user_session", MODE_PRIVATE).getString("api_token", "");

        RetrofitClient.getApiService().obtenerCitas("Bearer " + token, usuarioId).enqueue(new Callback<List<ApiService.CitaResponse>>() {
            @Override
            public void onResponse(Call<List<ApiService.CitaResponse>> call, Response<List<ApiService.CitaResponse>> response) {
                progressBarCitas.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiService.CitaResponse> citasList = response.body();
                    if (citasList.isEmpty()) {
                        layoutVacio.setVisibility(View.VISIBLE);
                    } else {
                        rvMisCitas.setVisibility(View.VISIBLE);
                        citasAdapter.actualizarLista(citasList);
                    }
                } else {
                    mostrarError("No se pudieron cargar las citas. Intente más tarde.");
                    layoutVacio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.CitaResponse>> call, Throwable t) {
                progressBarCitas.setVisibility(View.GONE);
                mostrarError("Error de conexión. Revise su internet.");
                layoutVacio.setVisibility(View.VISIBLE);
            }
        });
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
