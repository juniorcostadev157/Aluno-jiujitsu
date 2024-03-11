package com.example.alunojiujitsu.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.alunojiujitsu.activitys.DetalhesPagamento.DetalhesMensalidade
import com.example.alunojiujitsu.databinding.MensalidadeItemBinding
import com.example.alunojiujitsu.model.Mensalidade

class AdapterMensalidade(val context: Context, val lista_mensalidade:MutableList<Mensalidade>):
    RecyclerView.Adapter<AdapterMensalidade.MensalidadeViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensalidadeViewHolder {

        val item_lista = MensalidadeItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MensalidadeViewHolder(item_lista)
    }

    override fun getItemCount() = lista_mensalidade.size

    override fun onBindViewHolder(holder: MensalidadeViewHolder, position: Int) {

        Glide.with(context).load(lista_mensalidade.get(position).foto).into(holder.fotoMensalidade)
        holder.dataMensalidade.text = lista_mensalidade.get(position).data
        holder.precoMensalidade.text = "R$ ${lista_mensalidade.get(position).preco}"
        holder.titulo_cobranca.text = lista_mensalidade.get(position).titulo_cobranca

        holder.itemView.setOnClickListener {

            val intent = Intent(context, DetalhesMensalidade::class.java)
            intent.putExtra("data", lista_mensalidade.get(position).data)
            intent.putExtra("preco", lista_mensalidade.get(position).preco)
            intent.putExtra("titulo_cobranca", lista_mensalidade.get(position).titulo_cobranca)
            intent.putExtra("status_pagamento", lista_mensalidade.get(position).status_pagamento)
            intent.putExtra("alunoID", lista_mensalidade.get(position).alunoId)
            intent.putExtra("mensalidadeID", lista_mensalidade.get(position).id)
            intent.putExtra("nomeAluno", lista_mensalidade.get(position).nome)
            intent.putExtra("email_aluno", lista_mensalidade.get(position).email_aluno)

            context.startActivity(intent)

        }

    }
    inner class MensalidadeViewHolder(binding: MensalidadeItemBinding) : RecyclerView.ViewHolder(binding.root){

        val fotoMensalidade = binding.imageMensalidade
        val dataMensalidade = binding.txtData
        val precoMensalidade = binding.txtPrecoMensalidade
        val titulo_cobranca = binding.txtTituloCobranca



    }



}