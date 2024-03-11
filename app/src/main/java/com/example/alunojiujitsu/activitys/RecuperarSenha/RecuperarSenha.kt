package com.example.alunojiujitsu.activitys.RecuperarSenha

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.ToastPersonalizado.ToastPersonalizado
import com.example.alunojiujitsu.databinding.ActivityRecuperarSenhaBinding
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenha : AppCompatActivity() {
    lateinit var binding: ActivityRecuperarSenhaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.buttonRecuperarSenha.setOnClickListener {

        val email = binding.editRecuperarEmail.text.toString()

        if (email.isNotEmpty()){
            recuperarSenha(email)

            Toast.makeText(this, "Link para redefinir senha enviado no e-mail", Toast.LENGTH_LONG).show()
            finish()

        }else{
            Toast.makeText(this, "Digite seu email para recuperar a senha", Toast.LENGTH_LONG).show()

        }

        }
    }

    private fun recuperarSenha(email:String){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {

        }
    }
}