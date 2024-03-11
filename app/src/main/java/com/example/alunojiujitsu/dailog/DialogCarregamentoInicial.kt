package com.example.alunojiujitsu.dailog

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import com.example.alunojiujitsu.R

class DialogCarregamentoInicial(private  val activity: Activity){
        lateinit var dialog: Dialog

        fun iniciarCarregamentoAlertDialog(){
            val builder =AlertDialog.Builder(activity)
            val layoutInflater = activity.layoutInflater
            builder.setView(layoutInflater.inflate(R.layout.dialog_carregamento_inicial, null))
            builder.setCancelable(false)
            dialog = builder.create()
            dialog.show()
        }

    fun liberarCarregamento(){
        dialog.dismiss()
    }
}