package com.example.alunojiujitsu.activitys.MensalidadePaga

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alunojiujitsu.Adapters.AdapterMensalidadePaga
import com.example.alunojiujitsu.databinding.ActivityMensalidadePagaBinding
import com.example.alunojiujitsu.model.DB
import com.example.alunojiujitsu.model.MensalidadePagaClasse

class MensalidadePaga : AppCompatActivity() {

    lateinit var binding: ActivityMensalidadePagaBinding
    lateinit var adapterMensalidadePaga: AdapterMensalidadePaga
    val lista_mensalidade_paga:MutableList<MensalidadePagaClasse> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensalidadePagaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMensalidadePaga)
        supportActionBar?.title = "Mensalidade Paga"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val recyclerMensalidadePaga = binding.recyclerMensalidadePaga
        recyclerMensalidadePaga.layoutManager = LinearLayoutManager(this)
        recyclerMensalidadePaga.setHasFixedSize(true)
        adapterMensalidadePaga = AdapterMensalidadePaga(this, lista_mensalidade_paga)
        recyclerMensalidadePaga.adapter = adapterMensalidadePaga

        val db = DB()
        db.obterListaMensalidadePaga(lista_mensalidade_paga, adapterMensalidadePaga)


    }
}