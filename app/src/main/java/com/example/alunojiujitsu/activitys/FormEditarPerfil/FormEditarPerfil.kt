package com.example.alunojiujitsu.activitys.FormEditarPerfil

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide

import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.databinding.ActivityFormEditarPerfilBinding
import com.example.alunojiujitsu.model.DB
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import com.example.alunojiujitsu.ToastPersonalizado.ToastPersonalizado
import com.example.alunojiujitsu.activitys.telaPrincipal.TelaPrincipalAluno
import com.example.alunojiujitsu.dailog.DialogFotoPerfil
import com.example.alunojiujitsu.dailog.DialogPerfilAluno

class FormEditarPerfil : AppCompatActivity() {
    lateinit var binding: ActivityFormEditarPerfilBinding


    companion object {

        private const val CAMERA_REQUEST_CODE = 102
        private const val GALLERY_REQUEST_CODE = 103

    }
    private var imageUri: Uri? = null
    private var isUploading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormEditarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

     //   checkPermissions()
        carregarFotoPerfilUsuario()



        binding.imageButtonCamera.setOnClickListener {
            openCamera()
        }

        binding.imageButtonGaleria.setOnClickListener {
            openGallery()
        }


        val nomeUsuario = intent.extras!!.getString("nome")
        val idadeUsuario = intent.extras!!.getString("idade")
        val restricaoMedica = intent.extras!!.getString("restricao")

        binding.editAtualizarNome.setText(nomeUsuario)
        binding.editAtualizarIdade.setText(idadeUsuario)
        binding.editAtualizarRestricaoMedica.setText(restricaoMedica)


        val sugestaoIdade = arrayOf("04-10 anos", "10-15 anos", "15-18 anos", "Adulto")
        val adapterIdade = ArrayAdapter(this, android.R.layout.simple_list_item_1, sugestaoIdade)
        binding.editAtualizarIdade.apply {
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


        binding.buttonAtualizarDados.setOnClickListener {

            val nomeUsuarioAtualizado = binding.editAtualizarNome.text.toString()
            val restricaoMedicaAtualizada = binding.editAtualizarRestricaoMedica.text.toString()
            val idadeAtualizada = binding.editAtualizarIdade.text.toString()


            if (isUploading) {

                Toast.makeText(this, "Por favor , aguarde o upload da imagem", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (nomeUsuarioAtualizado.isNotEmpty() || restricaoMedicaAtualizada.isNotEmpty() || idadeAtualizada.isNotEmpty()){

                val db = DB()
                db.atualizarDadosAlunos(nomeUsuarioAtualizado, idadeAtualizada, restricaoMedicaAtualizada)
                Toast.makeText(this, "Atualizado com sucesso", Toast.LENGTH_LONG).show()

                finish()
                startActivity(Intent(this, TelaPrincipalAluno::class.java))

            }else{
                val snackbar = Snackbar.make(it, "Preencha todos os campos", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.setTextColor(Color.WHITE)
                  }



        }


    }

    private fun openCamera() {
        val imageFile = createImageFile()
        imageUri = FileProvider.getUriForFile(
            this,
            "com.example.alunojiujitsu.fileprovider",
            imageFile
        )
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timestamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }


    private fun openGallery() {
        // Solicite permissão de armazenamento aqui
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    imageUri?.let { uri ->
                        uploadImageToFirebaseStorage(uri) { imageUrl ->
                            Glide.with(this).load(imageUrl).into(binding.imageAtualizarPerfil)
                            saveImageToPreferences(imageUrl) // Salva a URL da imagem nas preferências
                        }
                    } ?: Log.e("Camera Resultado", "Uri da imagem é nulo.")
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data as Uri
                    uploadImageToFirebaseStorage(selectedImageUri) { imageUrl ->
                        Glide.with(this).load(imageUrl).into(binding.imageAtualizarPerfil)
                        saveImageToPreferences(imageUrl) // Salva a URL da imagem nas preferências
                    }
                }
            }
        } else {
            Log.d("Atividade Resultado", "Resultado não OK ou requisição não reconhecida.")
        }
    }


    private fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (String) -> Unit) {
        // Certifique-se de usar o UID do usuário como parte do caminho do arquivo no Storage para torná-lo único
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val storageRef = FirebaseStorage.getInstance().getReference("Imagem_Perfil/$userId.jpg")
        isUploading = true
        binding.buttonAtualizarDados.isEnabled = false

        storageRef.putFile(imageUri).addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                val downloadUrl = downloadUri.toString()
                isUploading = false
                binding.buttonAtualizarDados.isEnabled = true
                Log.d("Upload Sucesso", "Imagem enviada com sucesso: $downloadUrl")
                updateUserProfileImage(downloadUrl) // Atualiza a imagem no Firestore
                callback(downloadUrl) // Passa a URL da imagem para o callback
            }
        }.addOnFailureListener {
            // Trate o erro aqui
            ToastPersonalizado.showToast(this, "Erro ao fazer upload da imagem")
            Log.e("Upload deu ruim", "Erro ao fazer upload da imagem", it)
        }
    }


    private fun updateUserProfileImage(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("Alunos").document(userId ?: "")
        isUploading = true
        binding.buttonAtualizarDados.isEnabled = false
        val dialogFotoPerfil = DialogFotoPerfil(this)
        dialogFotoPerfil.iniciarCarregamentoAlertDialog()
        Handler(Looper.getMainLooper()).postDelayed({
            dialogFotoPerfil.liberarCarregamento()
        },5000)
        userRef.update("profileImageUrl", imageUrl).addOnSuccessListener {
            isUploading = false
            binding.buttonAtualizarDados.isEnabled = true
            Log.d("Update Sucesso", "Imagem de perfil atualizada com sucesso no Firestore")


        }.addOnFailureListener {
            // Trate o erro aqui
            ToastPersonalizado.showToast(this, "Erro ao atualizar a imagem no perfil")
            isUploading = false
            binding.buttonAtualizarDados.isEnabled = true

        }
    }


    private fun saveImageToPreferences(imageUrl: String) {
        val sharedPrefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putString("profileImageUrl", imageUrl).apply()
    }

    private fun carregarFotoPerfilUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("Alunos").document(userId ?: "")

        userRef.get().addOnSuccessListener { documento ->
            if (documento != null && documento.exists()) {
                val imageUrl = documento.getString("profileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {



                        Glide.with(this).load(imageUrl).into(binding.imageAtualizarPerfil)
                        saveImageToPreferences(imageUrl) // Salva a URL da imagem nas SharedPreferences


                }
            }
        }.addOnFailureListener {
           ToastPersonalizado.showToast(this, "Erro ao carregar a foto do perfil")
        }
    }



}