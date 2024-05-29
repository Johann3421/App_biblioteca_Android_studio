package com.example.librosappkotlin.Administrador

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.librosappkotlin.R
import com.example.librosappkotlin.databinding.ActivityListaPdfAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ListaPdfAdmin : AppCompatActivity() {

    private lateinit var binding : ActivityListaPdfAdminBinding

    private var idCategoria = ""
    private var tituloCategoria = ""

    private lateinit var pdfArrayList : ArrayList<ModeloPdf>
    private lateinit var adaptadorPdfAdmin : AdaptadorPdfAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityListaPdfAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        idCategoria = intent.getStringExtra("idCategoria")!!
        tituloCategoria = intent.getStringExtra("tituloCategoria")!!

        binding.TxtCategoriaLibro.text = tituloCategoria
        binding.IbRegresar.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        ListarLibros()

        binding.EtBuscarLibroAdmin.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(libro: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adaptadorPdfAdmin.filter.filter(libro)
                }catch (e:Exception){

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun ListarLibros() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.orderByChild("categoria").equalTo(idCategoria)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        pdfArrayList.clear()
                        for (ds in snapshot.children) {
                            val modelo = ds.getValue(ModeloPdf::class.java)
                            if (modelo != null) {
                                pdfArrayList.add(modelo)
                            }
                        }
                        obtenerUrlsPdf(pdfArrayList)
                    } catch (e: Exception) {
                        Log.e("Error", "Error al procesar los datos", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", "Error al obtener datos de Firebase", error.toException())
                }
            })
    }
    private fun obtenerUrlsPdf(pdfArrayList: ArrayList<ModeloPdf>) {
        val storageReference = FirebaseStorage.getInstance().reference
        for (modelo in pdfArrayList) {
            val pdfRef = storageReference.child(modelo.titulo) // Asumiendo que tienes un campo 'nombreArchivo' que contiene el nombre del archivo PDF en Firebase Storage
            pdfRef.downloadUrl.addOnSuccessListener { uri ->
                modelo.url = uri.toString() // Asignar la URL del PDF al modelo
            }.addOnFailureListener { exception ->
                // Manejar el error
                Log.e("Error", "Error al obtener la URL del PDF", exception)
            }
        }
        configurarAdaptador(pdfArrayList)
    }

    private fun configurarAdaptador(pdfArrayList: ArrayList<ModeloPdf>) {
        adaptadorPdfAdmin = AdaptadorPdfAdmin(this, pdfArrayList)
        binding.RvLibrosAdmin.adapter = adaptadorPdfAdmin
    }
}