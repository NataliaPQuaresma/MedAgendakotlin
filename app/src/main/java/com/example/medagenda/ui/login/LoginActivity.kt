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

/**
 * TELA DE LOGIN: a primeira tela que aparece quando o app abre.
 * (Quem define isso é o AndroidManifest.xml, com o filtro "LAUNCHER".)
 *
 * O que faz:
 *  - O usuário digita e-mail e senha e toca em "Entrar".
 *  - O app confere os campos e pergunta ao repositório se o login existe.
 *  - Se existir, abre a PrincipalActivity. Se não, mostra uma mensagem de erro.
 *  - O botão "Criar conta" abre a EscolhaTipoActivity (médico ou paciente).
 *
 * Layout (visual) desta tela: res/layout/activity_login.xml
 *
 * `Activity` = uma tela do Android. `AppCompatActivity` é a versão que funciona
 * igual em celulares novos e antigos.
 */
class LoginActivity : AppCompatActivity() {

    // `companion object` = coisas que pertencem à tela, mas podem ser usadas de fora
    // sem abrir a tela. Aqui guardamos o "nome da etiqueta" do e-mail.
    companion object {
        // Nome usado para mandar o e-mail de uma tela para outra (via Intent).
        // Os cadastros usam essa etiqueta para já deixar o e-mail preenchido aqui.
        const val EXTRA_EMAIL = "email"
    }

    // Referências aos componentes da tela (definidos no XML).
    // `lateinit` = "vou preencher depois", porque eles só existem após o setContentView.
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilSenha: TextInputLayout
    private lateinit var btnEntrar: MaterialButton

    // `onCreate` = o que acontece quando a tela é criada (é a "porta de entrada" da tela).
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()                         // app ocupa a tela inteira
        setContentView(R.layout.activity_login)    // liga este código ao XML da tela
        findViewById<android.view.View>(R.id.main).aplicarMargensDoSistema() // não fica atrás das barras

        // `findViewById` = procura no XML o componente com aquele id.
        tilEmail = findViewById(R.id.tilEmail)
        tilSenha = findViewById(R.id.tilSenha)
        btnEntrar = findViewById(R.id.btnEntrar)

        // Se a tela foi aberta por um cadastro, já preenche o e-mail.
        preencherEmail(intent)

        // O que acontece ao tocar em cada botão.
        btnEntrar.setOnClickListener { entrar() }
        findViewById<MaterialButton>(R.id.btnCriarConta).setOnClickListener {
            // `Intent` = um "pedido" para abrir outra tela.
            startActivity(Intent(this, EscolhaTipoActivity::class.java))
        }
    }

    /**
     * Chamado quando um cadastro volta para esta tela, que já estava aberta por baixo.
     * Por que precisa: nesse caso o Android não chama o onCreate de novo (a tela é
     * reaproveitada, por causa do "singleTop" no manifest). Ele chama este método.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        preencherEmail(intent)
    }

    // Se veio um e-mail junto com o Intent (vindo do cadastro), coloca no campo
    // e deixa o cursor na senha. Facilita: a pessoa só precisa digitar a senha.
    private fun preencherEmail(intent: Intent) {
        val email = intent.getStringExtra(EXTRA_EMAIL) ?: return   // se não veio e-mail, não faz nada
        tilEmail.editText?.setText(email)
        tilSenha.editText?.text?.clear()
        tilSenha.requestFocus()
    }

    // Executado ao tocar em "Entrar".
    private fun entrar() {
        val email = tilEmail.texto
        val senha = tilSenha.texto

        // VALIDAÇÃO: confere os campos ANTES de tentar o login.
        // `when` funciona como vários "if": devolve a mensagem do primeiro problema
        // encontrado, ou null se estiver tudo certo.
        val emailOk = tilEmail.validar(
            when {
                email.isEmpty() -> getString(R.string.erro_obrigatorio)
                !email.emailValido() -> getString(R.string.erro_email)
                else -> null
            }
        )
        val senhaOk = tilSenha.validar(if (senha.isEmpty()) getString(R.string.erro_obrigatorio) else null)
        if (!emailOk || !senhaOk) return   // tem erro? Para aqui (as mensagens já aparecem nos campos).

        // Desliga o botão para a pessoa não tocar duas vezes enquanto espera.
        btnEntrar.isEnabled = false

        // `lifecycleScope.launch` = roda um código que pode demorar (como chamar
        // a API) sem travar a tela. Necessário porque o login é uma função `suspend`.
        lifecycleScope.launch {
            when (val resultado = MedAgendaRepository.login(email, senha)) {
                is Resultado.Sucesso -> {
                    // Login certo: abre a tela principal.
                    val principal = Intent(this@LoginActivity, PrincipalActivity::class.java)
                    // Estas "flags" apagam as telas anteriores da memória. Assim, ao
                    // apertar "voltar" na tela principal, o app fecha em vez de voltar ao login.
                    principal.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(principal)
                }
                is Resultado.Erro -> {
                    // Login errado: mostra a mensagem ("E-mail ou senha incorretos.")
                    // num balãozinho (Toast) e liga o botão de novo.
                    Toast.makeText(this@LoginActivity, resultado.mensagem, Toast.LENGTH_SHORT).show()
                    btnEntrar.isEnabled = true
                }
            }
        }
    }
}
