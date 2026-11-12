package com.example.medagenda.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.medagenda.R
import com.example.medagenda.data.MedAgendaRepository
import com.example.medagenda.data.Resultado
import com.example.medagenda.ui.cadastro.EscolhaTipoActivity
import com.example.medagenda.ui.comum.aplicarMargensDoSistema
import com.example.medagenda.ui.comum.emailValido
import com.example.medagenda.ui.comum.texto
import com.example.medagenda.ui.comum.validar
import com.example.medagenda.ui.principal.PrincipalActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Tela inicial do app: login com e-mail e senha. */
class LoginActivity : AppCompatActivity() {

    companion object {
        /** Usado pelos cadastros para já preencher o e-mail recém-cadastrado. */
        const val EXTRA_EMAIL = "email"
    }

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilSenha: TextInputLayout
    private lateinit var btnEntrar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        findViewById<android.view.View>(R.id.main).aplicarMargensDoSistema()

        tilEmail = findViewById(R.id.tilEmail)
        tilSenha = findViewById(R.id.tilSenha)
        btnEntrar = findViewById(R.id.btnEntrar)

        preencherEmail(intent)

        btnEntrar.setOnClickListener { entrar() }
        findViewById<MaterialButton>(R.id.btnCriarConta).setOnClickListener {
            startActivity(Intent(this, EscolhaTipoActivity::class.java))
        }
    }

    /** Chamado quando um cadastro volta para esta tela (ela já estava aberta). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        preencherEmail(intent)
    }

    private fun preencherEmail(intent: Intent) {
        val email = intent.getStringExtra(EXTRA_EMAIL) ?: return
        tilEmail.editText?.setText(email)
        tilSenha.editText?.text?.clear()
        tilSenha.requestFocus()
    }

    private fun entrar() {
        val email = tilEmail.texto
        val senha = tilSenha.texto

        val emailOk = tilEmail.validar(
            when {
                email.isEmpty() -> getString(R.string.erro_obrigatorio)
                !email.emailValido() -> getString(R.string.erro_email)
                else -> null
            }
        )
        val senhaOk = tilSenha.validar(if (senha.isEmpty()) getString(R.string.erro_obrigatorio) else null)
        if (!emailOk || !senhaOk) return

        btnEntrar.isEnabled = false
        lifecycleScope.launch {
            when (val resultado = MedAgendaRepository.login(email, senha)) {
                is Resultado.Sucesso -> {
                    val principal = Intent(this@LoginActivity, PrincipalActivity::class.java)
                    // Limpa a pilha: ao apertar "voltar" na tela principal, o app fecha em vez de voltar ao login.
                    principal.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(principal)
                }
                is Resultado.Erro -> {
                    Toast.makeText(this@LoginActivity, resultado.mensagem, Toast.LENGTH_SHORT).show()
                    btnEntrar.isEnabled = true
                }
            }
        }
    }
}
