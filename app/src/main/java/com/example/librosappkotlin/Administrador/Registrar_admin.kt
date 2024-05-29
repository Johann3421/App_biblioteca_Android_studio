package com.example.librosappkotlin.Administrador

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.librosappkotlin.MainActivity
import com.example.librosappkotlin.R
import com.example.librosappkotlin.databinding.ActivityRegistrarAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class Registrar_admin : AppCompatActivity() {

    private lateinit var binding : ActivityRegistrarAdminBinding

    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var progresDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrarAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progresDialog = ProgressDialog(this)
        progresDialog.setTitle("Espere por favor")
        progresDialog.setCanceledOnTouchOutside(false)

        binding.IbRegresar.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        binding.BtnRegistrarAdmin.setOnClickListener {
            ValidarInformacion()
        }
        binding.TxtTengoCuenta.setOnClickListener {
            startActivity(Intent(this@Registrar_admin, Login_Admin::class.java))
        }






    }
    var nombres = ""
    var correo = ""
    var contraseña = ""
    var r_contraseña = ""

    private fun ValidarInformacion() {
        nombres = binding.EtNombresAdmin.text.toString().trim()
        correo = binding.EtEmailAdmin.text.toString().trim()
        contraseña = binding.EtPasswordAdmin.text.toString().trim()
        r_contraseña = binding.EtRPasswordAdmin.text.toString().trim()

        if (nombres.isEmpty()){
            binding.EtNombresAdmin.error = "Ingrese Nombres"
            binding.EtNombresAdmin.requestFocus()
        }
        else if (correo.isEmpty()){
            binding.EtEmailAdmin.error = "Ingrese Email"
            binding.EtEmailAdmin.requestFocus()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            binding.EtEmailAdmin.error = "Email no valido"
            binding.EtEmailAdmin.requestFocus()
        }
        else if (contraseña.isEmpty()){
            binding.EtPasswordAdmin.error = "Ingrese la contraseña"
            binding.EtPasswordAdmin.requestFocus()
        }
        else if (contraseña.length<6){
            binding.EtPasswordAdmin.error = "La contraseña debe tener mas de 6 caracteres"
            binding.EtPasswordAdmin.requestFocus()
        }
        else if (r_contraseña.isEmpty()){
            binding.EtRPasswordAdmin.error = "Repita la contraseña"
            binding.EtRPasswordAdmin.requestFocus()
        }
        else if (contraseña != r_contraseña){
            binding.EtRPasswordAdmin.error = "Las contraseñas no coinciden"
            binding.EtRPasswordAdmin.requestFocus()
        }
        else{
            CrearCuentaAdmin(correo,contraseña)
        }





    }

    private fun CrearCuentaAdmin(correo: String, contraseña: String) {
        progresDialog.setMessage("Creando cuenta")
        progresDialog.dismiss()

        firebaseAuth.createUserWithEmailAndPassword(correo, contraseña)
            .addOnSuccessListener {
                AgregarInfoBd()
            }
            .addOnFailureListener{e->
                progresDialog.dismiss()
                Toast.makeText(applicationContext, "Ha fallado la creacion de la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun AgregarInfoBd(){
        progresDialog.setMessage("Guardando informacion..")
        val tiempo = System.currentTimeMillis()
        val uid = firebaseAuth.uid

        val datos_admin : HashMap<String, Any?> = HashMap()
        datos_admin["uid"] = uid
        datos_admin["nombres"] = nombres
        datos_admin["email"] = correo
        datos_admin["rol"] = "admin"
        datos_admin["tiempo_registro"] = tiempo
        datos_admin["imagen"] = ""

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uid!!)
            .setValue(datos_admin)
            .addOnSuccessListener {
                progresDialog.dismiss()
                Toast.makeText(applicationContext, "Cuenta creada", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener{e->
                progresDialog.dismiss()
                Toast.makeText(applicationContext, "No se pudo guardar la informacion debido a ${e.message}", Toast.LENGTH_SHORT).show()

            }

    }
}