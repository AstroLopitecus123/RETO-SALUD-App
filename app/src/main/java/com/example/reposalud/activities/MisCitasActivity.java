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

public class MisCitasActivity extends BaseActivity {

    private RecyclerView rvMisCitas;
    private CitasAdapter citasAdapter;
    private LinearLayout layoutVacio;
    private ProgressBar progressBarCitas;
    private int usuarioId;
    private List<ApiService.CitaResponse> allCitas = new ArrayList<>();

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
                    
                    // Guardar en caché offline
                    SharedPreferences prefs = getSharedPreferences("offline_cache", MODE_PRIVATE);
                    prefs.edit().putString("mis_citas_" + usuarioId, new com.google.gson.Gson().toJson(citasList)).apply();

                    if (citasList.isEmpty()) {
                        layoutVacio.setVisibility(View.VISIBLE);
                    } else {
                        rvMisCitas.setVisibility(View.VISIBLE);
                        allCitas = citasList;
                        setupFilters();
                        citasAdapter.actualizarLista(citasList);
                    }
                } else {
                    cargarCitasOffline();
                }
            }

            @Override
            public void onFailure(Call<List<ApiService.CitaResponse>> call, Throwable t) {
                progressBarCitas.setVisibility(View.GONE);
                cargarCitasOffline();
            }
        });
    }

    private void cargarCitasOffline() {
        SharedPreferences prefs = getSharedPreferences("offline_cache", MODE_PRIVATE);
        String json = prefs.getString("mis_citas_" + usuarioId, "");
        if (!json.isEmpty()) {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<ApiService.CitaResponse>>() {}.getType();
            List<ApiService.CitaResponse> citasList = new com.google.gson.Gson().fromJson(json, type);
            if (citasList != null && !citasList.isEmpty()) {
                layoutVacio.setVisibility(View.GONE);
                rvMisCitas.setVisibility(View.VISIBLE);
                allCitas = citasList;
                setupFilters();
                citasAdapter.actualizarLista(citasList);
                Toast.makeText(this, "Mostrando versión offline", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        layoutVacio.setVisibility(View.VISIBLE);
        mostrarError("Sin conexión. No se encontraron citas offline.");
    }

    private void setupFilters() {
        android.widget.HorizontalScrollView filterScrollView = findViewById(R.id.filterScrollView);
        LinearLayout filterContainer = findViewById(R.id.filterContainer);
        if (filterScrollView == null || filterContainer == null) return;
        filterContainer.removeAllViews();
        filterScrollView.setVisibility(View.VISIBLE);

        List<String> states = new ArrayList<>();
        states.add("Todas");
        
        boolean hasConfirmada = false;
        boolean hasPendiente = false;
        List<String> remainingStates = new ArrayList<>();
        
        for (ApiService.CitaResponse c : allCitas) {
            if (c.estado != null) {
                if (c.estado.equals("CONFIRMADA")) {
                    hasConfirmada = true;
                } else if (c.estado.equals("PENDIENTE")) {
                    hasPendiente = true;
                } else if (!remainingStates.contains(c.estado)) {
                    remainingStates.add(c.estado);
                }
            }
        }
        
        if (hasConfirmada) states.add("CONFIRMADA");
        if (hasPendiente) states.add("PENDIENTE");
        states.addAll(remainingStates);

        float density = getResources().getDisplayMetrics().density;
        for (String state : states) {
            android.widget.TextView tv = new android.widget.TextView(this);
            
            String formattedName = "Todas";
            if (!state.equals("Todas")) {
                formattedName = state.replace("_", " ").toLowerCase();
                if (formattedName.equals("pendiente")) formattedName = "Pendientes";
                else if (formattedName.equals("confirmada")) formattedName = "Confirmadas";
                else if (formattedName.equals("cancelada")) formattedName = "Canceladas";
                else if (formattedName.equals("completada")) formattedName = "Completadas";
                else {
                    String[] words = formattedName.split(" ");
                    StringBuilder sb = new StringBuilder();
                    for (String w : words) {
                        if (w.length() > 0) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                    }
                    formattedName = sb.toString().trim();
                }
            }
            tv.setText(formattedName);
            tv.setTag(state);
            tv.setPadding((int)(16 * density), (int)(8 * density), (int)(16 * density), (int)(8 * density));
            tv.setTextSize(14);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, (int)(8 * density), 0);
            tv.setLayoutParams(lp);

            if (state.equals("Todas")) {
                tv.setBackgroundResource(R.drawable.bg_pill_selected);
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));
            } else {
                tv.setBackgroundResource(R.drawable.bg_pill_unselected);
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_slate));
            }

            tv.setOnClickListener(v -> {
                for (int i = 0; i < filterContainer.getChildCount(); i++) {
                    android.widget.TextView child = (android.widget.TextView) filterContainer.getChildAt(i);
                    child.setBackgroundResource(R.drawable.bg_pill_unselected);
                    child.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.dark_slate));
                }
                tv.setBackgroundResource(R.drawable.bg_pill_selected);
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));
                
                List<ApiService.CitaResponse> filtered = new ArrayList<>();
                if (state.equals("Todas")) {
                    filtered.addAll(allCitas);
                } else {
                    for (ApiService.CitaResponse c : allCitas) {
                        if (state.equals(c.estado)) {
                            filtered.add(c);
                        }
                    }
                }
                citasAdapter.actualizarLista(filtered);
                
                if (filtered.isEmpty()) {
                    layoutVacio.setVisibility(View.VISIBLE);
                    rvMisCitas.setVisibility(View.GONE);
                } else {
                    layoutVacio.setVisibility(View.GONE);
                    rvMisCitas.setVisibility(View.VISIBLE);
                }
            });

            filterContainer.addView(tv);
        }
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


