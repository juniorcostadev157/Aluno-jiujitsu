package com.example.alunojiujitsu.activitys.TelaMensalidade

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.alunojiujitsu.Adapters.AdapterMensalidade
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.databinding.ActivityTelaMensalidadeBinding
import com.example.alunojiujitsu.databinding.MensalidadeItemBinding
import com.example.alunojiujitsu.model.DB
import com.example.alunojiujitsu.model.Mensalidade
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class TelaMensalidade : AppCompatActivity() {

    lateinit var binding: ActivityTelaMensalidadeBinding
    lateinit var adapterMensalidade:AdapterMensalidade
    var lista_mensalidade: MutableList<Mensalidade> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaMensalidadeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar2)
        supportActionBar?.title = "Mensalidade"

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)






        val recycler_mensalidade = binding.recyclerMensalidade
        recycler_mensalidade.layoutManager = GridLayoutManager(this, 2)
        recycler_mensalidade.setHasFixedSize(true)
        adapterMensalidade = AdapterMensalidade(this, lista_mensalidade)
        recycler_mensalidade.adapter = adapterMensalidade
        val db = DB()

        val alunoId = FirebaseAuth.getInstance().currentUser?.uid
        if (alunoId != null) {
            db.obterListaMensalidadesDoAluno(alunoId, lista_mensalidade, adapterMensalidade)
        } else {
            // Trate o caso em que o ID do aluno não está disponível
        }

        val usuarioID = FirebaseAuth.getInstance().currentUser?.uid
        usuarioID?.let {
            db.escutarMudancasMensalidadePaga(it)
        }


    }







}