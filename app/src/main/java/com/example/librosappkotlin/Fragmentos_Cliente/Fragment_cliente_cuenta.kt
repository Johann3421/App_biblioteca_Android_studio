package com.example.librosappkotlin.Fragmentos_Cliente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.librosappkotlin.Elegir_rol
import com.example.librosappkotlin.R
import com.example.librosappkotlin.databinding.FragmentClienteCuentaBinding
import com.google.firebase.auth.FirebaseAuth


class Fragment_cliente_cuenta : Fragment() {

    private lateinit var binding :FragmentClienteCuentaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding = FragmentClienteCuentaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.CerrarSessionCliente.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(mContext, Elegir_rol::class.java))
            activity?.finishAffinity()
        }

    }


}