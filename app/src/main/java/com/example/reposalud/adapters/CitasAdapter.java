package com.example.reposalud.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reposalud.R;
import com.example.reposalud.network.ApiService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.ImageView;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {

    private Context context;
    private List<ApiService.CitaResponse> citasList;

    public CitasAdapter(Context context, List<ApiService.CitaResponse> citasList) {
        this.context = context;
        this.citasList = citasList;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        ApiService.CitaResponse cita = citasList.get(position);

        try {
            String fechaCruda = cita.fecha;
            fechaCruda = fechaCruda.replace("T", " ").replace(" • ", " ");
            
            java.text.SimpleDateFormat originalFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = originalFormat.parse(fechaCruda);
            
            java.text.SimpleDateFormat niceFormat = new java.text.SimpleDateFormat("EEEE, d 'de' MMMM '•' hh:mm a", new Locale("es", "ES"));
            String formattedDate = niceFormat.format(date);

            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            
            holder.tvFechaCita.setText(formattedDate);
        } catch (Exception e) {
            holder.tvFechaCita.setText(cita.fecha);
        }

        if (cita.medico != null && cita.medico.usuario != null) {
            holder.tvDoctorNombre.setText("Dr. " + cita.medico.usuario.nombre + " " + cita.medico.usuario.apellido);
            
            if (cita.medico.especialidad != null) {
                holder.tvEspecialidadNombre.setText(cita.medico.especialidad.nombre);
            } else {
                holder.tvEspecialidadNombre.setText("Especialista");
            }

            if (cita.medico.usuario.fotoUrl != null && cita.medico.usuario.fotoUrl.startsWith("http")) {
                Glide.with(context).load(cita.medico.usuario.fotoUrl).into(holder.ivDoctorFoto);
            } else {
                int resId = context.getResources().getIdentifier(cita.medico.usuario.fotoUrl != null ? cita.medico.usuario.fotoUrl : "logo_solo", "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.ivDoctorFoto.setImageResource(resId);
                } else {
                    holder.ivDoctorFoto.setImageResource(R.drawable.logo_solo);
                }
            }
        }

        // Estado
        String estado = cita.estado != null ? cita.estado.toUpperCase() : "PENDIENTE";
        holder.tvEstadoCita.setText(estado);
        
        if (estado.equals("CONFIRMADA")) {
            holder.tvEstadoCita.setTextColor(context.getResources().getColor(R.color.primary_green));
            holder.tvEstadoCita.setBackgroundResource(R.drawable.bg_pill_selected); // Cambiar esto en un futuro si es necesario
            holder.tvEstadoCita.getBackground().mutate().setTint(Color.parseColor("#E8F5E9"));
            
            holder.btnTeleconsulta.setVisibility(View.VISIBLE);
            holder.btnTeleconsulta.setOnClickListener(v -> {
                if (!com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(context)) {
                    android.widget.Toast.makeText(context, "Videollamada no disponible en modo sin conexión", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                String token = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("api_token", "");
                android.widget.Toast.makeText(context, "Conectando a teleconsulta...", android.widget.Toast.LENGTH_SHORT).show();
                
                com.example.reposalud.network.RetrofitClient.getApiService()
                    .getTeleconsultaConfig("Bearer " + token, "cita-" + cita.id)
                    .enqueue(new retrofit2.Callback<ApiService.TeleconsultaConfigResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<ApiService.TeleconsultaConfigResponse> call, retrofit2.Response<ApiService.TeleconsultaConfigResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String aId = response.body().agoraAppId;
                                String aToken = response.body().agoraToken;
                                if (aId != null && !aId.equals("AGORA_NOT_FOUND") && aToken != null && !aToken.startsWith("ERROR")) {
                                    android.content.Intent intent = new android.content.Intent(context, com.example.reposalud.activities.TeleconsultaActivity.class);
                                    intent.putExtra("agora_app_id", aId);
                                    intent.putExtra("agora_token", aToken);
                                    intent.putExtra("agora_canal", "cita-" + cita.id);
                                    context.startActivity(intent);
                                } else {
                                    android.widget.Toast.makeText(context, "Error de configuración Agora en el servidor", android.widget.Toast.LENGTH_LONG).show();
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Error al obtener acceso a la sala", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<ApiService.TeleconsultaConfigResponse> call, Throwable t) {
                            android.widget.Toast.makeText(context, "Error de red: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
            });
        } else if (estado.equals("CANCELADA")) {
            holder.tvEstadoCita.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvEstadoCita.setBackgroundResource(R.drawable.bg_pill_selected);
            holder.tvEstadoCita.getBackground().mutate().setTint(Color.parseColor("#FFEBEE"));
        } else {
            holder.tvEstadoCita.setTextColor(Color.parseColor("#1976D2"));
            holder.tvEstadoCita.setBackgroundResource(R.drawable.bg_pill_selected);
            holder.tvEstadoCita.getBackground().mutate().setTint(Color.parseColor("#E3F2FD"));
            holder.btnTeleconsulta.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return citasList != null ? citasList.size() : 0;
    }

    public void actualizarLista(List<ApiService.CitaResponse> nuevaLista) {
        this.citasList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFechaCita, tvEstadoCita, tvDoctorNombre, tvEspecialidadNombre;
        ImageView ivDoctorFoto;
        android.widget.LinearLayout btnTeleconsulta;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaCita = itemView.findViewById(R.id.tvFechaCita);
            tvEstadoCita = itemView.findViewById(R.id.tvEstadoCita);
            tvDoctorNombre = itemView.findViewById(R.id.tvDoctorNombre);
            tvEspecialidadNombre = itemView.findViewById(R.id.tvEspecialidadNombre);
            ivDoctorFoto = itemView.findViewById(R.id.ivDoctorFoto);
            btnTeleconsulta = itemView.findViewById(R.id.btnTeleconsulta);
        }
    }
}
