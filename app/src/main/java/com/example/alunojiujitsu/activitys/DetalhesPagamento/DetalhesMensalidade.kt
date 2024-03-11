package com.example.alunojiujitsu.activitys.DetalhesPagamento

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.activitys.Pagamentos.PagamentoMensalidade
import com.example.alunojiujitsu.databinding.ActivityDetalhesMensalidadeBinding

class DetalhesMensalidade : AppCompatActivity() {
    lateinit var binding: ActivityDetalhesMensalidadeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesMensalidadeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataMensalidade =intent.extras!!.getString("data")
        val precoMensalidade = intent.extras!!.getString("preco")
        val titulo_cobranca = intent.extras!!.getString("titulo_cobranca")
        val status_pagamento = intent.extras!!.getString("status_pagamento")
        val mensadalideID = intent.extras!!.getString("mensalidadeID")
        val nomeAluno = intent.extras!!.getString("nomeAluno")
        val email_aluno = intent.extras!!.getString("email_aluno")
        Log.d("nomeUsuario", "nome do usuario é ${nomeAluno}")
        binding.txtDataDetalhes.setText(dataMensalidade)
        binding.txtPrecoDetalhes.text = "R$ $precoMensalidade"
        binding.txtTituloCobranca.setText(titulo_cobranca)

        binding.buttonEscolherFormadePagamento.setOnClickListener {

            val intent = Intent(this, PagamentoMensalidade::class.java)
            intent.putExtra("data", dataMensalidade)
            intent.putExtra("preco", precoMensalidade)
            intent.putExtra("titulo_cobranca", titulo_cobranca)
            intent.putExtra("status_pagamento", status_pagamento)
            intent.putExtra("mensalidadeID", mensadalideID)
            intent.putExtra("nomeAluno", nomeAluno)
            intent.putExtra("email_aluno", email_aluno)



            startActivity(intent)

        }



    }
}