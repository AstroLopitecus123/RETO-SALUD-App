package com.example.reposalud.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reposalud.R;
import com.example.reposalud.database.UsuarioDAO;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    EditText etNombre, etApellido, etDni, etTelefono, etFechaNacimiento, etCorreo, etPassword;
    Button btnRegistrar;

    UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDni = findViewById(R.id.etDni);
        etTelefono = findViewById(R.id.etTelefono);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        android.widget.TextView tvLogin = findViewById(R.id.tvLogin);

        tvLogin.setOnClickListener(v -> finish());

        // Mostrar DatePicker al hacer clic en Fecha de Nacimiento
        etFechaNacimiento.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            java.util.Locale locale = new java.util.Locale("es", "ES");
            java.util.Locale.setDefault(locale);
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.locale = locale;
            android.content.Context context = getBaseContext().createConfigurationContext(config);

            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // Formato visual para el usuario
                String fechaVisual = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                etFechaNacimiento.setText(fechaVisual);
                
                // Formato real para la base de datos
                String fechaApi = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                etFechaNacimiento.setTag(fechaApi);
            }, year, month, day);
            
            datePickerDialog.show();
        });

        // la parte del cap
        usuarioDAO = new UsuarioDAO(this);

        btnRegistrar.setOnClickListener(v -> {
            if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                android.widget.Toast.makeText(this, "No disponible en modo sin conexión", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String dni = etDni.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String fechaNacimiento = etFechaNacimiento.getTag() != null ? etFechaNacimiento.getTag().toString() : "";
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || 
               telefono.isEmpty() || fechaNacimiento.isEmpty() || 
               correo.isEmpty() || password.isEmpty()) {

                Toast.makeText(this,
                        "Complete todos los campos",
                        Toast.LENGTH_SHORT).show();

            } else {
                ApiService.RegisterRequest regRequest = new ApiService.RegisterRequest(nombre, apellido, correo, password, telefono, fechaNacimiento, dni);
                RetrofitClient.getApiService().registro(regRequest).enqueue(new Callback<ApiService.AuthResponse>() {
                    @Override
                    public void onResponse(Call<ApiService.AuthResponse> call, Response<ApiService.AuthResponse> response) {
                        if (response.isSuccessful()) {
                            usuarioDAO.insertarUsuario(nombre, correo, password);
                            Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error al registrar en el servidor", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiService.AuthResponse> call, Throwable t) {
                        boolean registrado = usuarioDAO.insertarUsuario(nombre, correo, password);
                        if (registrado) {
                            Toast.makeText(RegisterActivity.this, "Registrado localmente (Modo Offline)", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "El correo ya existe localmente", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
