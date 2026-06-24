package com.example.reposalud.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.reposalud.R;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;
import com.example.reposalud.utils.NavigationHelper;

import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivProfileLarge;
    private ImageView ivAvatarSmall;
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Cargar datos del usuario
        cargarDatosUsuario();

        // 2. Configurar botones y opciones
        setupClickListeners();

        // 3. Configurar Bottom Navigation
        NavigationHelper.setupBottomNavigation(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvUserDetails = findViewById(R.id.tvUserDetails);
        ivProfileLarge = findViewById(R.id.ivProfileLarge);

        // Intentar obtener de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String nombre = prefs.getString("user_name", null);
        String fotoUrl = prefs.getString("foto_url", null);
        userId = prefs.getInt("id_usuario", -1);

        // Fallback al Intent si no hay en SharedPreferences
        if (nombre == null) {
            nombre = getIntent().getStringExtra("nombre_usuario");
        }

        if (nombre != null) {
            tvUserName.setText(nombre);
            tvUserDetails.setVisibility(android.view.View.GONE);
        }

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(this).load(fotoUrl).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL).into(ivProfileLarge);
        }
    }

    private void setupClickListeners() {
        // Opción: Historial Médico
        findViewById(R.id.cvHistorial).setOnClickListener(v -> {
            Intent intent = new Intent(this, HistorialMedicoActivity.class);
            startActivity(intent);
        });

        // Opción: Editar Perfil
        findViewById(R.id.cvEditarPerfil).setOnClickListener(v -> {
            if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                android.widget.Toast.makeText(this, "No disponible en modo sin conexión", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Opción: Ajustes
        findViewById(R.id.cvAjustes).setOnClickListener(v -> {
            Intent intent = new Intent(this, AjustesActivity.class);
            startActivity(intent);
        });
        // Botón: Cerrar Sesión
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Limpiar sesión
            getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply();
            
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Abrir galería al tocar la foto de perfil
        if (ivProfileLarge != null) {
            ivProfileLarge.setOnClickListener(v -> {
                if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this)) {
                    android.widget.Toast.makeText(this, "No disponible en modo sin conexión", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                abrirGaleria();
            });
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            iniciarUCrop(imageUri);
        } else if (requestCode == com.yalantis.ucrop.UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri resultUri = com.yalantis.ucrop.UCrop.getOutput(data);
            if (resultUri != null) {
                subirImagen(resultUri);
            }
        } else if (resultCode == com.yalantis.ucrop.UCrop.RESULT_ERROR) {
            Throwable cropError = com.yalantis.ucrop.UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(this, "Error al editar imagen: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void iniciarUCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "uCrop_" + System.currentTimeMillis() + ".jpg"));
        
        com.yalantis.ucrop.UCrop.Options options = new com.yalantis.ucrop.UCrop.Options();
        options.setCircleDimmedLayer(false);
        options.setShowCropFrame(true);
        options.setShowCropGrid(true);
        options.setToolbarTitle("Editar imagen");
        
        com.yalantis.ucrop.UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .start(this);
    }

    private void subirImagen(Uri uri) {
        if (userId == -1) {
            Toast.makeText(this, "Error: No se encontró ID de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String token = prefs.getString("api_token", "");
        if (token.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró token de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Toast.makeText(this, "Error: No se pudo leer la imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            // Comprimir la imagen a JPEG 70% para evitar el límite de 1MB del servidor
            File file = new File(getCacheDir(), "perfil_temp.jpg");
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream);
            outputStream.flush();
            outputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("archivo", file.getName(), requestFile);

            Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show();

            RetrofitClient.getApiService().subirFoto("Bearer " + token, userId, body).enqueue(new Callback<ApiService.UsuarioResponse>() {
                @Override
                public void onResponse(Call<ApiService.UsuarioResponse> call, Response<ApiService.UsuarioResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String nuevaFotoUrl = response.body().fotoUrl;
                        
                        // Guardar en SharedPreferences
                        getSharedPreferences("user_session", MODE_PRIVATE)
                                .edit()
                                .putString("foto_url", nuevaFotoUrl)
                                .apply();

                        // Actualizar UI
                        Glide.with(ProfileActivity.this).load(nuevaFotoUrl).diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL).into(ivProfileLarge);
                        
                        Toast.makeText(ProfileActivity.this, "Foto actualizada correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error al actualizar la foto", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiService.UsuarioResponse> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error procesando la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}


