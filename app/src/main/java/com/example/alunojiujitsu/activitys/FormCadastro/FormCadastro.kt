package com.example.alunojiujitsu.activitys.FormCadastro

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.databinding.ActivityFormCadastroBinding
import com.example.alunojiujitsu.model.DB
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class FormCadastro : AppCompatActivity() {

    lateinit var binding: ActivityFormCadastroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityFormCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sugestaoIdade = arrayOf("04-10 anos", "10-15 anos", "15-18 anos", "Adulto")
        val adapterIdade = ArrayAdapter(this, android.R.layout.simple_list_item_1, sugestaoIdade)
        binding.editAutoIdade.apply {
            setAdapter(adapterIdade)
            threshold = 0
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) showDropDown()
            }
            setOnClickListener {
                requestFocus()
                showDropDown()
            }
            inputType = InputType.TYPE_NULL
        }

        binding.editAutoIdade.setOnItemClickListener { parent, view, position, id ->
            val idadeSelecionada = parent.getItemAtPosition(position) as String
            atualizarFaixasComBaseNaIdade(idadeSelecionada)
        }


        var sugestaoGrau = arrayOf("0", "1" , "2" , "3", "4")
        var adapterGrau = ArrayAdapter(this, android.R.layout.simple_list_item_1, sugestaoGrau)
        binding.editGrauCadastro.apply {
            setAdapter(adapterGrau)
            threshold = 0
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) showDropDown()
            }
            setOnClickListener {
                requestFocus()
                showDropDown()
            }

            inputType = InputType.TYPE_NULL
        }


            binding.buttonCadastrar.setOnClickListener {

                binding.buttonCadastrar.isEnabled = false

            val nome = binding.editNomeCadastro.text.toString()
            val email = binding.editEmailCadastro.text.toString()
            val senha = binding.editSenhaCadastro.text.toString()
            val idade = binding.editAutoIdade.text.toString()
            val faixa = binding.editFaixaCadastro.text.toString()
            val grau = binding.editGrauCadastro.text.toString()
            val descricao_medica = binding.editRestricaoMedica.text.toString()

            if(nome.isNotEmpty() && email.isNotEmpty()&& senha.isNotEmpty() && idade.isNotEmpty() && faixa.isNotEmpty() && grau.isNotEmpty() && descricao_medica.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha).addOnCompleteListener{login->
                    if (login.isSuccessful){
                         val db = DB()
                        db.salvarAlunosBancoDados(nome, email, idade, faixa, grau, descricao_medica)
                        Toast.makeText(this, "Cadastro realizado com sucesso", Toast.LENGTH_LONG).show()
                        finish()

                    }

                }.addOnFailureListener {erroCadastro->

                    var mensagemErro = when(erroCadastro){
                        is FirebaseAuthWeakPasswordException ->"Digite uma senha mais forte"
                        is FirebaseAuthUserCollisionException -> "Essa conta ja possui cadastro"
                        is FirebaseNetworkException -> "Sem conexão com a internet"
                        else-> "Erro ao cadastrar"


                    }
                    val snackbar = Snackbar.make(it, mensagemErro, Snackbar.LENGTH_SHORT)
                    snackbar.show()
                    snackbar.setBackgroundTint(Color.RED)
                    snackbar.setTextColor(Color.WHITE)
                    binding.buttonCadastrar.isEnabled = true


                }



            }else{
                val snackbar = Snackbar.make(it, "Preencha todos os campos", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.setTextColor(Color.WHITE)
                snackbar.show()
                binding.buttonCadastrar.isEnabled = true


            }





        }

    }
    private fun atualizarFaixasComBaseNaIdade(idade: String) {
        val faixas = when (idade) {
            "04-10 anos" -> arrayOf("Branca", "Cinza", "Cinza-Branca", "Cinza-Preta", "Amarela", "Amarela-Branca", "Amarela-Preto")
            "10-15 anos" -> arrayOf("Branca", "Laranja", "Laranja-Branca", "Laranja-Preta", "Verde", "Verde-Branca", "Verde-Preto")
            "15-18 anos", "Adulto" -> arrayOf("Branca", "Azul", "Roxa", "Marrom", "Preta")
            else -> arrayOf() // Opção padrão vazia para lidar com valores inesperados
        }
        val adapterFaixa = ArrayAdapter(this, android.R.layout.simple_list_item_1, faixas)
        configurarAutoCompleteTextView(binding.editFaixaCadastro, adapterFaixa)
    }
    private fun configurarAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView, adapter: ArrayAdapter<String>) {

        autoCompleteTextView.apply {
            setAdapter(adapter)
            threshold = 0
            onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) showDropDown()
            }
            setOnClickListener {
                requestFocus()
                showDropDown()
            }
            inputType = InputType.TYPE_NULL
        }
    }



}