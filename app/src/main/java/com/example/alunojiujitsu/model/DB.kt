package com.example.alunojiujitsu.model

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.alunojiujitsu.Adapters.AdapterMensalidade
import com.example.alunojiujitsu.Adapters.AdapterMensalidadePaga
import com.example.alunojiujitsu.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import java.util.UUID

class DB {
    private val db = FirebaseFirestore.getInstance()

    fun salvarAlunosBancoDados(
        nome: String,
        email: String,
        idade: String,
        faixa: String,
        grau: String,
        descricao_medica: String
    ) {
        val authID = FirebaseAuth.getInstance().currentUser?.uid // Pega o ID do usuário autenticado
        val db = FirebaseFirestore.getInstance()

        // Se você quiser que o documento do aluno tenha o mesmo ID que o authID, use-o diretamente ao invés de criar um novo.
        val alunoRef = db.collection("Alunos").document(authID ?: throw NullPointerException("UID is null"))

        val alunoData = hashMapOf(
            "authID" to authID,
            "nome" to nome,
            "email" to email,
            "idade" to idade,
            "faixa" to faixa,
            "grau" to grau,
            "descricao_medica" to descricao_medica
        )

        alunoRef.set(alunoData).addOnSuccessListener {
            Log.d("Firestore", "Sucesso ao salvar no banco")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Erro ao salvar no banco", e)
        }
    }
    fun salvarTokenNoFirestore(token: String) {
        val idUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: return // Retorna se o usuário não estiver logado
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf("fcmToken" to token)

        // Salva o token na coleção 'Professores', no documento específico do usuário
        db.collection("Alunos").document(idUsuario)
            .set(data, SetOptions.merge()) // Usando SetOptions.merge() para atualizar apenas o token sem sobrescrever outros campos
            .addOnCompleteListener {

            }.addOnFailureListener {

            }
    }




    fun recuperarDadosAlunos(
        nomeAluno: TextView,
        emailAluno: TextView,
        faixaAluno: TextView,
        grausAluno: TextView,
        idadeAluno: TextView,
        restricoesMedicas: TextView,
        image: ImageView
    ) {
        val usuarioID = FirebaseAuth.getInstance().currentUser!!.uid
        val email = FirebaseAuth.getInstance().currentUser!!.email
        val db = FirebaseFirestore.getInstance()
        val documentosReferencia: DocumentReference = db.collection("Alunos").document(usuarioID)

        documentosReferencia.addSnapshotListener { documento, error ->
            if (documento != null) {
                nomeAluno.text = documento.getString("nome")
                emailAluno.text = email

                val faixaVerificador = documento.getString("faixa")
                faixaAluno.text = faixaVerificador ?: ""

                val grauVerificador = documento.getString("grau")
                grausAluno.text = grauVerificador ?: ""

                idadeAluno.text = documento.getString("idade")
                restricoesMedicas.text = documento.getString("descricao_medica")

                // Aqui você chama a função para definir a imagem da faixa com base na faixa e no grau
                setImageBasedOnBeltAndDegree(faixaVerificador, grauVerificador, image)
            }

        }

    }


