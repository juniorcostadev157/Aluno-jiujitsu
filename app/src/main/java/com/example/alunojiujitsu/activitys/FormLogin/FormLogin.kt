package com.example.alunojiujitsu.activitys.FormLogin

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.activitys.FormCadastro.FormCadastro
import com.example.alunojiujitsu.activitys.RecuperarSenha.RecuperarSenha
import com.example.alunojiujitsu.activitys.telaPrincipal.TelaPrincipalAluno
import com.example.alunojiujitsu.dailog.DialogCarregamentoInicial
import com.example.alunojiujitsu.databinding.ActivityFormLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class FormLogin : AppCompatActivity() {
    lateinit var binding: ActivityFormLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editEmail.requestFocus()

        abrirTelaRecuperarSenha()

        binding.txtCadastrar.setOnClickListener {
            startActivity(Intent(this, FormCadastro::class.java))
        }

        binding.buttonEntrar.setOnClickListener {
            binding.buttonEntrar.isEnabled = false
            val email = binding.editEmail.text.toString()
            val senha = binding.editSenha.text.toString()

            when{
                email.isEmpty()->{
                    binding.containterEmail.helperText = "Preencha o campo email"
                    binding.containterSenha.boxStrokeColor = Color.parseColor("#EE0B0B")
                    binding.buttonEntrar.isEnabled = true
                }
                senha.isEmpty()->{
                    binding.containterSenha.helperText = "Preencha o campo senha"
                    binding.containterSenha.boxStrokeColor = Color.parseColor("#EE0B0B")
                    binding.buttonEntrar.isEnabled = true
                }
                else->{
                  autenticacaoUsuario(email, senha)

                }
            }


        }


    }


    private fun autenticacaoUsuario(email:String, senha:String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha).addOnCompleteListener{login->
            if(login.isSuccessful){
                val dialogCarregamentoInicial =DialogCarregamentoInicial(this)
                dialogCarregamentoInicial.iniciarCarregamentoAlertDialog()
                Handler(Looper.getMainLooper()).postDelayed({
                    abrirTelaPrincipalAluno()
                    dialogCarregamentoInicial.liberarCarregamento()
                }, 3000)

                  }


        }.addOnFailureListener {usuario->

            if (usuario is FirebaseNetworkException){
                val snackbar = Snackbar.make(binding.contLogin, "Verifique a conexão com a internet", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.setTextColor(Color.WHITE)
                binding.buttonEntrar.isEnabled = true
            }else if (usuario is FirebaseAuthInvalidUserException){
                binding.containterEmail.helperText = "Digite um email valido"
                binding.containterEmail.boxStrokeColor =Color.parseColor("#EE0B0B")
                binding.buttonEntrar.isEnabled = true
            }else if(usuario is FirebaseAuthInvalidCredentialsException){
                binding.containterEmail.helperText = "Digite um e-mail valido"
                binding.containterEmail.boxStrokeColor =Color.parseColor("#EE0B0B")
                binding.buttonEntrar.isEnabled = true
            }else{
                val snackbar = Snackbar.make(binding.contLogin, "Erro ao logar o aluno", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.setTextColor(Color.WHITE)
                binding.buttonEntrar.isEnabled = true
            }


        }

    }

    private fun abrirTelaPrincipalAluno(){
        val intent = Intent(this, TelaPrincipalAluno::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val usuarioAtual = FirebaseAuth.getInstance().currentUser
        if (usuarioAtual != null){
            abrirTelaPrincipalAluno()
            finish()
        }
    }

    private fun abrirTelaRecuperarSenha(){
        binding.txtRecuperarSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenha::class.java))
        }
    }

}