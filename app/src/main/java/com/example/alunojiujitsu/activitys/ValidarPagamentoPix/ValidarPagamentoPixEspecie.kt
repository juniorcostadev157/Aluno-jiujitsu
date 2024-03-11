package com.example.alunojiujitsu.activitys.ValidarPagamentoPix

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.ToastPersonalizado.ToastPersonalizado
import com.example.alunojiujitsu.dailog.DialogCarregamentoComprovante
import com.example.alunojiujitsu.databinding.ActivityValidarPagamentoPixEspecieBinding
import com.example.alunojiujitsu.model.DB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ValidarPagamentoPixEspecie : AppCompatActivity() {

    lateinit var binding: ActivityValidarPagamentoPixEspecieBinding
    companion object {
        private const val IMAGE_PICK_CODE = 104
        private const val IMAGE_CAPTURE_CODE = 105
        private const val REQUEST_CAMERA_PERMISSION = 101
        // Outras constantes...
    }
    private var imageUri: Uri? = null
    private var db=DB()
    private var alunoID = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityValidarPagamentoPixEspecieBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.buttonEnviarComprovante.setOnClickListener {
            val mensalidadeID = intent.extras!!.getString("mensalidadeID").toString()

            db.verificarStatusMensalidade(mensalidadeID) { statusVerificado ->
                if (!statusVerificado) {
                    enviarComprovante()
                } else {

                    Toast.makeText(this,"Já existe um comprovante enviado e em análise para esta mensalidade.", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.imageButtonCameraValidao.setOnClickListener {
            openCameraForProof()
        }

        binding.imageButtonGaleriaValidacao.setOnClickListener {
            openGalleryForProof()
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissão concedida
                    openCameraForProof()
                } else {
                    // Permissão negada
                    Toast.makeText(this , "Permissão de câmera negada", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    // Lidar com a imagem escolhida da galeria
                    val selectedImageUri = data?.data as Uri
                    imageUri = selectedImageUri // Atribuir a URI da imagem selecionada à variável imageUri
                    Glide.with(this).load(selectedImageUri).into(binding.imageComprovante)
                }
                IMAGE_CAPTURE_CODE -> {
                    // Lidar com a imagem capturada pela câmera
                    imageUri?.let { uri ->
                        Glide.with(this).load(uri).into(binding.imageComprovante)
                    }
                }
            }
        }
    }



    private fun openGalleryForProof() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, IMAGE_PICK_CODE)
    }


    // Função para fazer o upload da imagem para o Firebase Storage e atualizar o Firestore
    private fun uploadProofToFirebaseStorage(imageUri: Uri, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReference("Comprovantes/${UUID.randomUUID()}.jpg")
        storageRef.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                Log.d("Upload", "Comprovante enviado com sucesso: $imageUrl")
                callback(imageUrl) // Chame o callback passando a URL da imagem
            }
        }.addOnFailureListener {
            // Lidar com falhas no upload
            Log.e("Upload", "Falha ao enviar comprovante", it)
        }
    }
    private fun openCameraForProof() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.alunojiujitsu.fileprovider", // altere para o seu FileProvider
                    it
                )
                imageUri = photoURI
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
            }
        }
    }
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun enviarComprovante(){

        val titulo = intent.extras!!.getString("titulo").toString()
        val nome = intent.extras!!.getString("nome").toString()
        val preco = intent.extras!!.getString("preco").toString()
        val data = intent.extras!!.getString("data").toString()
        val status_pagamento = intent.extras!!.getString("status_pagamento").toString()
        val refMes = intent.extras!!.getString("refMes").toString()
        val mensalidadeID = intent.extras!!.getString("mensalidadeID").toString()
        val nomeAluno = intent.extras!!.getString("nomeAluno").toString()
        val email_aluno = intent.extras!!.getString("email_aluno").toString()
        binding.buttonEnviarComprovante.isEnabled = false
        imageUri?.let { uri ->
            uploadProofToFirebaseStorage(uri) { imageUrl ->
                // Agora que temos a URL da imagem, podemos salvar todos os detalhes da mensalidade



                val dialogCarregamentoComprovante = DialogCarregamentoComprovante(this)
                dialogCarregamentoComprovante.iniciarCarregamentoAlertDialog()
                Handler(Looper.getMainLooper()).postDelayed({
                    db.salvarMensalidadeAlunos(nome, titulo, preco, status_pagamento, data, refMes, imageUrl, mensalidadeID, nomeAluno, email_aluno, alunoID)
                    // Reabilita o botão quando a operação for concluída com sucesso
                    dialogCarregamentoComprovante.liberarCarregamento()
                    finish()

                    Toast.makeText(this, "Comprovante enviado com sucesso", Toast.LENGTH_LONG).show()

                }, 5000)



            }
        } ?: run {
            Toast.makeText(this,"Por favor, selecione uma imagem como comprovante.", Toast.LENGTH_LONG ).show()
            binding.buttonEnviarComprovante.isEnabled = true
        }
    }


}