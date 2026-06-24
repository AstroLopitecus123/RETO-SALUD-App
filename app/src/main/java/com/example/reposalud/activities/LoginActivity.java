package com.example.reposalud.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reposalud.R;
import com.example.reposalud.database.UsuarioDAO;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends BaseActivity {

    EditText etCorreo, etPassword;
    Button btnLogin;
    TextView tvRegistrar;
    UsuarioDAO usuarioDAO;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_REPOSALUD);
        super.onCreate(savedInstanceState);

        // --- VERIFICAR AUTOLOGIN ---
        android.content.SharedPreferences prefsAjustes = getSharedPreferences("AjustesApp", MODE_PRIVATE);
        boolean mantenerSesion = prefsAjustes.getBoolean("mantener_sesion", false);
        
        android.content.SharedPreferences prefsUser = getSharedPreferences("user_session", MODE_PRIVATE);
        boolean isLoggedIn = prefsUser.getBoolean("is_logged_in", false);
        
        if (mantenerSesion && isLoggedIn) {
            String nombre = prefsUser.getString("user_name", "Usuario");
            int id = prefsUser.getInt("id_usuario", -1);
            // No pasamos token aún para no complicar, asumiendo offline o guardado si es necesario.
            // Si necesitamos token completo, habría que extraerlo, pero por ahora solo el intent
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("nombre_usuario", nombre);
            intent.putExtra("id_usuario", id);
            startActivity(intent);
            finish();
            return;
        }
        // ---------------------------

        setContentView(R.layout.activity_login);

        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegistrar = findViewById(R.id.tvRegistrar);
        // la parte del cap

        usuarioDAO = new UsuarioDAO(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("473447043826-0d5crfghn3m7cug1ibfefnr24lsmp5g8.apps.googleusercontent.com")
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        tvRegistrar.setOnClickListener(v -> {
            if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(LoginActivity.this)) {
                Toast.makeText(LoginActivity.this, "No disponible en modo sin conexión", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Bypass para desarrollo y visualización
            if (correo.equalsIgnoreCase("admin") || (correo.equals("test@test.com") && password.equals("123456"))) {
                irAHome("Usuario de Prueba");
                return;
            }

            if(correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // EDITADO POR ASTRO - Intento de Login Remoto en Railway primero
                ApiService.LoginRequest loginRequest = new ApiService.LoginRequest(correo, password);
                RetrofitClient.getApiService().login(loginRequest).enqueue(new retrofit2.Callback<ApiService.AuthResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<ApiService.AuthResponse> call, retrofit2.Response<ApiService.AuthResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiService.AuthResponse authResponse = response.body();
                            if (authResponse != null) {
                                String fotoUrl = authResponse.fotoUrl;
                                int backendId = authResponse.id;
                                String nombreCompleto = authResponse.nombre;
                                if (authResponse.apellido != null && !authResponse.apellido.isEmpty()) {
                                    nombreCompleto += " " + authResponse.apellido;
                                }
                                
                                // Guardar datos permanentes para modo offline
                                android.content.SharedPreferences offlinePrefs = getSharedPreferences("offline_users", MODE_PRIVATE);
                                offlinePrefs.edit()
                                    .putInt(correo + "_id", backendId)
                                    .putString(correo + "_foto", fotoUrl)
                                    .putString(correo + "_nombre", nombreCompleto)
                                    .apply();

                                // Si el login web fue exitoso, guardamos/actualizamos en el SQLite local
                                if (!usuarioDAO.existeCorreo(correo)) {
                                    usuarioDAO.insertarUsuario(nombreCompleto, correo, password);
                                }

                                irAHome(nombreCompleto, backendId, authResponse.token, fotoUrl);
                            }
                        } else {
                            // Si el servidor responde pero con error
                            Toast.makeText(LoginActivity.this, "Credenciales incorrectas en el servidor", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ApiService.AuthResponse> call, Throwable t) {
                        // Intento de login offline
                        String nombreLocal = usuarioDAO.obtenerNombreUsuario(correo, password);
                        if (nombreLocal != null) {
                            android.content.SharedPreferences offlinePrefs = getSharedPreferences("offline_users", MODE_PRIVATE);
                            int idRemoto = offlinePrefs.getInt(correo + "_id", -1);
                            String fotoUrl = offlinePrefs.getString(correo + "_foto", null);
                            String nombreCompleto = offlinePrefs.getString(correo + "_nombre", nombreLocal);
                            
                            irAHome(nombreCompleto, idRemoto, "", fotoUrl);
                            Toast.makeText(LoginActivity.this, "Modo sin conexión", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Credenciales inválidas o sin acceso previo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    // EDITADO POR ASTRO - Google Auth y Sincronizacion con Nube
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Toast.makeText(this, "Verificando cuenta de Google, por favor espera...", Toast.LENGTH_LONG).show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String nombre = account.getDisplayName();
                    String correo = account.getEmail();
                    String idToken = account.getIdToken();

                    // Guardado local offline (por si acaso)
                    String nombreFinal = usuarioDAO.registrarOloginGoogle(nombre, correo);
                    
                    // Sincronizacion web real con el endpoint /auth/google
                    if (idToken != null && !idToken.isEmpty()) {
                        java.util.Map<String, String> body = new java.util.HashMap<>();
                        body.put("idToken", idToken);
                        
                        RetrofitClient.getApiService().loginGoogle(body).enqueue(new retrofit2.Callback<ApiService.AuthResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<ApiService.AuthResponse> call, retrofit2.Response<ApiService.AuthResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    irAHome(nombreFinal, response.body().id, response.body().token, response.body().fotoUrl);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Error al sincronizar cuenta de Google con el servidor", Toast.LENGTH_SHORT).show();
                                    irAHome(nombreFinal, -1, "", null);
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<ApiService.AuthResponse> call, Throwable t) {
                                irAHome(nombreFinal, -1, "", null);
                            }
                        });
                    } else {
                        irAHome(nombreFinal, -1, "", null);
                    }
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Error con Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void irAHome(String nombre) {
        irAHome(nombre, -1, "", null);
    }

    private void irAHome(String nombre, int idRemoto, String token, String fotoUrl) {
        android.content.SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        if (fotoUrl == null) {
            fotoUrl = prefs.getString("foto_url", null);
        }
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_name", nombre)
                .putInt("id_usuario", idRemoto)
                .putString("api_token", token)
                .putString("foto_url", fotoUrl)
                .apply();

        Toast.makeText(this, "Bienvenido " + nombre, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("nombre_usuario", nombre);
        intent.putExtra("id_usuario", idRemoto);
        startActivity(intent);
        finish();
    }
}