    fun obterListaMensalidadesDoAluno(alunoId: String, lista_mensalidade: MutableList<Mensalidade>, adapterMensalidade: AdapterMensalidade) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Alunos").document(alunoId).collection("Mensalidades")
            .get()
            .addOnSuccessListener { resultado ->
                lista_mensalidade.clear() // Limpa a lista atual
                val mensalidadesTemp = mutableListOf<Mensalidade>()

                for (documento in resultado) {
                    val mensalidade = documento.toObject(Mensalidade::class.java).apply {
                        this.id = documento.id // Captura o ID do documento da mensalidade, se necessário
                        val usuarioID = FirebaseAuth.getInstance().currentUser!!.uid

                        val db = FirebaseFirestore.getInstance()
                        val documentosReferencia: DocumentReference = db.collection("Alunos").document(usuarioID)
                        documentosReferencia.addSnapshotListener { value, error ->
                            if (value != null) {
                              nome = value.getString("nome")
                              email_aluno = value.getString("email")


                            }
                        }
                    }
                    // Só adiciona à lista temporária se o status não for "Aprovado"
                    if (mensalidade.status_pagamento != "Aprovado") {
                        mensalidadesTemp.add(mensalidade)
                    }
                }

                // Ordena as mensalidades pela data
                val mensalidadesOrdenadas = mensalidadesTemp.sortedBy { mensalidade ->
                    mensalidade.data?.let { converterDataParaOrdenacao(it) } ?: ""
                }

                lista_mensalidade.addAll(mensalidadesOrdenadas)
                adapterMensalidade.notifyDataSetChanged() // Atualiza o adaptador
            }
            .addOnFailureListener { e ->
                Log.d("Mensalidade", "Erro ao listar mensalidades", e)
            }
    }


    fun atualizarStatusMensalidadeAluno(usuarioID: String, mensalidadeID: String, novoStatus: String) {
        val db = FirebaseFirestore.getInstance()
        val mensalidadeRef = db.collection("Alunos").document(usuarioID)
            .collection("Mensalidades").document(mensalidadeID)

        mensalidadeRef.update("status_pagamento", novoStatus).addOnSuccessListener {
            Log.d("DB", "Mensalidade atualizada com sucesso")
        }.addOnFailureListener { e ->
            Log.e("DB", "Erro ao atualizar mensalidade", e)
        }
    }

    fun escutarMudancasMensalidadePaga(usuarioID: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Mensalidade_Aluno").document(usuarioID)
            .collection("Mensalidade_Paga")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("DB", "Ouvir falhou.", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when(dc.type) {
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("DB", "Mensalidade_Paga modificada: ${dc.document.data}")
                            val status = dc.document.getString("status_pagamento")
                            if (status == "Aprovado") {
                                // AQUI você precisa ter o ID da mensalidade correspondente na coleção "Mensalidades"
                                // Isso pode ser armazenado como um campo em cada documento em "Mensalidade_Paga"
                                val mensalidadeID = dc.document.getString("id_mensalidade") // Supondo que você tenha esse campo
                                mensalidadeID?.let { id ->
                                    atualizarStatusMensalidadeAluno(usuarioID, id, status)
                                }
                            }
                        }
                        else->{

                        }
                        // Trate outros casos (ADDED, REMOVED) conforme necessário
                    }
                }
            }
    }

    fun converterDataParaOrdenacao(data: String): String {
        val partes = data.split("/")
        if (partes.size == 2) { // Garante que a data está no formato esperado
            return "${partes[1]}${partes[0].padStart(2, '0')}" // Preenche o mês com zero à esquerda se necessário
        }
        return data // Retorna a data original se não estiver no formato esperado
    }

    fun obterListaMensalidadePaga(lista_mensalidade_paga:MutableList<MensalidadePagaClasse>, adapterMensalidadePaga: AdapterMensalidadePaga){
        var db = FirebaseFirestore.getInstance()
        var usuarioID = FirebaseAuth.getInstance().currentUser!!.uid
        db.collection("Mensalidade_Aluno").document(usuarioID).collection("Mensalidade_Paga").orderBy("refMes", Query.Direction.DESCENDING)
            .get().addOnCompleteListener{tarefa->
                if (tarefa.isSuccessful){
                    for (documento in tarefa.result!!){
                        val mensalidadePaga = documento.toObject(MensalidadePagaClasse::class.java)
                        lista_mensalidade_paga.add(mensalidadePaga)
                        adapterMensalidadePaga.notifyDataSetChanged()
                    }
                }
            }

    }




    fun atualizarDadosAlunos(nome: String, idade: String, restricao: String) {
        val usuarioID = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val documentoReferencia: DocumentReference = db.collection("Alunos").document(usuarioID)
        documentoReferencia.update("nome", nome, "idade", idade, "descricao_medica", restricao)
            .addOnCompleteListener {




            }

    }

    fun salvarMensalidadeAlunos(
        nomePagador: String,
        tituloCobranca: String,
        precoMensalidade: String,
        status_pagamento: String,
        data: String,
        refMes: String,
        foto: String,
        idMensalidadePendente: String,
        nomeAluno:String,
        email_aluno:String,
        alunoID:String
           ) {
        val db = FirebaseFirestore.getInstance()
        val usuarioID = FirebaseAuth.getInstance().currentUser!!.uid

        val mensalidadePaga = hashMapOf(
            "nome_pagador" to nomePagador,
            "titulo_cobranca" to tituloCobranca,
            "preco" to precoMensalidade,
            "status_pagamento" to status_pagamento,
            "data" to data,
            "refMes" to refMes,
            "foto" to foto,
            "id_mensalidade" to idMensalidadePendente, // Use este campo para armazenar o ID da mensalidade pendente
            "nome" to nomeAluno,
            "email_aluno" to email_aluno,
            "alunoID" to alunoID

        )
        Log.d("nomeUsuario", "nome do usuario é ${nomeAluno}")
        // Use o mesmo ID para o documento na coleção 'Mensalidade_Paga'
        val documentoReferencia = db.collection("Mensalidade_Aluno")
            .document(usuarioID).collection("Mensalidade_Paga").document(idMensalidadePendente)

        documentoReferencia.set(mensalidadePaga).addOnSuccessListener {
            Log.d("j", "Sucesso ao salvar mensalidade paga")
        }.addOnFailureListener { e ->
            Log.e("j", "Erro ao salvar mensalidade paga", e)
        }
    }

    fun verificarStatusMensalidade(mensalidadeID: String, callback: (Boolean) -> Unit) {
        val usuarioID = FirebaseAuth.getInstance().currentUser!!.uid
        val mensalidadeRef = FirebaseFirestore.getInstance()
            .collection("Alunos").document(usuarioID)
            .collection("Mensalidades").document(mensalidadeID)

        mensalidadeRef.get().addOnSuccessListener { documento ->
            if (documento.exists()) {
                // Supondo que o status seja armazenado em um campo chamado "status_pagamento"
                val status = documento.getString("status_pagamento")
                // Se o status for "Em verificação", consideramos que já existe um comprovante enviado
                callback(status == "Em verificação")
            } else {
                callback(false)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }





    fun setImageBasedOnBeltAndDegree(faixa: String?, grau: String?, imageView: ImageView) {
        when (faixa) {
            "Branca" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_branca)
                    "1" -> imageView.setImageResource(R.drawable.faixa_branca_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_branca_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_branca_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_branca_4graus)
                }
            }

            "Azul" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_azul)
                    "1" -> imageView.setImageResource(R.drawable.faixa_azul_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_azul_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_azul_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_branca_4graus)
                }
            }

            "Roxa" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_roxa)
                    "1" -> imageView.setImageResource(R.drawable.faixa_roxa_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_roxa_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_roxa_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_roxa_4graus)
                }
            }

            "Marrom" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_marrom)
                    "1" -> imageView.setImageResource(R.drawable.faixa_marrom_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_marrom_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_marrom_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_marrom_4graus)
                }
            }

            "Preta" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_preta_jiujitsu)
                    "1" -> imageView.setImageResource(R.drawable.faixa_preta_jiujitsu)
                    "2" -> imageView.setImageResource(R.drawable.faixa_preta_jiujitsu)
                    "3" -> imageView.setImageResource(R.drawable.faixa_preta_jiujitsu)
                    "4" -> imageView.setImageResource(R.drawable.faixa_preta_jiujitsu)
                }
            }
            "Cinza" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_cinza)
                    "1" -> imageView.setImageResource(R.drawable.faixa_cinza_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_cinza_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_cinza_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_cinza_4graus)
                }

            }
            "Cinza-Branca" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_cinza_branca)
                    "1" -> imageView.setImageResource(R.drawable.faixa_cinza_branca_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_cinza_branca_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_cinza_branca_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_cinza_branca_4graus)
                }

            }
            "Cinza-Preta" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_cinza_preta)
                    "1" -> imageView.setImageResource(R.drawable.faixa_cinza_preta_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_cinza_preta_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_cinza_preta_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_cinza_preta_4graus)
                }

            }

            "Amarela" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_amarela)
                    "1" -> imageView.setImageResource(R.drawable.faixa_amarela_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_amarela_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_amarela_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_amarela_4graus)
                }

            }
            "Amarela-Branca" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_amarela_branca)
                    "1" -> imageView.setImageResource(R.drawable.faixa_amarela_branca_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_amarela_branca_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_amarela_branca_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_amarela_branca_4graus)
                }

            }
            "Amarela-Preta" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_amarela_preta)
                    "1" -> imageView.setImageResource(R.drawable.faixa_amarela_preta_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_amarela_preta_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_amarela_preta_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_amarela_preta_4graus)
                }

            }
            "Laranja" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_laranja)
                    "1" -> imageView.setImageResource(R.drawable.faixa_laranja_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_laranja_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_laranja_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_laranja_4graus)
                }

            }
            "Laranja-Branca" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_laranja_branca)
                    "1" -> imageView.setImageResource(R.drawable.faixa_laranja_branca_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_laranja_branca_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_laranja_branca_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_laranka_branca_4graus)
                }

            }
            "Laranja-Preta" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_laranja_preta)
                    "1" -> imageView.setImageResource(R.drawable.faixa_laranja_preta_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_laranja_preta_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_laranja_preta_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_laranja_preta_4graus)
                }

            }
            "Verde" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_verde)
                    "1" -> imageView.setImageResource(R.drawable.faixa_verde_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_verde_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_verde_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_verde_4graus)
                }

            }
            "Verde-Branca" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_verde_branca)
                    "1" -> imageView.setImageResource(R.drawable.faixa_verde_branca_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_verde_branca_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_verde_branca_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_verde_branca_4graus)
                }

            }
            "Verde-Preta" -> {
                when (grau) {
                    "0" -> imageView.setImageResource(R.drawable.faixa_verde_preta)
                    "1" -> imageView.setImageResource(R.drawable.faixa_verde_preta_1grau)
                    "2" -> imageView.setImageResource(R.drawable.faixa_verde_preta_2graus)
                    "3" -> imageView.setImageResource(R.drawable.faixa_verde_preta_3graus)
                    "4" -> imageView.setImageResource(R.drawable.faixa_verde_preta_4graus)
                }

            }
        }


    }



}








