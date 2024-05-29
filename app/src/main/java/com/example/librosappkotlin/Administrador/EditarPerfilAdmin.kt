package com.example.librosappkotlin.Administrador

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.librosappkotlin.R
import com.example.librosappkotlin.databinding.ActivityEditarPerfilAdminBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditarPerfilAdmin : AppCompatActivity() {

    private lateinit var binding : ActivityEditarPerfilAdminBinding

    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var imagenUri : Uri?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilAdminBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        cargarInformacion()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbRegresar.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.FbCambiarImg.setOnClickListener{
            mostrarOpciones()
        }

        binding.BtnActualizarInfo.setOnClickListener{
            validarInformacion()
        }


    }

    private fun mostrarOpciones() {
        val popupMenu = PopupMenu(this, binding.imgPerfilAdmin)
        popupMenu.menu.add(Menu.NONE,0,0,"Galeria")
        popupMenu.menu.add(Menu.NONE,1,1,"Camara")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val id = item.itemId
            if (id == 0){
                //Elegir una imagen de la galeria
                if (ContextCompat.checkSelfPermission(applicationContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    seleccionarImgGaleria()
                }else{
                    permisoGaleria.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }


            }else if(id == 1){
                //Tomar una fotografia
                if (ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    tomarFotografia()
                }else{
                    permisoCamara.launch(android.Manifest.permission.CAMERA)
                }

            }
            true

        }

    }

    private fun tomarFotografia() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Titulo_temp")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_temp")
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        ARLCamara.launch(intent)
    }

    private val ARLCamara = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{resultado->
            if(resultado.resultCode == Activity.RESULT_OK){
                subirImagenStorage()
            }
            else{
                Toast.makeText(applicationContext, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val permisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){Permiso_concedido->
            if (Permiso_concedido){
                tomarFotografia()
            }else{
                Toast.makeText(applicationContext, "El permiso para acceder a la camara del dispositivo no ah sido concedido", Toast.LENGTH_SHORT).show()
            }
        }



    private fun seleccionarImgGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        ARLGaleria.launch(intent)
    }

    private val ARLGaleria = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{resultado ->
            if(resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data

                //binding.imgPerfilAdmin.setImageURI(imagenUri)
                subirImagenStorage()

            }else{
                Toast.makeText(applicationContext, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val permisoGaleria =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){Permiso_concedido->
            if (Permiso_concedido){
                seleccionarImgGaleria()
            }else{
                Toast.makeText(applicationContext, "El permiso para acceder a la galeria no ah sido concedido", Toast.LENGTH_SHORT).show()

            }
        }


    private var nombres = ""
    private fun validarInformacion() {
        nombres = binding.EtANombres.text.toString().trim()
        if (nombres.isEmpty()){
            Toast.makeText(applicationContext, "Ingrese un nuevo nombre", Toast.LENGTH_SHORT).show()
        }else{
            ActualizarInformacion()

        }
    }

    private fun subirImagenStorage() {
        progressDialog.setMessage("Subiendo imagen a Storage")
        progressDialog.show()

        val rutaimagen = "ImagenesPerfilAdministradores/"+firebaseAuth.uid

        val ref = FirebaseStorage.getInstance().getReference(rutaimagen)
        ref.putFile(imagenUri!!)
            .addOnSuccessListener {taskSnapShot->
                val uriTask : Task<Uri> =taskSnapShot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlImagen = "${uriTask.result}"
                subirImagenDatabase(urlImagen)

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImagenDatabase(urlImagen : String){
        progressDialog.setMessage("Actualizando imagen")

        val hashmap : HashMap<String, Any> = HashMap()
        if (imagenUri != null){
            hashmap["imagen"] = urlImagen
        }
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Su imagen se a actualizado", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun ActualizarInformacion() {
        progressDialog.setMessage("Actualizando informacion")
        val hashmap : HashMap<String, Any> = HashMap()
        hashmap["nombres"] = "${nombres}"
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Se actualizo correctamente", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "No se realizo la actualizacion debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarInformacion() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Obtener la info en tiempo real
                    val nombre = "${snapshot.child("nombres").value}"
                    val imagen = "${snapshot.child("imagen").value}"

                    //Setear
                    binding.EtANombres.setText(nombre)

                    try {
                        Glide.with(applicationContext)
                            .load(imagen)
                            .placeholder(R.drawable.ic_img_perfil)
                            .into(binding.imgPerfilAdmin)
                    }catch (e:Exception){
                        Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}