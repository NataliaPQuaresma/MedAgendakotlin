package com.example.medagenda.ui.cadastro

import android.app.Activity
import android.content.Intent
import com.example.medagenda.R
import com.example.medagenda.ui.comum.emailValido
import com.example.medagenda.ui.comum.telefoneValido
import com.example.medagenda.ui.comum.texto
import com.example.medagenda.ui.comum.validar
import com.example.medagenda.ui.login.LoginActivity
import com.google.android.material.textfield.TextInputLayout

/**
 * Os campos que os dois cadastros têm em comum (layout campos_comuns_cadastro.xml)
 * e a validação deles.
 */
class CamposComuns(private val activity: Activity) {

    val tilNome: TextInputLayout = activity.findViewById(R.id.tilNome)
    val tilTelefone: TextInputLayout = activity.findViewById(R.id.tilTelefone)
    val tilEmail: TextInputLayout = activity.findViewById(R.id.tilEmail)
    val tilSenha: TextInputLayout = activity.findViewById(R.id.tilSenha)
    val tilConfirmarSenha: TextInputLayout = activity.findViewById(R.id.tilConfirmarSenha)

    /** Valida todos os campos e mostra o erro embaixo de cada um. Retorna true se tudo está certo. */
    fun validar(): Boolean {
        val obrigatorio = activity.getString(R.string.erro_obrigatorio)

        val nomeOk = tilNome.validar(if (tilNome.texto.isEmpty()) obrigatorio else null)

        val telefoneOk = tilTelefone.validar(
            when {
                tilTelefone.texto.isEmpty() -> obrigatorio
                !tilTelefone.texto.telefoneValido() -> activity.getString(R.string.erro_telefone)
                else -> null
            }
        )

        val emailOk = tilEmail.validar(
            when {
                tilEmail.texto.isEmpty() -> obrigatorio
                !tilEmail.texto.emailValido() -> activity.getString(R.string.erro_email)
                else -> null
            }
        )

        val senhaOk = tilSenha.validar(
            when {
                tilSenha.texto.isEmpty() -> obrigatorio
                tilSenha.texto.length < 6 -> activity.getString(R.string.erro_senha_curta)
                else -> null
            }
        )

        val confirmarOk = tilConfirmarSenha.validar(
            when {
                tilConfirmarSenha.texto.isEmpty() -> obrigatorio
                tilConfirmarSenha.texto != tilSenha.texto -> activity.getString(R.string.erro_senhas_diferentes)
                else -> null
            }
        )

        return nomeOk && telefoneOk && emailOk && senhaOk && confirmarOk
    }

    /** Telefone só com números, para salvar padronizado. */
    val telefoneSomenteNumeros: String
        get() = tilTelefone.texto.filter { it.isDigit() }
}

/** Depois de cadastrar: volta para o login (fechando as telas de cadastro) com o e-mail já preenchido. */
fun Activity.voltarParaLoginComEmail(email: String) {
    val login = Intent(this, LoginActivity::class.java)
    login.putExtra(LoginActivity.EXTRA_EMAIL, email)
    login.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    startActivity(login)
    finish()
}
