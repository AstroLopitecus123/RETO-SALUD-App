package com.example.reposalud.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import okhttp3.MultipartBody;

// EDITADO POR ASTRO -Comunicación con el backend
public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/registro")
    Call<AuthResponse> registro(@Body RegisterRequest request);

    @POST("auth/google")
    Call<AuthResponse> loginGoogle(@Body java.util.Map<String, String> body);

    @POST("api/citas")
    Call<CitaResponse> crearCita(
            @retrofit2.http.Header("Authorization") String token,
            @Body CitaRequest request
    );

    @GET("api/citas/usuario/{id}")
    Call<List<CitaResponse>> obtenerCitas(
            @retrofit2.http.Header("Authorization") String token,
            @Path("id") int usuarioId
    );

    @GET("api/medicos")
    Call<List<MedicoResponse>> obtenerMedicos();

    @GET("api/historiales/paciente/{pacienteId}")
    Call<List<HistorialResponse>> obtenerHistorialPorPaciente(
            @retrofit2.http.Header("Authorization") String token,
            @Path("pacienteId") int pacienteId
    );

    @GET("api/disponibilidades/medico/{id}")
    Call<List<DisponibilidadResponse>> obtenerDisponibilidades(
            @retrofit2.http.Header("Authorization") String token,
            @Path("id") int medicoId
    );

    @Multipart
    @POST("api/usuarios/{id}/foto")
    Call<UsuarioResponse> subirFoto(
            @retrofit2.http.Header("Authorization") String token,
            @Path("id") int id,
            @Part MultipartBody.Part archivo
    );

    @POST("api/pagos/tarjeta")
    Call<PagoResponse> registrarPagoTarjeta(
            @retrofit2.http.Header("Authorization") String token,
            @Body PagoTarjetaRequest request
    );

    @retrofit2.http.PUT("api/usuarios/{id}")
    Call<UsuarioResponse> actualizarUsuario(
            @retrofit2.http.Header("Authorization") String token,
            @Path("id") int id,
            @Body UpdateUserRequest request
    );

    @GET("api/usuarios/{id}")
    Call<UsuarioResponse> obtenerUsuarioPorId(
            @retrofit2.http.Header("Authorization") String token,
            @Path("id") int id
    );

    class LoginRequest {
        public String correo;
        public String contrasena;
        public LoginRequest(String correo, String contrasena) {
            this.correo = correo;
            this.contrasena = contrasena;
        }
    }

    class RegisterRequest {
        public String nombre;
        public String apellido;
        public String correo;
        public String contrasena;
        public String telefono;
        public String fechaNacimiento;
        public String dni;
        public Long paisId = 1L;
        public String rol = "PACIENTE";
        
        public RegisterRequest(String nombre, String apellido, String correo, String contrasena, String telefono, String fechaNacimiento, String dni) {
            this.nombre = nombre;
            this.apellido = apellido;
            this.correo = correo;
            this.contrasena = contrasena;
            this.telefono = telefono;
            this.fechaNacimiento = fechaNacimiento;
            this.dni = dni;
        }
    }

    class UpdateUserRequest {
        public String nombre;
        public String apellido;
        public String telefono;
        public String fechaNacimiento;
        public String dni;

        public UpdateUserRequest(String nombre, String apellido, String telefono, String fechaNacimiento, String dni) {
            this.nombre = nombre;
            this.apellido = apellido;
            this.telefono = telefono;
            this.fechaNacimiento = fechaNacimiento;
            this.dni = dni;
        }
    }

    class CitaRequest {
        public IdObject paciente;
        public IdObject medico;
        public String fecha;
        public String estado = "CONFIRMADA";
        public CitaRequest(int pacienteId, int medicoId, String fecha, String hora) {
            this.paciente = new IdObject(pacienteId);
            this.medico = new IdObject(medicoId);
            this.fecha = fecha + "T" + hora + ":00";
        }
    }

    class IdObject {
        public int id;
        public IdObject(int id) { this.id = id; }
    }

    class PagoTarjetaRequest {
        public Long citaId;
        public Long usuarioId;
        public double monto;
        public String referencia;
        public Boolean exito;
        public PagoTarjetaRequest(Long citaId, Long usuarioId, double monto, String referencia, Boolean exito) {
            this.citaId = citaId;
            this.usuarioId = usuarioId;
            this.monto = monto;
            this.referencia = referencia;
            this.exito = exito;
        }
    }

    class PagoResponse {
        public Long id;
        public String estadoPago;
    }

    // EDITADO POR ASTRO - Respuesta para coincidir con el backend
    class AuthResponse {
        public String token;
        public int id;
        public String nombre;
        public String apellido;
        public String correo;
        public String fotoUrl;
    }

    class CitaResponse {
        public int id;
        public String fecha;
        public String estado;
        public MedicoHistorialResponse medico;
    }

    class MedicoResponse {
        public int id;
        public UsuarioResponse usuario;
        public EspecialidadResponse especialidad;
    }

    class UsuarioResponse {
        public int id;
        public String nombre;
        public String apellido;
        public String correo;
        public String fotoUrl;
        public String telefono;
        public String dni;
        public String fechaNacimiento;
    }

    class EspecialidadResponse {
        public int id;
        public String nombre;
    }

    class HistorialResponse {
        public int id;
        public String diagnostico;
        public String receta;
        public String notas;
        public CitaHistorialResponse cita;
    }

    class CitaHistorialResponse {
        public int id;
        public String fecha;
        public String hora;
        public MedicoHistorialResponse medico;
        public EspecialidadResponse especialidad;
    }

    class MedicoHistorialResponse {
        public int id;
        public UsuarioResponse usuario;
        public EspecialidadResponse especialidad;
    }

    class DisponibilidadResponse {
        public Long id;
        public String fecha;
        public String horaInicio;
        public String horaFin;
        public String estado;
    }

    // EDITADO POR ASTRO - Clases para Stripe
    class PaymentIntentResponse {
        @com.google.gson.annotations.SerializedName(value="clientSecret", alternate={"client_secret"})
        public String clientSecret;
    }

    @POST("api/create-payment-intent")
    Call<PaymentIntentResponse> createPaymentIntent(
            @retrofit2.http.Header("Authorization") String token,
            @Body java.util.Map<String, Object> body
    );

    class TeleconsultaConfigResponse {
        public String agoraAppId;
        public String agoraToken;
    }

    @GET("api/teleconsulta/config")
    Call<TeleconsultaConfigResponse> getTeleconsultaConfig(
            @retrofit2.http.Header("Authorization") String token,
            @retrofit2.http.Query("canal") String canal
    );
}
