package com.example.reposalud.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reposalud.R;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.reposalud.database.UsuarioDAO;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etTelefono, etFechaNacimiento, etDni;
    private Button btnGuardar;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etNombre = findViewById(R.id.etNombreEdit);
        etApellido = findViewById(R.id.etApellidoEdit);
        etTelefono = findViewById(R.id.etTelefonoEdit);
        etFechaNacimiento = findViewById(R.id.etFechaNacimientoEdit);
        etDni = findViewById(R.id.etDniEdit);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getInt("id_usuario", -1);
        String nombreActual = prefs.getString("user_name", "");
        etNombre.setText(nombreActual);

        String token = prefs.getString("api_token", "");
        if (userId != -1 && !token.isEmpty()) {
            RetrofitClient.getApiService().obtenerUsuarioPorId("Bearer " + token, userId).enqueue(new Callback<ApiService.UsuarioResponse>() {
                @Override
                public void onResponse(Call<ApiService.UsuarioResponse> call, Response<ApiService.UsuarioResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiService.UsuarioResponse user = response.body();
                        if (user.nombre != null) etNombre.setText(user.nombre);
                        if (user.apellido != null) etApellido.setText(user.apellido);
                        if (user.dni != null) etDni.setText(user.dni);
                        if (user.telefono != null) etTelefono.setText(user.telefono);
                        if (user.fechaNacimiento != null) {
                            etFechaNacimiento.setTag(user.fechaNacimiento);
                            try {
                                String[] parts = user.fechaNacimiento.split("-");
                                if (parts.length == 3) {
                                    String formatted = parts[2] + "/" + parts[1] + "/" + parts[0];
                                    etFechaNacimiento.setText(formatted);
                                }
                            } catch (Exception e) {
                                etFechaNacimiento.setText(user.fechaNacimiento);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiService.UsuarioResponse> call, Throwable t) {
                    // Silent failure, user will edit what they have
                }
            });
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarEditProfile);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFechaNacimiento.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            Locale locale = new Locale("es", "ES");
            Locale.setDefault(locale);
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.locale = locale;
            getBaseContext().createConfigurationContext(config);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String fechaVisual = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                etFechaNacimiento.setText(fechaVisual);
                
                String fechaApi = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                etFechaNacimiento.setTag(fechaApi);
            }, year, month, day);
            datePickerDialog.show();
        });

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getTag() != null ? etFechaNacimiento.getTag().toString() : "";

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no válido. Inicie sesión de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String token = prefs.getString("api_token", "");

        ApiService.UpdateUserRequest request = new ApiService.UpdateUserRequest(nombre, apellido, telefono, fechaNacimiento, dni);

        RetrofitClient.getApiService().actualizarUsuario("Bearer " + token, userId, request).enqueue(new Callback<ApiService.UsuarioResponse>() {
            @Override
            public void onResponse(Call<ApiService.UsuarioResponse> call, Response<ApiService.UsuarioResponse> response) {
                if (response.isSuccessful()) {
                    prefs.edit().putString("user_name", nombre).apply();

                    UsuarioDAO usuarioDAO = new UsuarioDAO(EditProfileActivity.this);
                    usuarioDAO.actualizarNombreUsuario(nombre, prefs.getString("correo", ""));

                    Toast.makeText(EditProfileActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Error al actualizar en el servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.UsuarioResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error de red. Inténtalo más tarde.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
