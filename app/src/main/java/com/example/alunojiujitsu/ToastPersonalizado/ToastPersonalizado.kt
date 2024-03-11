package com.example.alunojiujitsu.ToastPersonalizado

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.alunojiujitsu.R

object ToastPersonalizado {

    fun showToast(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.toast_customizado_sucesso, null)

        val textView = view.findViewById<TextView>(R.id.txtMensagem)
        textView.text = message

        val toast = Toast(context)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.show()
    }
}