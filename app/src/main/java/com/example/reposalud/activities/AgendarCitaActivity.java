package com.example.reposalud.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.reposalud.R;
import com.example.reposalud.database.CitaDAO;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;
import com.example.reposalud.utils.NavigationHelper;
import androidx.core.widget.TextViewCompat;
import android.content.res.ColorStateList;

import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.database.Cursor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AgendarCitaActivity extends BaseActivity {

    // la parte del cap
    private CitaDAO citaDAO;
    
    private String selectedEspecialidad = "";
    private int selectedDoctorId = -1;
    private String selectedDoctorName = "";
    private String selectedFecha = "";
    private String selectedHora = "";

    private View sectionProfessional, sectionDateTime, timeSlotsContainer, sectionSummary;
    private LinearLayout doctorsContainer;
    private final Map<Integer, TextView> specialtyPills = new HashMap<>();
    private Map<String, java.util.List<ApiService.DisponibilidadResponse>> disponibilidadesMap = new HashMap<>();

    // Campos para el detalle dinámico
    private TextView tvEspecialidadDesc;
    private LinearLayout containerServicios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_cita);

        citaDAO = new CitaDAO(this);

        setupUI();
        NavigationHelper.setupBottomNavigation(this);

        // Verificar si viene una especialidad pre-seleccionada o un doctor
        int especialidadId = getIntent().getIntExtra("especialidad_id", -1);
        int doctorIdExtra = getIntent().getIntExtra("doctor_id", -1);
        String doctorNombreExtra = getIntent().getStringExtra("doctor_nombre");

        if (especialidadId != -1) {
            String especialidadNombre = getIntent().getStringExtra("especialidad_nombre");
            if (especialidadNombre != null) {
                selectEspecialidad(especialidadId, especialidadNombre);
            }
        } else if (doctorIdExtra != -1) {
            preseleccionarDoctor(doctorIdExtra, doctorNombreExtra);
        }

        obtenerMedicosDeLaWeb();
    }

    private void preseleccionarDoctor(int doctorId, String doctorNombre) {
        // Buscar la especialidad de este doctor para cargar el contexto
        Cursor cursor = citaDAO.obtenerDoctorPorId(doctorId);
        if (cursor != null && cursor.moveToFirst()) {
            int espId = cursor.getInt(cursor.getColumnIndexOrThrow("especialidad_id"));
            String espNombre = cursor.getString(cursor.getColumnIndexOrThrow("especialidad_nombre"));
            selectEspecialidad(espId, espNombre);
            
            selectedDoctorId = doctorId;
            selectedDoctorName = doctorNombre;
            sectionDateTime.setVisibility(View.VISIBLE);
            updateSummary();
            cursor.close();
        }
    }

    private void obtenerMedicosDeLaWeb() {
        RetrofitClient.getApiService().obtenerMedicos().enqueue(new retrofit2.Callback<java.util.List<ApiService.MedicoResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<ApiService.MedicoResponse>> call, retrofit2.Response<java.util.List<ApiService.MedicoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    citaDAO.limpiarDoctores();
                    boolean nuevosInsertados = false;
                    for (ApiService.MedicoResponse m : response.body()) {
                        String prefijo = "Dr. ";
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
                    // Si hay nuevos doctores y ya había una especialidad seleccionada, recargar la lista
                    if (nuevosInsertados && !selectedEspecialidad.isEmpty()) {
                        for (Map.Entry<Integer, TextView> entry : specialtyPills.entrySet()) {
                            if (entry.getValue().getText().toString().equals(selectedEspecialidad)) {
                                loadDoctores(entry.getKey());
                                break;
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<ApiService.MedicoResponse>> call, Throwable t) {}
        });
    }

    private void setupUI() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        sectionProfessional = findViewById(R.id.sectionProfessional);
        sectionDateTime = findViewById(R.id.sectionDateTime);
        timeSlotsContainer = findViewById(R.id.timeSlotsContainer);
        sectionSummary = findViewById(R.id.sectionSummary);
        doctorsContainer = findViewById(R.id.doctorsContainer);
        
        // Inicializar contenedores de detalle
        tvEspecialidadDesc = findViewById(R.id.tvEspecialidadDesc);
        containerServicios = findViewById(R.id.containerServiciosEspecialidad);

        TextView tvVerTodas = findViewById(R.id.tvVerTodasEspecialidades);
        if (tvVerTodas != null) {
            tvVerTodas.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, EspecialidadesActivity.class);
                startActivity(intent);
            });
        }

        // Especialidades logic
        setupSpecialtyPills();

        // Date Selection
        int[] dayIds = {R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu, R.id.dayFri, R.id.daySat};
        for (int id : dayIds) {
            TextView dayTv = findViewById(id);
            dayTv.setOnClickListener(v -> {
                if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                    Toast.makeText(this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Reset visual state for all selectable days
                for (int otherId : dayIds) {
                    TextView otherTv = findViewById(otherId);
                    otherTv.setBackgroundResource(R.drawable.bg_pill_unselected);
                    otherTv.setTextColor(ContextCompat.getColor(this, R.color.dark_slate));
                }
                dayTv.setBackgroundResource(R.drawable.bg_pill_selected);
                dayTv.setTextColor(ContextCompat.getColor(this, R.color.white));

                String rawText = dayTv.getText().toString();
                String dayName = "";
                if (rawText.startsWith("L")) dayName = getString(R.string.day_monday);
                else if (rawText.startsWith("M")) dayName = getString(R.string.day_tuesday);
                else if (rawText.startsWith("X")) dayName = getString(R.string.day_wednesday);
                else if (rawText.startsWith("J")) dayName = getString(R.string.day_thursday);
                else if (rawText.startsWith("V")) dayName = getString(R.string.day_friday);
                else if (rawText.startsWith("S")) dayName = getString(R.string.day_saturday);

                String dayNum = rawText.substring(rawText.indexOf("\n") + 1);
                selectedFecha = getString(R.string.date_format_october, dayName, dayNum);

                timeSlotsContainer.setVisibility(View.VISIBLE);
                updateSummary();
            });
        }

        setupTimeSlots();

        // Botón Confirmar
        findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedEspecialidad.isEmpty() || selectedDoctorName.isEmpty() || selectedFecha.isEmpty() || selectedHora.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_incomplete_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            android.content.Intent intent = new android.content.Intent(this, PagoActivity.class);
            intent.putExtra("especialidad", selectedEspecialidad);
            intent.putExtra("doctorName", selectedDoctorName);
            intent.putExtra("doctorId", selectedDoctorId);
            intent.putExtra("doctorBackendId", selectedDoctorBackendId);
            intent.putExtra("fecha", selectedFecha);
            intent.putExtra("hora", selectedHora);
            intent.putExtra("precio", getString(R.string.consultation_price));
            startActivityForResult(intent, 1001);
        });
        
        updateSummary();
    }

    private void setupSpecialtyPills() {
        specialtyPills.put(1, findViewById(R.id.pillCardio));
        specialtyPills.put(2, findViewById(R.id.pillPediatria));
        specialtyPills.put(3, findViewById(R.id.pillMedicina));
        specialtyPills.put(4, findViewById(R.id.pillGineco));
        specialtyPills.put(5, findViewById(R.id.pillNutricion));
        specialtyPills.put(6, findViewById(R.id.pillPsicologia));

        for (Map.Entry<Integer, TextView> entry : specialtyPills.entrySet()) {
            entry.getValue().setOnClickListener(v -> {
                if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                    Toast.makeText(this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                    return;
                }
                int id = entry.getKey();
                String name = entry.getValue().getText().toString();
                selectEspecialidad(id, name);
            });
        }
    }

    private void selectEspecialidad(int id, String name) {
        selectedEspecialidad = name;

        for (TextView pill : specialtyPills.values()) {
            pill.setBackgroundResource(R.drawable.bg_pill_unselected);
            pill.setTextColor(ContextCompat.getColor(this, R.color.light_text));
            TextViewCompat.setCompoundDrawableTintList(pill, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.light_text)));
        }

        TextView selected = specialtyPills.get(id);
        if (selected != null) {
            selected.setBackgroundResource(R.drawable.bg_pill_selected);
            selected.setTextColor(ContextCompat.getColor(this, R.color.white));
            androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(selected, android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            
            findViewById(R.id.placeholderEmpty).setVisibility(View.GONE);
        }

        // Mostrar detalle dinámico
        actualizarDetalleEspecialidad(name);

        loadDoctores(id);
        sectionProfessional.setVisibility(View.VISIBLE);
        updateSummary();
    }

    private void actualizarDetalleEspecialidad(String name) {
        findViewById(R.id.sectionEspecialidadDetalle).setVisibility(View.VISIBLE);
        String[] servicios;
        
        if (name.contains("Pediatría")) {
            tvEspecialidadDesc.setText(R.string.desc_pediatria);
            servicios = new String[]{"Control Niño Sano", "Vacunas", "Emergencias"};
        } else if (name.contains("Cardiología")) {
            tvEspecialidadDesc.setText(R.string.desc_cardiologia);
            servicios = new String[]{"Electrocardiograma", "Ecocardiograma", "Presión Arterial"};
        } else if (name.contains("Medicina General")) {
            tvEspecialidadDesc.setText(R.string.desc_medicina_general);
            servicios = new String[]{"Chequeo Anual", "Recetas", "Certificados"};
        } else if (name.contains("Ginecología")) {
            tvEspecialidadDesc.setText(R.string.desc_ginecologia);
            servicios = new String[]{"Control Prenatal", "Papanicolaou", "Ecografías"};
        } else if (name.contains("Nutrición")) {
            tvEspecialidadDesc.setText(R.string.desc_nutricion);
            servicios = new String[]{"Plan de Dieta", "Control de Peso", "Bioimpedancia"};
        } else if (name.contains("Psicología")) {
            tvEspecialidadDesc.setText(R.string.desc_psicologia);
            servicios = new String[]{"Terapia Individual", "Terapia de Pareja", "Orientación"};
        } else {
            tvEspecialidadDesc.setText("Especialistas altamente calificados.");
            servicios = new String[]{"Consulta", "Seguimiento"};
        }

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

    private int selectedDoctorBackendId = -1;

    private void loadDoctores(int especialidadId) {
        doctorsContainer.removeAllViews();
        Cursor cursor = citaDAO.obtenerDoctoresPorEspecialidad(especialidadId);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
                
                View card = LayoutInflater.from(this).inflate(R.layout.item_doctor_card, doctorsContainer, false);
                TextView tvName = card.findViewById(R.id.tvDoctorName);
                tvName.setText(nombre);

                try {
                    float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
                    int exp = cursor.getInt(cursor.getColumnIndexOrThrow("experiencia"));

                    TextView tvRating = card.findViewById(R.id.tvDoctorRatingItem);
                    if (tvRating != null) tvRating.setText(String.format(Locale.getDefault(), "%.1f", rating));

                    TextView tvExp = card.findViewById(R.id.tvDoctorExpItem);
                    if (tvExp != null) tvExp.setText(String.format(Locale.getDefault(), "+%d años exp.", exp));
                    
                    TextView tvEsp = card.findViewById(R.id.tvDoctorSpecialty);
                    if (tvEsp != null) tvEsp.setText(selectedEspecialidad);

                    android.widget.ImageView ivDoctor = card.findViewById(R.id.ivDoctorItem);
                    String imagenDb = cursor.getString(cursor.getColumnIndexOrThrow("imagen"));
                    if (imagenDb != null && imagenDb.startsWith("http")) {
                        com.bumptech.glide.Glide.with(AgendarCitaActivity.this).load(imagenDb).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL).into(ivDoctor);
                    } else {
                        if (imagenDb == null || imagenDb.isEmpty()) imagenDb = "logo_solo";
                        int resId = getResources().getIdentifier(imagenDb, "drawable", getPackageName());
                        if (resId != 0) {
                            ivDoctor.setImageResource(resId);
                        } else {
                            ivDoctor.setImageResource(R.drawable.logo_solo);
                        }
                    }
                } catch (Exception ignored) {}
                
                int backendId = -1;
                try { backendId = cursor.getInt(cursor.getColumnIndexOrThrow("backend_id")); } catch (Exception e) {}
                final int finalBackendId = backendId;
                
                card.setOnClickListener(v -> {
                    selectedDoctorId = id;
                    selectedDoctorName = nombre;
                    selectedDoctorBackendId = finalBackendId;
                    sectionDateTime.setVisibility(View.VISIBLE);
                    updateSummary();
                    v.post(() -> sectionDateTime.getParent().requestChildFocus(sectionDateTime, sectionDateTime));
                    cargarDisponibilidadesDinamicas(finalBackendId);
                });
                
                doctorsContainer.addView(card);
            } while (cursor.moveToNext());
            cursor.close();
        } else {

            TextView tvEmpty = new TextView(this);
            tvEmpty.setText(getString(R.string.no_doctors_available));
            tvEmpty.setPadding(20, 40, 20, 40);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.light_text));
            doctorsContainer.addView(tvEmpty);
        }
    }

    private void cargarDisponibilidadesDinamicas(int backendId) {
        LinearLayout datesContainerStatic = findViewById(R.id.datesContainerStatic);
        LinearLayout datesContainerDynamic = findViewById(R.id.datesContainerDynamic);
        android.widget.GridLayout timesGridStatic = findViewById(R.id.timesGridStatic);
        android.widget.GridLayout timesGridDynamic = findViewById(R.id.timesGridDynamic);
        View calendarHeader = ((View) findViewById(R.id.tvMonth).getParent());

        if (backendId == -1) {
            calendarHeader.setVisibility(View.VISIBLE);
            datesContainerStatic.setVisibility(View.VISIBLE);
            datesContainerDynamic.setVisibility(View.GONE);
            timesGridStatic.setVisibility(View.VISIBLE);
            timesGridDynamic.setVisibility(View.GONE);
            return;
        }

        calendarHeader.setVisibility(View.GONE);
        datesContainerStatic.setVisibility(View.GONE);
        datesContainerDynamic.setVisibility(View.VISIBLE);
        timesGridStatic.setVisibility(View.GONE);
        timesGridDynamic.setVisibility(View.VISIBLE);

        datesContainerDynamic.removeAllViews();
        timesGridDynamic.removeAllViews();
        
        TextView tvCargando = new TextView(this);
        tvCargando.setText("Cargando horarios...");
        tvCargando.setTextColor(ContextCompat.getColor(this, R.color.light_text));
        datesContainerDynamic.addView(tvCargando);

        String token = getSharedPreferences("user_session", MODE_PRIVATE).getString("api_token", "");

        RetrofitClient.getApiService().obtenerDisponibilidades("Bearer " + token, backendId).enqueue(new retrofit2.Callback<java.util.List<ApiService.DisponibilidadResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<ApiService.DisponibilidadResponse>> call, retrofit2.Response<java.util.List<ApiService.DisponibilidadResponse>> response) {
                datesContainerDynamic.removeAllViews();
                if (response.isSuccessful() && response.body() != null) {
                    disponibilidadesMap = new java.util.TreeMap<>();
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    for (int i = 0; i < 14; i++) {
                        String dateStr = sdf.format(cal.getTime());
                        disponibilidadesMap.put(dateStr, new java.util.ArrayList<>());
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
                    }
                    for (ApiService.DisponibilidadResponse d : response.body()) {
                        if ("DISPONIBLE".equals(d.estado) && disponibilidadesMap.containsKey(d.fecha)) {
                            disponibilidadesMap.get(d.fecha).add(d);
                        }
                    }

                    android.widget.HorizontalScrollView hsv = new android.widget.HorizontalScrollView(AgendarCitaActivity.this);
                    hsv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    hsv.setHorizontalScrollBarEnabled(false);
                    
                    LinearLayout innerContainer = new LinearLayout(AgendarCitaActivity.this);
                    innerContainer.setOrientation(LinearLayout.HORIZONTAL);
                    innerContainer.setLayoutParams(new android.widget.HorizontalScrollView.LayoutParams(android.widget.HorizontalScrollView.LayoutParams.WRAP_CONTENT, android.widget.HorizontalScrollView.LayoutParams.WRAP_CONTENT));
                    hsv.addView(innerContainer);
                    datesContainerDynamic.addView(hsv);

                    float density = getResources().getDisplayMetrics().density;

                    for (String fechaStr : disponibilidadesMap.keySet()) {
                        TextView dayTv = new TextView(AgendarCitaActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            (int)(56 * density), 
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins((int)(6 * density), 0, (int)(6 * density), 0);
                        dayTv.setLayoutParams(params);
                        dayTv.setGravity(android.view.Gravity.CENTER);
                        dayTv.setBackgroundResource(R.drawable.bg_pill_unselected);
                        dayTv.setTextColor(ContextCompat.getColor(AgendarCitaActivity.this, R.color.dark_slate));
                        dayTv.setPadding(0, (int)(12 * density), 0, (int)(12 * density));
                        
                        String letter = "";
                        try {
                            java.util.Date date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(fechaStr);
                            letter = new java.text.SimpleDateFormat("E", new java.util.Locale("es", "ES")).format(date).substring(0, 1).toUpperCase() + "\n";
                        } catch (Exception e) {}
                        
                        String[] partes = fechaStr.split("-");
                        String dayNum = partes.length == 3 ? partes[2] : fechaStr;
                        dayTv.setText(letter + dayNum);
                        
                        dayTv.setOnClickListener(v -> {
                            if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(AgendarCitaActivity.this)) {
                                Toast.makeText(AgendarCitaActivity.this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int i=0; i<innerContainer.getChildCount(); i++) {
                                TextView t = (TextView) innerContainer.getChildAt(i);
                                t.setBackgroundResource(R.drawable.bg_pill_unselected);
                                t.setTextColor(ContextCompat.getColor(AgendarCitaActivity.this, R.color.dark_slate));
                            }
                            dayTv.setBackgroundResource(R.drawable.bg_pill_selected);
                            dayTv.setTextColor(ContextCompat.getColor(AgendarCitaActivity.this, R.color.white));
                            
                            selectedFecha = fechaStr;
                            timeSlotsContainer.setVisibility(View.VISIBLE);
                            updateSummary();
                            mostrarHorasDinamicas(fechaStr);
                        });
                        innerContainer.addView(dayTv);
                    }
                } else {
                    mostrarMensajeVacio(datesContainerDynamic);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<ApiService.DisponibilidadResponse>> call, Throwable t) {
                datesContainerDynamic.removeAllViews();
                TextView tvError = new TextView(AgendarCitaActivity.this);
                tvError.setText("Error de conexión con el servidor");
                datesContainerDynamic.addView(tvError);
            }
        });
    }

    private void mostrarMensajeVacio(LinearLayout datesContainer) {
        datesContainer.removeAllViews();
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("El doctor no tiene horarios disponibles en este momento.");
        tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.light_text));
        tvEmpty.setGravity(android.view.Gravity.CENTER);
        tvEmpty.setPadding(32, 32, 32, 32);
        tvEmpty.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_calendar, 0, 0);
        tvEmpty.setCompoundDrawablePadding(16);
        datesContainer.addView(tvEmpty);
    }

    private void mostrarHorasDinamicas(String fechaStr) {
        android.widget.GridLayout timesGridDynamic = findViewById(R.id.timesGridDynamic);
        timesGridDynamic.removeAllViews();
        
        java.util.List<ApiService.DisponibilidadResponse> slots = disponibilidadesMap.get(fechaStr);
        if (slots == null || slots.isEmpty()) {
            TextView timeTv = new TextView(this);
            timeTv.setText("No hay horarios disponibles para este día");
            timeTv.setTextColor(ContextCompat.getColor(this, R.color.light_text));
            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.columnSpec = android.widget.GridLayout.spec(0, 3);
            params.width = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(16, 16, 16, 16);
            timeTv.setLayoutParams(params);
            timesGridDynamic.addView(timeTv);
            return;
        }

        java.util.Collections.sort(slots, new java.util.Comparator<ApiService.DisponibilidadResponse>() {
            public int compare(ApiService.DisponibilidadResponse o1, ApiService.DisponibilidadResponse o2) {
                return o1.horaInicio.compareTo(o2.horaInicio);
            }
        });

        float density = getResources().getDisplayMetrics().density;
        
        for (ApiService.DisponibilidadResponse d : slots) {
            TextView timeTv = new TextView(this);
            android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams();
            params.width = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(16, 16, 16, 16);
            timeTv.setLayoutParams(params);
            
            timeTv.setGravity(android.view.Gravity.CENTER);
            timeTv.setBackgroundResource(R.drawable.bg_pill_unselected);
            timeTv.setTextColor(ContextCompat.getColor(this, R.color.dark_slate));
            timeTv.setPadding(32, 16, 32, 16);
            
            String horaCortada = d.horaInicio != null && d.horaInicio.length() >= 5 ? d.horaInicio.substring(0,5) : d.horaInicio;
            timeTv.setText(horaCortada);

            timeTv.setOnClickListener(v -> {
                if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(AgendarCitaActivity.this)) {
                    Toast.makeText(AgendarCitaActivity.this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i=0; i<timesGridDynamic.getChildCount(); i++) {
                    TextView t = (TextView) timesGridDynamic.getChildAt(i);
                    t.setBackgroundResource(R.drawable.bg_pill_unselected);
                    t.setTextColor(ContextCompat.getColor(this, R.color.dark_slate));
                }
                timeTv.setBackgroundResource(R.drawable.bg_pill_selected);
                timeTv.setTextColor(ContextCompat.getColor(this, R.color.white));
                
                selectedHora = horaCortada;
                sectionSummary.setVisibility(View.VISIBLE);
                updateSummary();
            });
            timesGridDynamic.addView(timeTv);
        }
    }

    private void setupTimeSlots() {
        int[] timeIds = {R.id.time0900, R.id.time1030, R.id.time1115, R.id.time1400, R.id.time1530, R.id.time1645};
        for (int id : timeIds) {
            TextView tv = findViewById(id);
            tv.setOnClickListener(v -> {
                if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                    Toast.makeText(this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int otherId : timeIds) {
                    TextView otherTv = findViewById(otherId);
                    otherTv.setBackgroundResource(R.drawable.bg_pill_unselected);
                    otherTv.setTextColor(ContextCompat.getColor(this, R.color.light_text));
                }
                tv.setBackgroundResource(R.drawable.bg_pill_selected);
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                
                String timeText = tv.getText().toString();
                // Simple logic for AM/PM based on ID or text
                if (id == R.id.time0900 || id == R.id.time1030 || id == R.id.time1115) {
                    selectedHora = timeText + " AM";
                } else {
                    selectedHora = timeText + " PM";
                }
                
                sectionSummary.setVisibility(View.VISIBLE);
                updateSummary();
            });
        }
    }

    private void updateSummary() {
        TextView summaryDoctor = findViewById(R.id.summaryDoctor);
        TextView summaryDate = findViewById(R.id.summaryDate);
        TextView summaryTime = findViewById(R.id.summaryTime);

        String displayFecha = selectedFecha;
        if (displayFecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                java.text.SimpleDateFormat of = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date d = of.parse(displayFecha);
                java.text.SimpleDateFormat nf = new java.text.SimpleDateFormat("EEEE, d 'de' MMMM", new java.util.Locale("es", "ES"));
                String res = nf.format(d);
                displayFecha = res.substring(0, 1).toUpperCase() + res.substring(1);
            } catch (Exception e) {}
        }

        String displayHora = selectedHora;
        if (!displayHora.contains("AM") && !displayHora.contains("PM") && !displayHora.isEmpty()) {
            try {
                java.text.SimpleDateFormat of = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                java.util.Date d = of.parse(displayHora);
                java.text.SimpleDateFormat nf = new java.text.SimpleDateFormat("hh:mm a", new java.util.Locale("es", "ES"));
                displayHora = nf.format(d).toUpperCase();
            } catch (Exception e) {}
        }

        summaryDoctor.setText(selectedDoctorName.isEmpty() ? getString(R.string.not_selected) : selectedDoctorName);
        summaryDate.setText(displayFecha.isEmpty() ? getString(R.string.not_selected_fem) : displayFecha);
        summaryTime.setText(displayHora.isEmpty() ? getString(R.string.not_selected_fem) : displayHora);
    }

    private void showSuccessDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.dialog_success_title)
            .setMessage(getString(R.string.dialog_success_message, selectedDoctorName, selectedFecha, selectedHora))
            .setPositiveButton(R.string.dialog_btn_ok, (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


