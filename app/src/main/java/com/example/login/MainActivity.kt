package com.example.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database

class MainActivity : AppCompatActivity() {

    // Conexión con Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Enlazar las vistas del diseño
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etClave = findViewById<EditText>(R.id.etClave)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvEstado = findViewById<TextView>(R.id.tvEstado)

        // ---------- REGISTRARSE ----------
        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val clave = etClave.text.toString()

            if (nombre.isEmpty() || correo.isEmpty() || clave.length < 6) {
                tvEstado.text = "Llena todos los campos (clave mínimo 6)"
                return@setOnClickListener
            }

            // 1. Crear el usuario en Authentication
            auth.createUserWithEmailAndPassword(correo, clave)
                .addOnSuccessListener { resultado ->
                    val uid = resultado.user!!.uid

                    // 2. Guardar su perfil en Realtime Database
                    val usuario = mapOf(
                        "nombre" to nombre,
                        "correo" to correo
                    )
                    db.child("usuarios").child(uid).setValue(usuario)
                        .addOnSuccessListener {
                            tvEstado.text = "Usuario registrado: $nombre"
                        }
                        .addOnFailureListener { e ->
                            tvEstado.text = "Error al guardar: ${e.message}"
                        }
                }
                .addOnFailureListener { e ->
                    tvEstado.text = "Error al registrar: ${e.message}"
                }
        }

        // ---------- INGRESAR ----------
        btnIngresar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val clave = etClave.text.toString()

            if (correo.isEmpty() || clave.isEmpty()) {
                tvEstado.text = "Escribe correo y contraseña"
                return@setOnClickListener
            }

            // 1. Verificar correo y contraseña
            auth.signInWithEmailAndPassword(correo, clave)
                .addOnSuccessListener { resultado ->
                    val uid = resultado.user!!.uid

                    // 2. Leer su nombre desde Realtime Database
                    db.child("usuarios").child(uid).child("nombre").get()
                        .addOnSuccessListener { dato ->
                            val nombre = dato.getValue(String::class.java)
                            tvEstado.text = "¡Bienvenido, $nombre!"
                        }
                }
                .addOnFailureListener {
                    tvEstado.text = "Correo o contraseña incorrectos"
                }
        }
    }
}
 