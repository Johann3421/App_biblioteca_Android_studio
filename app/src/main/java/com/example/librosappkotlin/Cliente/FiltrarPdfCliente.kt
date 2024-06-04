package com.example.librosappkotlin.Cliente

import android.widget.Filter
import com.example.librosappkotlin.Administrador.ModeloPdf

class FiltrarPdfCliente : Filter {


    var filtrolist : ArrayList<ModeloPdf>
    var adaptadorPdfCliente : AdaptadorPdfCliente

    constructor(filtrolist: ArrayList<ModeloPdf>, adaptadorPdfCliente: AdaptadorPdfCliente) {
        this.filtrolist = filtrolist
        this.adaptadorPdfCliente = adaptadorPdfCliente
    }

    override fun performFiltering(libro: CharSequence?): Filter.FilterResults {
        var libro : CharSequence?= libro
        val resultados = Filter.FilterResults()
        if (libro!= null && libro.isNotEmpty()){
            libro = libro.toString().lowercase()
            val modeloFiltrado : ArrayList<ModeloPdf> = ArrayList()
            for (i in filtrolist.indices){
                if (filtrolist[i].titulo.lowercase().contains(libro)){

                    modeloFiltrado.add(filtrolist[i])

                }
            }
            resultados.count = modeloFiltrado.size
            resultados.values = modeloFiltrado
        }
        else{
            resultados.count = filtrolist.size
            resultados.values = filtrolist
        }
        return resultados
    }

    override fun publishResults(p0: CharSequence?, resultados: Filter.FilterResults) {
        adaptadorPdfCliente.pdfArrayList = resultados.values as ArrayList<ModeloPdf>
        adaptadorPdfCliente.notifyDataSetChanged()
    }
}