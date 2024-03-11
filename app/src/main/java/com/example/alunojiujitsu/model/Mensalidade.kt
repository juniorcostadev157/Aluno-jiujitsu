package com.example.alunojiujitsu.model

data class Mensalidade(
    val alunoId: String? = null, // ID do aluno
    var id: String? = null, // ID do documento da mensalidade
    val foto: String? = null,
    val data: String? = null,
    val preco: String? = null,
    var titulo_cobranca: String? = null,
    var status_pagamento: String? = null,
    var nome:String? = null,
    var email_aluno:String? = null
)