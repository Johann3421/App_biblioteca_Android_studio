package com.example.librosappkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.librosappkotlin.Fragmentos_Admin.Fragment_admin_cuenta
import com.example.librosappkotlin.Fragmentos_Admin.Fragment_admin_dashboard
import com.example.librosappkotlin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        ComprobarSession()
        VerFragmentoDashboard()


        binding.BottomNvAdmin.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.Menu_Panel->{
                    VerFragmentoDashboard()
                    true
                }
                R.id.Menu_Cuenta->{
                    VerFragmentoCuenta()
                    true
                }
                else->{
                    false
                }
            }
        }

    }

    private fun VerFragmentoDashboard(){
        var nombre_titulo= "Dashboard"
        binding.TitutloRLAdmin.text = nombre_titulo

        val fragment = Fragment_admin_dashboard()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentsAdmin.id, fragment, "Fragment Dashboard")
        fragmentTransaction.commit()
    }

    private fun VerFragmentoCuenta(){
        var nombre_titulo= "Mi Cuenta"
        binding.TitutloRLAdmin.text = nombre_titulo

        val fragment = Fragment_admin_cuenta()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.FragmentsAdmin.id, fragment, "Fragment mi cuenta")
        fragmentTransaction.commit()
    }

    private fun ComprobarSession(){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            startActivity(Intent(this,Elegir_rol::class.java))
            finishAffinity()
        }else{
            /*Toast.makeText(applicationContext, "Bienvenido(a) ${firebaseUser.email}", Toast.LENGTH_SHORT).show()*/

        }
    }
}