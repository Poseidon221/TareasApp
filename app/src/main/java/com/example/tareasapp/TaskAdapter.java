package com.example.tareasapp;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private ArrayList<Task> list;

    public TaskAdapter(ArrayList<Task> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, descripcion, estado;
        Button btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.txtTitulo);
            descripcion = itemView.findViewById(R.id.txtDescripcion);
            estado = itemView.findViewById(R.id.txtEstado);

            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Task task = list.get(position);

        holder.titulo.setText(task.getTitulo());
        holder.descripcion.setText(task.getDescripcion());
        holder.estado.setText(task.getEstado());

        // ELIMINAR TAREA
        holder.btnEliminar.setOnClickListener(v -> {

            list.remove(position);

            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());
        });

        // EDITAR TAREA
        holder.btnEditar.setOnClickListener(v -> {

            View dialogView = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.dialog_edit_task, null);

            EditText etTitulo = dialogView.findViewById(R.id.etEditarTitulo);
            EditText etDescripcion = dialogView.findViewById(R.id.etEditarDescripcion);

            etTitulo.setText(task.getTitulo());
            etDescripcion.setText(task.getDescripcion());

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Editar tarea")
                    .setView(dialogView)
                    .setPositiveButton("Guardar", (dialog, which) -> {

                        task.setTitulo(etTitulo.getText().toString());
                        task.setDescripcion(etDescripcion.getText().toString());

                        notifyItemChanged(position);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}