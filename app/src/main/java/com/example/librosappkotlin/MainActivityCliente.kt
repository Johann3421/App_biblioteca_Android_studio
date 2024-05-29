package com.example.librosappkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.librosappkotlin.Fragmentos_Cliente.Fragment_cliente_cuenta
import com.example.librosappkotlin.Fragmentos_Cliente.Fragment_cliente_favoritos
import com.example.librosappkotlin.databinding.ActivityMainClienteBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivityCliente : AppCompatActivity() {

    private lateinit var binding : ActivityMainClienteBinding

    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSession()

        verFragmentoDashboard()

        binding.BottomNavCliente.setOnItemSelectedListener {item->
            when(item.itemId){
                R.id.Menu_dashboard_cl->{
                    verFragmentoDashboard()
                    true
                }
                R.id.Menu_favoritos_cl->{
                    verFragmentoFavoritos()
                    true
                }
                R.id.Menu_cuenta_cl->{
                    verFragmentoCuenta()
                    true
                }
                else->{
                    false
                }
            }
        }

    }

    private fun comprobarSession(){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            startActivity(Intent(this,Elegir_rol::class.java))
            finishAffinity()
        }else{
            Toast.makeText(applicationContext, "Bienvenido(a) ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verFragmentoDashboard(){
        val nombre_titulo = "Dashboard"
        binding.TituloRlCliente.text = nombre_titulo
        val fragment = Fragment_cliente_cuenta()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsCliente.id,fragment,"Fragment dashboard")
        fragmentTransaction.commit()

    }

    private fun verFragmentoFavoritos(){
        val nombre_titulo = "Favoritos"
        binding.TituloRlCliente.text = nombre_titulo
        val fragment = Fragment_cliente_favoritos()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsCliente.id,fragment,"Fragment favoritos")
        fragmentTransaction.commit()

    }

    private fun verFragmentoCuenta(){
        val nombre_titulo = "Cuenta"
        binding.TituloRlCliente.text = nombre_titulo
        val fragment = Fragment_cliente_cuenta()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsCliente.id,fragment,"Fragment cuenta")
        fragmentTransaction.commit()
    }


















}