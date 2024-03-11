package com.example.alunojiujitsu.dailog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import com.bumptech.glide.Glide
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.activitys.FormEditarPerfil.FormEditarPerfil
import com.example.alunojiujitsu.databinding.ActivityTelaPrincipalAlunoBinding
import com.example.alunojiujitsu.databinding.DialogAlunoPerfilBinding
import com.example.alunojiujitsu.model.DB
import kotlin.math.acos

class DialogPerfilAluno(private val activity: Activity) {
    lateinit var dialog: AlertDialog
    lateinit var binding: DialogAlunoPerfilBinding

    fun iniciarPerfilUsuario(imageUrl: String?) {
        val builder = AlertDialog.Builder(activity)
        binding = DialogAlunoPerfilBinding.inflate(activity.layoutInflater)
        builder.setView(binding.root)
        builder.setCancelable(true)
        dialog = builder.create()

        // Configura a imagem de perfil se o imageUrl não for nulo
        imageUrl?.let {
            Glide.with(activity).load(it).into(binding.imagePerfil)
        }

        dialog.show()
    }

    fun recuperarDadosAluno() {
            val nome = binding.txtNomeAluno
            val email = binding.txtEmailPerfil
            val idade = binding.txtIdadePerfil
            val restricoes = binding.txtRestricoesMedicasPerfil
            val faixa = binding.txtFaixaPerfil
            val grau = binding.txtGraus
            val imagemfaixa = binding.imageFaixa


            val db = DB()
            db.recuperarDadosAlunos(nome, email, faixa, grau, idade, restricoes, imagemfaixa)

            binding.buttonEditarPerfil.setOnClickListener {

                val nomeUsuario = binding.txtNomeAluno.text.toString()
                val idadeUsuario = binding.txtIdadePerfil.text.toString()
                val restricaoMedica = binding.txtRestricoesMedicasPerfil.text.toString()
                val intent = Intent(activity, FormEditarPerfil::class.java)
                intent.putExtra("nome", nomeUsuario)
                intent.putExtra("idade", idadeUsuario)
                intent.putExtra("restricao", restricaoMedica)


                activity.startActivity(intent)
                activity.finish()


            }

        }




    }
