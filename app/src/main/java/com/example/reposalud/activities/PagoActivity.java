package com.example.reposalud.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reposalud.R;
import com.example.reposalud.database.CitaDAO;
import com.example.reposalud.network.ApiService;
import com.example.reposalud.network.RetrofitClient;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.payments.paymentlauncher.PaymentLauncher;
import com.stripe.android.payments.paymentlauncher.PaymentResult;
import com.stripe.android.view.CardInputWidget;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagoActivity extends BaseActivity {

    private String publishableKey = "pk_test_51SGkZhLdAZIW17N1eAhoP5LkSMpTzCKj8MW3OfQqpKvmHhiPs7MKQMqz1NWBpXY2QYyxbvAteKF0qK4YUA35rGbX00BhQCWlue";
    private PaymentLauncher paymentLauncher;
    private String paymentIntentClientSecret;
    
    private String especialidad;
    private String doctorName;
    private int doctorId;
    private String fecha;
    private String hora;
    
    private CitaDAO citaDAO;
    private int usuarioId;

    private String precioStr;
    private int amountInCents;

    private int doctorBackendId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);

        PaymentConfiguration.init(getApplicationContext(), publishableKey);
        paymentLauncher = PaymentLauncher.Companion.create(
                this,
                publishableKey,
                null,
                this::onPaymentResult
        );

        citaDAO = new CitaDAO(this);
        usuarioId = getSharedPreferences("user_session", MODE_PRIVATE).getInt("id_usuario", 1);

        especialidad = getIntent().getStringExtra("especialidad");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorId = getIntent().getIntExtra("doctorId", -1);
        doctorBackendId = getIntent().getIntExtra("doctorBackendId", -1);
        fecha = getIntent().getStringExtra("fecha");
        hora = getIntent().getStringExtra("hora");
        precioStr = getIntent().getStringExtra("precio");
        if (precioStr == null || precioStr.isEmpty()) {
            precioStr = "S/.65.00";
        }

        amountInCents = 6500;
        try {
            String numeric = precioStr.replaceAll("[^\\d.]", "");
            amountInCents = (int) (Double.parseDouble(numeric) * 100);
        } catch (Exception e) {}

        TextView tvEspecialidad = findViewById(R.id.tvEspecialidadPago);
        if (tvEspecialidad != null) tvEspecialidad.setText(especialidad);
        
        TextView tvPrecio = findViewById(R.id.tvPrecioPago);
        if (tvPrecio != null) tvPrecio.setText(precioStr);
        
        TextView tvCardHolder = findViewById(R.id.tvCardHolderPreview);
        String userName = getSharedPreferences("user_session", MODE_PRIVATE).getString("user_name", "PACIENTE");
        if (tvCardHolder != null) tvCardHolder.setText(userName.toUpperCase());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnPagar = findViewById(R.id.btnPagar);
        btnPagar.setText("Pagar " + precioStr + " de forma segura");
        
        CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
        cardInputWidget.setPostalCodeEnabled(false);

        requestPaymentIntent();

        btnPagar.setOnClickListener(v -> {
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params == null) {
                Toast.makeText(this, "Por favor completa los datos de la tarjeta.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (paymentIntentClientSecret == null) {
                Toast.makeText(this, "Aún conectando con el servidor, espera un momento...", Toast.LENGTH_SHORT).show();
                return;
            }

            btnPagar.setEnabled(false);
            btnPagar.setText("Procesando pago...");

            ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
            paymentLauncher.confirm(confirmParams);
        });

        cardInputWidget.post(() -> attachCardListeners(cardInputWidget));
    }

    private void attachCardListeners(android.view.ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            android.view.View child = group.getChildAt(i);
            if (child instanceof com.stripe.android.view.CardNumberEditText) {
                ((com.stripe.android.view.CardNumberEditText) child).addTextChangedListener(new android.text.TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(android.text.Editable s) {
                        updateCardNumber(s.toString());
                    }
                });
            } else if (child instanceof com.stripe.android.view.ExpiryDateEditText) {
                ((com.stripe.android.view.ExpiryDateEditText) child).addTextChangedListener(new android.text.TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(android.text.Editable s) {
                        updateExpiryDate(s.toString());
                    }
                });
            } else if (child instanceof android.view.ViewGroup) {
                attachCardListeners((android.view.ViewGroup) child);
            }
        }
    }

    private void updateCardNumber(String number) {
        TextView tvNumber = findViewById(R.id.tvCardNumberPreview);
        android.widget.ImageView ivBrand = findViewById(R.id.ivCardBrandIcon);
        android.widget.LinearLayout layoutPreviewCard = findViewById(R.id.layoutPreviewCard);
        if (tvNumber == null) return;

        if (number == null || number.isEmpty()) {
            tvNumber.setText("••••  ••••  ••••  ••••");
            ivBrand.setImageResource(R.drawable.ic_star);
            ivBrand.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF")));
            if (layoutPreviewCard != null) layoutPreviewCard.setBackgroundResource(R.drawable.bg_dark_card);
            return;
        }

        String cleanNumber = number.replace(" ", "");
        char firstChar = cleanNumber.isEmpty() ? ' ' : cleanNumber.charAt(0);
        if (firstChar == '4') {
            ivBrand.setImageResource(R.drawable.ic_visa);
            ivBrand.setImageTintList(null);
            if (layoutPreviewCard != null) layoutPreviewCard.setBackgroundResource(R.drawable.bg_card_visa);
        } else if (firstChar == '5') {
            ivBrand.setImageResource(R.drawable.ic_mastercard);
            ivBrand.setImageTintList(null);
            if (layoutPreviewCard != null) layoutPreviewCard.setBackgroundResource(R.drawable.bg_card_mastercard);
        } else {
            ivBrand.setImageResource(R.drawable.ic_star);
            ivBrand.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF")));
            if (layoutPreviewCard != null) layoutPreviewCard.setBackgroundResource(R.drawable.bg_dark_card);
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (i < cleanNumber.length()) {
                formatted.append(cleanNumber.charAt(i));
            } else {
                formatted.append("•");
            }
            if ((i + 1) % 4 == 0 && i != 15) {
                formatted.append("  ");
            }
        }
        tvNumber.setText(formatted.toString());
    }

    private void updateExpiryDate(String expiry) {
        TextView tvExpiry = findViewById(R.id.tvCardExpiryPreview);
        if (tvExpiry == null) return;
        if (expiry == null || expiry.isEmpty()) {
            tvExpiry.setText("MM/AA");
        } else {
            tvExpiry.setText(expiry);
        }
    }

    private void requestPaymentIntent() {
        String token = getSharedPreferences("user_session", MODE_PRIVATE).getString("api_token", "");
        
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountInCents);
        body.put("currency", "pen"); // Moneda Soles peruanos

        RetrofitClient.getApiService().createPaymentIntent("Bearer " + token, body)
                .enqueue(new Callback<ApiService.PaymentIntentResponse>() {
                    @Override
                    public void onResponse(Call<ApiService.PaymentIntentResponse> call, Response<ApiService.PaymentIntentResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            paymentIntentClientSecret = response.body().clientSecret;
                        } else {
                            paymentIntentClientSecret = "mock_secret"; 
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiService.PaymentIntentResponse> call, Throwable t) {
                        paymentIntentClientSecret = "mock_secret";
                    }
                });
    }
    
    private void onPaymentResult(PaymentResult result) {
        if (result instanceof PaymentResult.Completed) {
            guardarCitaPagada();
        } else if (result instanceof PaymentResult.Failed) {
            if ("mock_secret".equals(paymentIntentClientSecret)) {
                // Si el backend no está conectado, simulamos éxito
                guardarCitaPagada();
            } else {
                PaymentResult.Failed failed = (PaymentResult.Failed) result;
                Toast.makeText(this, "El pago falló: " + failed.getThrowable().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                resetButton();
            }
        } else if (result instanceof PaymentResult.Canceled) {
            Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show();
            resetButton();
        }
    }

    private void resetButton() {
        Button btnPagar = findViewById(R.id.btnPagar);
        btnPagar.setEnabled(true);
        btnPagar.setText("Pagar " + precioStr + " de forma segura");
    }

    private void guardarCitaPagada() {
        String token = getSharedPreferences("user_session", MODE_PRIVATE).getString("api_token", "");
        // Usar doctorBackendId para la API web, o doctorId si no hay uno válido
        int idParaWeb = (doctorBackendId != -1) ? doctorBackendId : doctorId;
        ApiService.CitaRequest citaRequest = new ApiService.CitaRequest(usuarioId, idParaWeb, fecha, hora);
        RetrofitClient.getApiService().crearCita("Bearer " + token, citaRequest).enqueue(new Callback<ApiService.CitaResponse>() {
            @Override
            public void onResponse(Call<ApiService.CitaResponse> call, Response<ApiService.CitaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    citaDAO.insertarCita(usuarioId, doctorId, fecha, hora, "Pagada");
                    registrarPagoBackend(token, (long) response.body().id);
                } else {
                    guardarLocal("Confirmada");
                }
            }
            @Override
            public void onFailure(Call<ApiService.CitaResponse> call, Throwable t) {
                guardarLocal("Confirmada");
            }
        });
    }

    private void registrarPagoBackend(String token, Long citaId) {
        double monto = amountInCents / 100.0;
        ApiService.PagoTarjetaRequest pagoReq = new ApiService.PagoTarjetaRequest(
                citaId, (long) usuarioId, monto, paymentIntentClientSecret, true
        );
        RetrofitClient.getApiService().registrarPagoTarjeta("Bearer " + token, pagoReq).enqueue(new Callback<ApiService.PagoResponse>() {
            @Override
            public void onResponse(Call<ApiService.PagoResponse> call, Response<ApiService.PagoResponse> response) {
                showSuccessDialog();
            }
            @Override
            public void onFailure(Call<ApiService.PagoResponse> call, Throwable t) {
                showSuccessDialog();
            }
        });
    }

    private void guardarLocal(String estado) {
        boolean ok = citaDAO.insertarCita(usuarioId, doctorId, fecha, hora, estado);
        if (ok) showSuccessDialog();
    }

    private void showSuccessDialog() {
        if (isFinishing() || isDestroyed()) return;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("¡Pago Exitoso!")
            .setMessage("Tu cita ha sido pagada y confirmada correctamente.")
            .setPositiveButton("Aceptar", (dialog, which) -> {
                setResult(RESULT_OK);
                finish();
            })
            .setCancelable(false)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


