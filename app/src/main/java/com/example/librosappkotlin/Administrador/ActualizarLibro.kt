package com.example.librosappkotlin.Administrador

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.librosappkotlin.R
import com.example.librosappkotlin.databinding.ActivityActualizarLibroBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActualizarLibro : AppCompatActivity() {

    private lateinit var binding : ActivityActualizarLibroBinding

    private var idLibro = ""

    private lateinit var progressDialog: ProgressDialog

    //Titulo
    private lateinit var catTituloArrayList : ArrayList<String>
    //Id
    private lateinit var catIdArrayList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityActualizarLibroBinding.inflate(layoutInflater)
        setContentView(binding.root)


        idLibro = intent.getStringExtra("idLibro")!!




        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        cargarCategorias()
        cargarInformacion()


        binding.IbRegresar.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.TvCategoriaLibro.setOnClickListener{
            dialogCategoria()
        }

        binding.BtnActualizarLibro.setOnClickListener{

            validarInformacion()
        }

    }

    private fun cargarInformacion() {
        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Obtener la informacion en tiempo real del libro seleccionado
                    val titulo = snapshot.child("titulo").value.toString()
                    val descripcion = snapshot.child("descripcion").value.toString()
                    id_seleccionar = snapshot.child("categoria").value.toString()

                    //seteamos en las vistas
                    binding.EtTituloLibro.setText(titulo)
                    binding.EtDescripcionLibro.setText(descripcion)

                    val refCategoria = FirebaseDatabase.getInstance().getReference("Categorias")
                    refCategoria.child(id_seleccionar)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //Obtener la Categoria
                                val categoria = snapshot.child("categoria").value
                                //seteamos en el textview
                                binding.TvCategoriaLibro.text = categoria.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var titulo = ""
    private var descripcion = ""

    private fun validarInformacion() {
        //Obtener los datos ingresados
        titulo = binding.EtTituloLibro.text.toString().trim()
        descripcion = binding.EtDescripcionLibro.text.toString().trim()

        if (titulo.isEmpty()){
            Toast.makeText(this, "Ingrese Titulo", Toast.LENGTH_SHORT).show()
        }
        else if (descripcion.isEmpty()){
            Toast.makeText(this, "Ingrese Descripcion", Toast.LENGTH_SHORT).show()
        }else if (id_seleccionar.isEmpty()){
            Toast.makeText(this, "Seleccione una Categoria", Toast.LENGTH_SHORT).show()
        }else{
            actualizarInformacion()
        }

    }

    private fun actualizarInformacion() {
        progressDialog.setMessage("Actualizando Informacion")
        progressDialog.show()
        val hashMap = HashMap<String, Any>()
        hashMap["titulo"] = "$titulo"
        hashMap["descripcion"] = "$descripcion"
        hashMap["categoria"] = "$id_seleccionar"

        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro)
            .updateChildren(hashMap)
            .addOnSuccessListener {

                progressDialog.dismiss()
                Toast.makeText(this, "La actualizacion fue exitosa", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "La actualizacion fallo debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var id_seleccionar = ""
    private var titulo_seleccionado = ""


    private fun dialogCategoria() {
        val categoriaArray = arrayOfNulls<String>(catTituloArrayList.size)
        for (i in catTituloArrayList.indices){
            categoriaArray[i] = catTituloArrayList[i]
        }

        val buider = AlertDialog.Builder(this)
        buider.setTitle("Seleccione una categoria")
            .setItems(categoriaArray){dialog, posicion->
                id_seleccionar = catIdArrayList[posicion]
                titulo_seleccionado = catTituloArrayList[posicion]

                binding.TvCategoriaLibro.text = titulo_seleccionado
            }
            .show()

    }

    private fun cargarCategorias() {
        catTituloArrayList = ArrayList()
        catIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                catTituloArrayList.clear()
                catIdArrayList.clear()
                for (ds in snapshot.children){
                    val id = ""+ds.child("id").value
                    val categoria = ""+ds.child("categoria").value

                    catTituloArrayList.add(categoria)
                    catIdArrayList.add(id)

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}