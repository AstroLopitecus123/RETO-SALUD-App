package com.example.reposalud.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reposalud.R;
import com.example.reposalud.database.DataBaseHelper;
import com.example.reposalud.utils.NavigationHelper;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadesActivity extends AppCompatActivity {

    private RecyclerView rvEspecialidades;
    private DataBaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_especialidades);

        dbHelper = new DataBaseHelper(this);
        rvEspecialidades = findViewById(R.id.rvEspecialidades);
        rvEspecialidades.setLayoutManager(new LinearLayoutManager(this));

        cargarEspecialidades();

        // Configurar Bottom Navigation
        NavigationHelper.setupBottomNavigation(this);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEspecialidades();
    }

    private void cargarEspecialidades() {
        List<Especialidad> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, nombre FROM especialidades", null);

        if (cursor.moveToFirst()) {
            do {
                lista.add(new Especialidad(
                    cursor.getInt(0),
                    cursor.getString(1)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        rvEspecialidades.setAdapter(new EspecialidadAdapter(lista));
    }

    private static class Especialidad {
        int id;
        String nombre;
        Especialidad(int id, String nombre) { this.id = id; this.nombre = nombre; }
    }

    private class EspecialidadAdapter extends RecyclerView.Adapter<EspecialidadAdapter.ViewHolder> {
        private List<Especialidad> especialidades;

        EspecialidadAdapter(List<Especialidad> especialidades) {
            this.especialidades = especialidades;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_especialidad, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Especialidad e = especialidades.get(position);
            holder.tvNombre.setText(e.nombre);

            // Asignar iconos según el nombre
            int iconRes = R.drawable.ic_medical_cross;
            if (e.nombre.contains("Cardio")) iconRes = R.drawable.ic_heart;
            else if (e.nombre.contains("Pedia")) iconRes = R.drawable.ic_baby;
            else if (e.nombre.contains("Gineco")) iconRes = R.drawable.ic_female_health;
            
            holder.ivIcon.setImageResource(iconRes);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(EspecialidadesActivity.this, DetalleEspecialidadActivity.class);
                intent.putExtra("especialidad_id", e.id);
                intent.putExtra("especialidad_nombre", e.nombre);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return especialidades.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre;
            ImageView ivIcon;
            ViewHolder(View v) {
                super(v);
                tvNombre = v.findViewById(R.id.tvEspecialidadNombre);
                ivIcon = v.findViewById(R.id.ivEspecialidadIcon);
            }
        }
    }
}
