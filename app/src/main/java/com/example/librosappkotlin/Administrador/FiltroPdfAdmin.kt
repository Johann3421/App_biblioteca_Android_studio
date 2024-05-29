package com.example.librosappkotlin.Administrador

import android.widget.Filter

class FiltroPdfAdmin : Filter{

    var filtrolist : ArrayList<ModeloPdf>
    var adaptadorPdfAdmin : AdaptadorPdfAdmin

    constructor(filtrolist: ArrayList<ModeloPdf>, adaptadorPdfAdmin: AdaptadorPdfAdmin) {
        this.filtrolist = filtrolist
        this.adaptadorPdfAdmin = adaptadorPdfAdmin
    }

    override fun performFiltering(libro: CharSequence?): FilterResults {
        var libro : CharSequence?= libro
        val resultados = FilterResults()
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

    override fun publishResults(p0: CharSequence?, resultados: FilterResults) {
        adaptadorPdfAdmin.pdfArrayList = resultados.values as ArrayList<ModeloPdf>
        adaptadorPdfAdmin.notifyDataSetChanged()
    }
}