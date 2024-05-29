package com.example.librosappkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.librosappkotlin.Administrador.Registrar_admin
import com.example.librosappkotlin.Cliente.Registro_Cliente
import com.example.librosappkotlin.databinding.ActivityElegirRolBinding
import com.example.librosappkotlin.databinding.ActivityMainBinding

class Elegir_rol : AppCompatActivity() {

    private lateinit var binding: ActivityElegirRolBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElegirRolBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.BtnRolAdministrador.setOnClickListener {
            //Toast.makeText(applicationContext, "Rol Administrador",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@Elegir_rol,Registrar_admin::class.java))
        }

        binding.BtnRolCliente.setOnClickListener {
            startActivity(Intent(this@Elegir_rol, Registro_Cliente::class.java))

        }

    }
}