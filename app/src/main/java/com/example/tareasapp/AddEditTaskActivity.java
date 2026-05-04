package com.example.tareasapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditTaskActivity extends AppCompatActivity {

    EditText etTitulo, etDescripcion;
    Button btnGuardar;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);

        db = new DBHelper(this);

        btnGuardar.setOnClickListener(v -> {

            String titulo = etTitulo.getText().toString();
            String descripcion = etDescripcion.getText().toString();

            if (titulo.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            db.insertTask(titulo, descripcion, "Pendiente");

            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show();
            finish(); // vuelve a MainActivity
        });
    }
}
