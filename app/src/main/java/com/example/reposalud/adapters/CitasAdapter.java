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

        // Doctor Info
        if (cita.medico != null && cita.medico.usuario != null) {
            holder.tvDoctorNombre.setText("Dr. " + cita.medico.usuario.nombre + " " + cita.medico.usuario.apellido);
            
            if (cita.medico.especialidad != null) {
                holder.tvEspecialidadNombre.setText(cita.medico.especialidad.nombre);
            } else {
                holder.tvEspecialidadNombre.setText("Especialista");
            }

            // Cargar imagen
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
            holder.tvEstadoCita.getBackground().setTint(Color.parseColor("#E8F5E9"));
        } else if (estado.equals("CANCELADA")) {
            holder.tvEstadoCita.setTextColor(Color.parseColor("#D32F2F"));
            holder.tvEstadoCita.setBackgroundResource(R.drawable.bg_pill_selected);
            holder.tvEstadoCita.getBackground().setTint(Color.parseColor("#FFEBEE"));
        } else {
            holder.tvEstadoCita.setTextColor(Color.parseColor("#1976D2"));
            holder.tvEstadoCita.setBackgroundResource(R.drawable.bg_pill_selected);
            holder.tvEstadoCita.getBackground().setTint(Color.parseColor("#E3F2FD"));
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

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaCita = itemView.findViewById(R.id.tvFechaCita);
            tvEstadoCita = itemView.findViewById(R.id.tvEstadoCita);
            tvDoctorNombre = itemView.findViewById(R.id.tvDoctorNombre);
            tvEspecialidadNombre = itemView.findViewById(R.id.tvEspecialidadNombre);
            ivDoctorFoto = itemView.findViewById(R.id.ivDoctorFoto);
        }
    }
}
