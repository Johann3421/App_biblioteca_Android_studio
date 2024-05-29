package com.example.librosappkotlin.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.librosappkotlin.MainActivityCliente
import com.example.librosappkotlin.R
import com.example.librosappkotlin.databinding.ActivityRegistroClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Registro_Cliente : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroClienteBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroClienteBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.IbRegresar.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnRegistrarCliente.setOnClickListener{
            validarInformacion()
        }

    }

    var nombres = ""
    var edad = ""
    var email = ""
    var password = ""
    var r_password = ""
    private fun validarInformacion() {
        nombres = binding.EtNombresCl.text.toString().trim()
        edad = binding.EtEdadCl.text.toString().trim()
        email = binding.EtEmailCl.text.toString().trim()
        password = binding.EtPasswordCl.text.toString().trim()
        r_password = binding.EtRPasswordCl.text.toString().trim()

        if (nombres.isEmpty()){
            binding.EtNombresCl.error = "Ingrese nombres"
            binding.EtNombresCl.requestFocus()
        }
        else if (edad.isEmpty()){
            binding.EtEdadCl.error = "Ingrese la edad"
            binding.EtEdadCl.requestFocus()
        }
        else if (email.isEmpty()){
            binding.EtEmailCl.error = "Ingrese un correo"
            binding.EtEmailCl.requestFocus()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.EtEmailCl.error = "Correo no valido"
            binding.EtEmailCl.requestFocus()
        }
        else if (password.isEmpty()){
            binding.EtPasswordCl.error = "Ingrese una contraseña"
            binding.EtPasswordCl.requestFocus()
        }
        else if (password.length<6){
            binding.EtPasswordCl.error = "Debe tener mas de 6 caracteres"
            binding.EtPasswordCl.requestFocus()
        }
        else if (r_password.isEmpty()){
            binding.EtRPasswordCl.error = "Confirme contraseña"
            binding.EtRPasswordCl.requestFocus()
        }

        else if (r_password != password){
            binding.EtRPasswordCl.error = "Las contraseñas no coinciden"
            binding.EtRPasswordCl.requestFocus()
        }
        else{
            crearCuentaCliente(email,password)
        }





    }

    private fun crearCuentaCliente(email: String, password: String) {
        progressDialog.setMessage("Creando Cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                agregarInfoBD()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Ha fallado el registro del cliente debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }






    }

    private fun agregarInfoBD() {

        progressDialog.setMessage("Guardando Informacion")


        val tiempo = System.currentTimeMillis()

        val uid = firebaseAuth.uid!!

        val datos_cliente : HashMap<String, Any> = HashMap()

        datos_cliente["uid"] = uid
        datos_cliente["nombre"] = nombres
        datos_cliente["edad"] = edad
        datos_cliente["email"] = email
        datos_cliente["rol"] = "cliente"
        datos_cliente["tiempo_registro"] = tiempo
        datos_cliente["imagen"] = ""

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")

        reference.child(uid)
            .setValue(datos_cliente)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Se ha creado su cuenta", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivityCliente::class.java))
                finishAffinity()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Ha fallado el registro del cliente debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }





    }
}