package com.example.librosappkotlin.Administrador

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.librosappkotlin.R
import com.example.librosappkotlin.databinding.ActivityEditarPerfilAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditarPerfilAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        cargarInformacion()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.FbCambiarImg.setOnClickListener {
            mostrarOpciones()
        }

        binding.BtnActualizarInfo.setOnClickListener {
            validarInformacion()
        }
    }

    private fun mostrarOpciones() {
        val popupMenu = PopupMenu(this, binding.imgPerfilAdmin)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Galeria")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camara")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                0 -> {
                    // Elegir una imagen de la galeria
                    if (checkAndRequestPermissionForStorage()) {
                        seleccionarImgGaleria()
                    }
                }
                1 -> {
                    // Tomar una fotografia
                    if (checkAndRequestPermission(Manifest.permission.CAMERA)) {
                        tomarFotografia()
                    }
                }
            }
            true
        }
    }

    private fun checkAndRequestPermission(permission: String): Boolean {
        return if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            when (permission) {
                Manifest.permission.READ_EXTERNAL_STORAGE -> permisoGaleria.launch(permission)
                Manifest.permission.CAMERA -> permisoCamara.launch(permission)
            }
            false
        }
    }

    private fun checkAndRequestPermissionForStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
            val permissionsToRequest = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (permissionsToRequest.isEmpty()) {
                true
            } else {
                permisoGaleriaMultiple.launch(permissionsToRequest.toTypedArray())
                false
            }
        } else {
            checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun tomarFotografia() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Titulo_temp")
            put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_temp")
        }
        imagenUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri)
        ARLCamara.launch(intent)
    }

    private val ARLCamara = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                subirImagenStorage()
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val permisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permisoConcedido ->
            if (permisoConcedido) {
                tomarFotografia()
            } else {
                Toast.makeText(this, "El permiso para acceder a la camara del dispositivo no ha sido concedido", Toast.LENGTH_SHORT).show()
            }
        }

    private fun seleccionarImgGaleria() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        ARLGaleria.launch(intent)
    }

    private val ARLGaleria = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                imagenUri = resultado.data?.data
                subirImagenStorage()
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val permisoGaleria =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permisoConcedido ->
            if (permisoConcedido) {
                seleccionarImgGaleria()
            } else {
                Toast.makeText(this, "El permiso para acceder a la galeria no ha sido concedido", Toast.LENGTH_SHORT).show()
            }
        }

    private val permisoGaleriaMultiple =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                seleccionarImgGaleria()
            } else {
                Toast.makeText(this, "El permiso para acceder a la galeria no ha sido concedido", Toast.LENGTH_SHORT).show()
            }
        }

    private fun validarInformacion() {
        val nombres = binding.EtANombres.text.toString().trim()
        if (nombres.isEmpty()) {
            Toast.makeText(this, "Ingrese un nuevo nombre", Toast.LENGTH_SHORT).show()
        } else {
            actualizarInformacion(nombres)
        }
    }

    private fun actualizarInformacion(nombres: String) {
        progressDialog.setMessage("Actualizando informacion")
        val hashmap = hashMapOf<String, Any>("nombres" to nombres)
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Se actualizo correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "No se realizo la actualizacion debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImagenStorage() {
        progressDialog.setMessage("Subiendo imagen a Storage")
        progressDialog.show()

        val rutaImagen = "ImagenesPerfilAdministradores/${firebaseAuth.uid}"
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.putFile(imagenUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                uriTask.addOnSuccessListener { uri ->
                    subirImagenDatabase(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImagenDatabase(urlImagen: String) {
        progressDialog.setMessage("Actualizando imagen")
        val hashmap = hashMapOf<String, Any>("imagen" to urlImagen)
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Su imagen se ha actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarInformacion() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombres").value.toString()
                val imagen = snapshot.child("imagen").value.toString()

                binding.EtANombres.setText(nombre)

                Glide.with(this@EditarPerfilAdmin)
                    .load(imagen)
                    .placeholder(R.drawable.ic_img_perfil)
                    .into(binding.imgPerfilAdmin)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditarPerfilAdmin, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
