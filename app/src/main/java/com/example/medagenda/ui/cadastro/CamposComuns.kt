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
 * CAMPOS COMUNS DOS CADASTROS
 *
 * O que é: os 5 campos que médico e paciente têm em comum
 * (nome, telefone, e-mail, senha e confirmar senha) e a validação deles.
 *
 * Por que existe: sem esta classe, o código de validação estaria repetido nas duas
 * telas de cadastro. Aqui fica escrito uma vez só, e as duas telas usam.
 *
 * Liga com:
 *  - res/layout/campos_comuns_cadastro.xml -> o visual destes 5 campos.
 *  - CadastroPacienteActivity e CadastroMedicoActivity -> criam um `CamposComuns(this)`.
 */
class CamposComuns(private val activity: Activity) {

    // Procura os campos na tela que criou esta classe (paciente ou médico).
    // Os ids vêm do arquivo campos_comuns_cadastro.xml.
    val tilNome: TextInputLayout = activity.findViewById(R.id.tilNome)
    val tilTelefone: TextInputLayout = activity.findViewById(R.id.tilTelefone)
    val tilEmail: TextInputLayout = activity.findViewById(R.id.tilEmail)
    val tilSenha: TextInputLayout = activity.findViewById(R.id.tilSenha)
    val tilConfirmarSenha: TextInputLayout = activity.findViewById(R.id.tilConfirmarSenha)

    /**
     * Confere todos os campos e mostra o erro embaixo de cada um que estiver errado.
     * Devolve `true` só se TODOS estiverem certos.
     *
     * Por que valida todos de uma vez (e não para no primeiro erro): assim a pessoa
     * vê todos os problemas juntos, em vez de corrigir um por um.
     */
    fun validar(): Boolean {
        val obrigatorio = activity.getString(R.string.erro_obrigatorio)   // "Campo obrigatório"

        // Nome: só não pode estar vazio.
        val nomeOk = tilNome.validar(if (tilNome.texto.isEmpty()) obrigatorio else null)

        // Telefone: obrigatório e com DDD (10 ou 11 dígitos).
        val telefoneOk = tilTelefone.validar(
            when {
                tilTelefone.texto.isEmpty() -> obrigatorio
                !tilTelefone.texto.telefoneValido() -> activity.getString(R.string.erro_telefone)
                else -> null
            }
        )

        // E-mail: obrigatório e no formato algo@algo.algo.
        val emailOk = tilEmail.validar(
            when {
                tilEmail.texto.isEmpty() -> obrigatorio
                !tilEmail.texto.emailValido() -> activity.getString(R.string.erro_email)
                else -> null
            }
        )

        // Senha: obrigatória e com pelo menos 6 caracteres.
        val senhaOk = tilSenha.validar(
            when {
                tilSenha.texto.isEmpty() -> obrigatorio
                tilSenha.texto.length < 6 -> activity.getString(R.string.erro_senha_curta)
                else -> null
            }
        )

        // Confirmar senha: tem que ser igual à senha (evita erro de digitação).
        val confirmarOk = tilConfirmarSenha.validar(
            when {
                tilConfirmarSenha.texto.isEmpty() -> obrigatorio
                tilConfirmarSenha.texto != tilSenha.texto -> activity.getString(R.string.erro_senhas_diferentes)
                else -> null
            }
        )

        return nomeOk && telefoneOk && emailOk && senhaOk && confirmarOk
    }

    /**
     * Telefone só com os números, ex.: "(54) 99999-0000" -> "54999990000".
     * Por que: assim todos os telefones ficam salvos no mesmo formato.
     */
    val telefoneSomenteNumeros: String
        get() = tilTelefone.texto.filter { it.isDigit() }
}

/**
 * Usado pelos dois cadastros depois de salvar com sucesso:
 * volta para a tela de login, com o e-mail recém-cadastrado já preenchido.
 *
 * As flags fazem o seguinte:
 *  - CLEAR_TOP: fecha as telas que estão "em cima" do login (Escolha e Cadastro).
 *    Assim, se a pessoa apertar "voltar" no login, não volta para o cadastro.
 *  - SINGLE_TOP: reaproveita o login que já estava aberto, em vez de abrir outro.
 *    Por isso, lá no login, quem recebe o e-mail é o `onNewIntent`.
 */
fun Activity.voltarParaLoginComEmail(email: String) {
    val login = Intent(this, LoginActivity::class.java)
    login.putExtra(LoginActivity.EXTRA_EMAIL, email)   // "anexa" o e-mail ao pedido
    login.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    startActivity(login)
    finish()   // fecha a tela de cadastro
}
