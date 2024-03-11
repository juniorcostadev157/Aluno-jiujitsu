package com.example.alunojiujitsu.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alunojiujitsu.databinding.BoletoPagoItemBinding
import com.example.alunojiujitsu.model.MensalidadePagaClasse

class AdapterMensalidadePaga(val context: Context, val lista_mensalidade_paga:MutableList<MensalidadePagaClasse>)
    : RecyclerView.Adapter<AdapterMensalidadePaga.MensalidadePagaViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensalidadePagaViewHolder {
       val item_lista = BoletoPagoItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MensalidadePagaViewHolder(item_lista)
    }

    override fun getItemCount() = lista_mensalidade_paga.size

    override fun onBindViewHolder(holder: MensalidadePagaViewHolder, position: Int) {
        holder.titulo.text = lista_mensalidade_paga.get(position).titulo_cobranca
        holder.nomeUsuario.text = lista_mensalidade_paga.get(position).nome
        holder.precoMensalidade.text = "R$: ${lista_mensalidade_paga.get(position).preco}"
        holder.dataMensalidade.text = lista_mensalidade_paga.get(position).data
        holder.refMes.text = lista_mensalidade_paga.get(position).refMes
        holder.status_pagamento.text = lista_mensalidade_paga.get(position).status_pagamento

        if (holder.status_pagamento.text.equals("Em verificação")){
                holder.status_pagamento.setTextColor(Color.parseColor("#F10808"))
        }else if (holder.status_pagamento.text.equals("Aprovado")){
            holder.status_pagamento.setTextColor(Color.parseColor("#023C05"))
        }

    }
    inner class  MensalidadePagaViewHolder(binding: BoletoPagoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.txtTitulo
        val nomeUsuario = binding.txtNome
        val precoMensalidade = binding.txtPreco
        val dataMensalidade = binding.txtData
        val refMes = binding.txtRefMes
        val status_pagamento = binding.txtStatusPagamento

    }
}