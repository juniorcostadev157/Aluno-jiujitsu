package com.example.alunojiujitsu.activitys.telaPrincipal

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.alunojiujitsu.R
import com.example.alunojiujitsu.activitys.FormLogin.FormLogin
import com.example.alunojiujitsu.activitys.MensalidadePaga.MensalidadePaga
import com.example.alunojiujitsu.activitys.TelaMensalidade.TelaMensalidade
import com.example.alunojiujitsu.dailog.DialogPerfilAluno
import com.example.alunojiujitsu.databinding.ActivityTelaPrincipalAlunoBinding
import com.example.alunojiujitsu.model.DB
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class TelaPrincipalAluno : AppCompatActivity() {
    lateinit var binding: ActivityTelaPrincipalAlunoBinding
    private var db =DB()
    val images = listOf(R.drawable.jiu1,R.drawable.macaco_jiu,  R.drawable.jiu2, R.drawable.jiu3, R.drawable.jiu4)
    var currentImageIndex = 0
    private var dialogPerfilAluno: DialogPerfilAluno? = null


    companion object {

        private const val REQUEST_CAMERA_PERMISSION = 101

    }






    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaPrincipalAlunoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Area do Aluno"


        verificarPermissoes()
        salvarTokenNotificacao()

        val imageUrl = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("profileImageUrl", null)
        dialogPerfilAluno?.iniciarPerfilUsuario(imageUrl)
        dialogPerfilAluno?.recuperarDadosAluno()





    }

 /*   fun trocarImagem() {
        binding.imageView4.setImageResource(images[currentImageIndex])
        currentImageIndex = (currentImageIndex + 1) % images.size

        // Chama a função novamente após 5 segundos
        handler.postDelayed({ trocarImagem() }, 10000)
    }


  */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        verificarPermissoes()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.perfil-> abrirDialogPerfilAluno()
            R.id.mensalidade->abrirTelaMensalidade()
            R.id.pagos->abrirTelaMensalidadePaga()
            R.id.loja->"loja"
            R.id.pedidos->"pedidos"
            R.id.sair->deslogarUsuario()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deslogarUsuario(){

        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, FormLogin::class.java))
        finish()
    }

    private fun abrirDialogPerfilAluno() {
        if (dialogPerfilAluno == null) {
            dialogPerfilAluno = DialogPerfilAluno(this)
        }
        // Carregar a imagem de perfil atualizada, se disponível
        val imageUrl = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("profileImageUrl", null)
        dialogPerfilAluno?.iniciarPerfilUsuario(imageUrl)
        dialogPerfilAluno?.recuperarDadosAluno()
    }

    private fun abrirTelaMensalidade(){
        val intent = Intent(this, TelaMensalidade::class.java)
        startActivity(intent)
    }
    private fun abrirTelaMensalidadePaga(){
        startActivity(Intent(this, MensalidadePaga::class.java))
    }

    private fun verificarPermissoes() {
        val permissoesNecessarias = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val todasPermissoesConcedidas = permissoesNecessarias.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!todasPermissoesConcedidas) {
            // Se não, solicite as permissões
            ActivityCompat.requestPermissions(this, permissoesNecessarias, REQUEST_CAMERA_PERMISSION)
        } else {
            // Permissões já concedidas, continue com o fluxo do aplicativo
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                // Verifica se todas as permissões foram concedidas
                val todasPermissoesConcedidas = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                // Se todas as permissões foram concedidas, nada precisa ser feito
                if (todasPermissoesConcedidas) {
                    // Permissões concedidas, continuar o fluxo normal do app
                } else {
                    // Se alguma permissão foi negada, verifica se o usuário escolheu "Não permitir"
                    val algumaPermissaoPermanentementeNegada = grantResults.indices
                        .filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
                        .any { !shouldShowRequestPermissionRationale(permissions[it]) }

                    if (algumaPermissaoPermanentementeNegada) {
                        // Se o usuário escolheu "Não permitir" para alguma permissão, deslogar
                      //  Toast.makeText(this, "Você precisa permitir o acesso para utilizar o app.", Toast.LENGTH_LONG).show()
                       // deslogarUsuario()
                    } else {
                        // Se o usuário não escolheu "Não permitir" permanentemente, não deslogar
                      //  Toast.makeText(this, "Permissão negada. Você pode permitir o acesso nas Configurações.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }
    private fun salvarTokenNotificacao(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(ContentValues.TAG, msg)

            // Se necessário, salve o token no Firestore
            db.salvarTokenNoFirestore(token)
        })

    }









}