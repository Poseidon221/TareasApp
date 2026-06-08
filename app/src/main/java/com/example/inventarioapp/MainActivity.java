package com.example.inventarioapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {
    private EditText etCodigo, etDescripcion, etPrecio;
    private Button btnRegistrar, btnBorrar, btnEditar, btnBuscar, btnVerTodos;

    private RecyclerView rvArticulos;
    private ArticuloAdapter adaptador;
    private List<Articulo> listaArticulos;
    private FirebaseFirestore db;
    private android.widget.ProgressBar pbCarga;
    private com.google.android.material.switchmaterial.SwitchMaterial swOferta;

    private ListenerRegistration listenerArticulos;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCodigo = findViewById(R.id.etCodigo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        etPrecio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                String texto = s.toString().trim();

                if (texto.isEmpty()) {
                    etPrecio.setError(null);
                    return;
                }

                try {
                    double precio = Double.parseDouble(texto);

                    if (precio <= 0) {
                        etPrecio.setError("El precio debe ser mayor a 0");
                    } else {
                        etPrecio.setError(null);
                    }

                } catch (NumberFormatException e) {
                    etPrecio.setError("Precio inválido");
                }
            }
        });
        etDescripcion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                String descripcion = s.toString().trim();

                if (descripcion.isEmpty()) {
                    etDescripcion.setError(null);
                    return;
                }

                if (descripcion.length() < 3) {
                    etDescripcion.setError("La descripción debe tener al menos 3 caracteres");
                } else {
                    etDescripcion.setError(null);
                }
            }
        });

        rvArticulos = findViewById(R.id.rvArticulos);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnEditar = findViewById(R.id.btnEditar);
        btnBorrar = findViewById(R.id.btnBorrar);

        pbCarga = findViewById(R.id.pbCarga);
        swOferta = findViewById(R.id.swOferta);
        btnVerTodos = findViewById(R.id.btnVerTodos);
        db = FirebaseFirestore.getInstance();

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarArticuloFirebase();
            }
        });

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarArticuloFirebase();
            }
        });

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificarArticuloFirebase();
            }
        });

        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarArticuloFirebase();
            }
        });

        btnVerTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { filtrarSoloOfertas(); }
        });

        rvArticulos.setLayoutManager(new LinearLayoutManager(this));

        cargarDatosEnTiempoReal();

    }

    private void registrarArticulo(){
        String codigo = etCodigo.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String precio = etPrecio.getText().toString();

        if (!precio.isEmpty()) {
            try {
                double precioValor = Double.parseDouble(precio);

                if (precioValor <= 0) {
                    etPrecio.setError("El precio debe ser mayor a 0");
                    return;
                }
            } catch (NumberFormatException e) {
                etPrecio.setError("Precio inválido");
                return;
            }
        }

        if (!codigo.isEmpty() && !descripcion.isEmpty() && !precio.isEmpty()){

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion.db", null, 1);
            SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

            ContentValues registro = new ContentValues();
            registro.put("codigo", codigo);
            registro.put("descripcion", descripcion);
            registro.put("precio", precio);

            //INSERT INTO articulos (codigo, descripcion, precio) VALUES (123423, "Teclado", 230000.00);
            baseDeDatos.insert("articulos", null, registro);

            cargarListaArticulos();

            if (adaptador != null){
                adaptador.notifyDataSetChanged();
            }

            //Cerrar conexion a la base de datos
            baseDeDatos.close();

            etCodigo.setText("");
            etDescripcion.setText("");
            etPrecio.setText("");

            Toast.makeText(this, "Tarea registrada correctamente", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarArticulo(){
        String codigo = etCodigo.getText().toString();

        if (!codigo.isEmpty()){
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion.db", null, 1);
            SQLiteDatabase baseDeDatos = admin.getReadableDatabase();

            android.database.Cursor fila = baseDeDatos.rawQuery("SELECT descripcion, precio FROM articulos WHERE codigo = " + codigo, null);

            if (fila.moveToFirst()){
                etDescripcion.setText(fila.getString(0));
                etPrecio.setText(fila.getString(1));
                Toast.makeText(this, "Tarea encontrada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "La tarea no existe con ese codigo", Toast.LENGTH_SHORT).show();
                etDescripcion.setText("");
                etPrecio.setText("");
            }

            baseDeDatos.close();
            fila.close();
        } else {
            Toast.makeText(this, "Debes ingresar el codigo de la tarea a buscar", Toast.LENGTH_SHORT).show();
        }
    }

    private void buscarArticuloFirebase(){
        String codigo = etCodigo.getText().toString();

        if(codigo.isEmpty()){
            Toast.makeText(this, "Ingrese el codigo a buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Tareas").document(codigo).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        String descripcion = documentSnapshot.getString("descripcion");
                        Double precio = documentSnapshot.getDouble("precio");
                        Boolean oferta = documentSnapshot.getBoolean("oferta");

                        etDescripcion.setText(descripcion);
                        etPrecio.setText(String.valueOf(precio));

                        if (oferta != null){
                            swOferta.setChecked(oferta);
                        } else {
                            swOferta.setChecked(false);
                        }

                        Toast.makeText(this, "Tarea encontrada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "La tarea no existe", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                   Toast.makeText(this, "Error de conexion", Toast.LENGTH_SHORT).show();
                });
    }

    private void borrarArticulo(){
        String codigo = etCodigo.getText().toString();

        if (!codigo.isEmpty()){
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion.db", null, 1);
            SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

            int cantidadBorrados = baseDeDatos.delete("Tareas", "codigo=" + codigo, null);

            baseDeDatos.close();

            etCodigo.setText("");
            etDescripcion.setText("");
            etPrecio.setText("");

            if (cantidadBorrados == 1){
                Toast.makeText(this, "Tarea eliminada exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "La Tarea no existe", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Ingrese el codigo de la tarea a eliminar", Toast.LENGTH_SHORT).show();
        }
    }

    private void borrarArticuloFirebase(){
        String codigo = etCodigo.getText().toString();

        if (codigo.isEmpty()){
            Toast.makeText(this, "Ingrese el codigo de la tarea a eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Tareas").document(codigo).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    etCodigo.setText("");
                    etDescripcion.setText("");
                    etPrecio.setText("");
                    swOferta.setChecked(false);
                })
                .addOnFailureListener(e -> {
                   Toast.makeText(this, "Error al borrar", Toast.LENGTH_SHORT).show();
                });
    }

    private void modificarArticulo(){
        String codigo = etCodigo.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String precio = etPrecio.getText().toString();

        if (!precio.isEmpty()) {
            try {
                double precioValor = Double.parseDouble(precio);

                if (precioValor <= 0) {
                    etPrecio.setError("El precio debe ser mayor a 0");
                    return;
                }
            } catch (NumberFormatException e) {
                etPrecio.setError("Precio inválido");
                return;
            }
        }

        if (!codigo.isEmpty() && !descripcion.isEmpty() && !precio.isEmpty()){
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion.db", null, 1);
            SQLiteDatabase baseDeDatos = admin.getWritableDatabase();

            ContentValues registroNuevo = new ContentValues();
            registroNuevo.put("codigo", codigo);
            registroNuevo.put("descripcion", descripcion);
            registroNuevo.put("precio", precio);

            int cantidadActualizados = baseDeDatos.update("Tareas", registroNuevo, "codigo=" + codigo, null);

            baseDeDatos.close();

            etCodigo.setText("");
            etDescripcion.setText("");
            etPrecio.setText("");

            if (cantidadActualizados == 1){
                Toast.makeText(this, "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se encontro Tarea para actualizar", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Debes llenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }


    private void modificarArticuloFirebase(){
        String codigo = etCodigo.getText().toString();
        String descripcion = etDescripcion.getText().toString();

        if (descripcion.trim().length() < 3) {
            etDescripcion.setError("La descripción debe tener al menos 3 caracteres");
            return;
        }

        String precio = etPrecio.getText().toString();
        boolean oferta = swOferta.isChecked();

        if (!codigo.isEmpty() && !descripcion.isEmpty() && !precio.isEmpty()){
            db.collection("Tareas").document(codigo)
                    .update(
                            "descripcion", descripcion,
                            "precio", Double.parseDouble(precio),
                            "oferta", oferta
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tarea actualizado", Toast.LENGTH_SHORT).show();
                        etCodigo.setText("");
                        etDescripcion.setText("");
                        etPrecio.setText("");
                        swOferta.setChecked(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Llena todos los campos para editar", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarListaArticulos(){
        //SQLite
        //listaArticulos = new ArrayList<>();

        //AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion.db", null, 1);
        //SQLiteDatabase bd = admin.getReadableDatabase();

        //android.database.Cursor fila = bd.rawQuery("SELECT codigo, descripcion, precio FROM articulos", null);

        //while (fila.moveToNext()){
        //    int codigo = fila.getInt(0);
        //    String descripcion = fila.getString(1);
        //    double precio = fila.getDouble(2);

        //    listaArticulos.add(new Articulo(codigo, descripcion, precio));
        //}

        //bd.close();
        //fila.close();

        //adaptador = new ArticuloAdapter(listaArticulos);

        //rvArticulos.setAdapter(adaptador);

        //Firebase
        listaArticulos = new ArrayList<>();

        db.collection("Tareas")
                .get()
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful()){
                        listaArticulos.clear();

                        for (QueryDocumentSnapshot documento : task.getResult()){
                            Articulo articulo = documento.toObject(Articulo.class);
                            listaArticulos.add(articulo);
                        }

                        adaptador = new ArticuloAdapter(listaArticulos);
                        rvArticulos.setAdapter(adaptador);
                    } else {
                        Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
                    }
                } );
    }

    private void cargarDatosEnTiempoReal(){
        listaArticulos = new ArrayList<>();
        adaptador = new ArticuloAdapter(listaArticulos);
        rvArticulos.setAdapter(adaptador);

        listenerArticulos = db.collection("Tareas")
                .addSnapshotListener( (value, error) -> {
                    if(error != null){
                        Toast.makeText(this, "Fallo al escuchar los cambios", Toast.LENGTH_SHORT).show();
                    }

                    if (value != null){
                        listaArticulos.clear();

                        for (QueryDocumentSnapshot documento : value){
                            Articulo articulo = documento.toObject(Articulo.class);
                            listaArticulos.add(articulo);
                        }

                        adaptador.notifyDataSetChanged();
                    }
                } );
    }

    private void registrarArticuloFirebase(){
        String codigo = etCodigo.getText().toString();
        String descripcion = etDescripcion.getText().toString();

        if (descripcion.trim().length() < 3) {
            etDescripcion.setError("La descripción debe tener al menos 3 caracteres");
            return;
        }

        String precio = etPrecio.getText().toString();

        boolean estaEnOferta = swOferta.isChecked();

        if (!codigo.isEmpty() && !descripcion.isEmpty() && !precio.isEmpty()){

            pbCarga.setVisibility(View.VISIBLE);
            btnRegistrar.setEnabled(false);

            Map<String, Object> articuloMap = new HashMap<>();
            articuloMap.put("codigo", Integer.parseInt(codigo));
            articuloMap.put("descripcion", descripcion);
            articuloMap.put("precio", Double.parseDouble(precio));
            articuloMap.put("oferta", estaEnOferta);

            db.collection("Tareas").document(codigo)
                    .set(articuloMap)
                    .addOnSuccessListener(aVoid -> {
                        pbCarga.setVisibility(View.GONE);
                        btnRegistrar.setEnabled(true);
                        Toast.makeText(MainActivity.this, "Tarea guardada correctamente", Toast.LENGTH_SHORT).show();
                        etCodigo.setText("");
                        etDescripcion.setText("");
                        etPrecio.setText("");
                        swOferta.setChecked(false);
                    })
                    .addOnFailureListener(e -> {
                        pbCarga.setVisibility(View.GONE);
                        btnRegistrar.setEnabled(true);
                        Toast.makeText(MainActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Llena todos los campos del formulario", Toast.LENGTH_SHORT).show();
        }
    }

    private void filtrarSoloOfertas() {
        db.collection("Tareas")
                .whereEqualTo("oferta", true)
                .whereGreaterThan("precio", 100000)
                .addSnapshotListener((value, error) -> {
                    if (error != null){
                        return;
                    }

                    if (value != null){
                        listaArticulos.clear();

                        for (QueryDocumentSnapshot documento: value){
                            listaArticulos.add(documento.toObject(Articulo.class));
                        }

                        adaptador.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (listenerArticulos != null) {
            listenerArticulos.remove();
        }
    }
}

