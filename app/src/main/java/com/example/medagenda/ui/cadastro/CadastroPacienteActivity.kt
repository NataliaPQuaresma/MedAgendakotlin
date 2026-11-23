package com.example.medagenda.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.medagenda.R
import com.example.medagenda.data.MedAgendaRepository
import com.example.medagenda.data.Resultado
import com.example.medagenda.model.Paciente
import com.example.medagenda.ui.comum.aplicarMargensDoSistema
import com.example.medagenda.ui.comum.texto
import com.example.medagenda.ui.principal.PrincipalActivity
import kotlinx.coroutines.launch

/**
 * Cadastro de Paciente no MedAgenda.
 *
 * PONTO PARA A APRESENTAÇÃO:
 * - Experiência fluida (UX): ao concluir o cadastro com sucesso, o app efetua
 *   o login automático do paciente e o direciona diretamente para a tela principal
 *   onde ele já tem acesso imediato ao botão "Agendar Consulta", sem fricção de ter
 *   que digitar as credenciais novamente.
 *
 * EM PALAVRAS SIMPLES:
 * Campos: nome, telefone, e-mail, senha e confirmar senha.
 * Caminho: EscolhaTipoActivity ("Sou paciente") -> ESTA TELA -> Tela Principal (já logado)
 *  1) A pessoa preenche os campos e toca em "Cadastrar".
 *  2) Os campos são conferidos (CamposComuns.validar()).
 *  3) Um Paciente é montado e salvo no repositório (tabela de pacientes).
 *  4) Se deu certo, faz o login automático e abre a Principal. Se não, mostra o erro.
 *
 * Layout: res/layout/activity_cadastro_paciente.xml
 * (os campos vêm de campos_comuns_cadastro.xml, incluído dentro dele)
 */
class CadastroPacienteActivity : AppCompatActivity() {

    private lateinit var campos: CamposComuns   // os 5 campos e a validação deles
    private lateinit var btnCadastrar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro_paciente)       // liga ao XML da tela
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        campos = CamposComuns(this)                  // encontra os campos nesta tela
        btnCadastrar = findViewById(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener { cadastrar() }
        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }   // volta para a escolha
    }

    // Executado ao tocar em "Cadastrar".
    private fun cadastrar() {
        // 1) Se algum campo estiver errado, para aqui (os erros já aparecem na tela).
        if (!campos.validar()) return

        // 2) Monta o paciente com o que foi digitado.
        //    O id não é informado: o repositório gera na hora de salvar.
        val paciente = Paciente(
            nome = campos.tilNome.texto,
            telefone = campos.telefoneSomenteNumeros,
            email = campos.tilEmail.texto.lowercase(),   // e-mail sempre em minúsculas, para padronizar
            senha = campos.tilSenha.texto
        )

        // Desliga o botão para não cadastrar duas vezes com toque duplo.
        btnCadastrar.isEnabled = false

        // 3) Envia ao repositório sem travar a tela (função `suspend`).
        lifecycleScope.launch {
            when (val resultado = MedAgendaRepository.cadastrarPaciente(paciente)) {
                is Resultado.Sucesso -> {
                    // 4a) Deu certo: avisa com um balão (Toast)...
                    Toast.makeText(this@CadastroPacienteActivity, R.string.msg_cadastro_ok, Toast.LENGTH_SHORT).show()
                    // Login automático e redirecionamento direto para a tela principal
                    // ...faz o login com o e-mail e a senha que acabou de cadastrar...
                    MedAgendaRepository.login(paciente.email, paciente.senha)
                    // ...e abre a Principal. As flags apagam as telas anteriores (login,
                    // escolha, cadastro), para o "voltar" não retornar ao cadastro.
                    val principal = Intent(this@CadastroPacienteActivity, PrincipalActivity::class.java)
                    principal.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(principal)
                    finish()
                }
                is Resultado.Erro -> {
                    // Ex.: e-mail já cadastrado — exibe o erro no próprio TextInputLayout
                    // 4b) Mostra a mensagem embaixo do campo de e-mail e liga o botão de novo.
                    campos.tilEmail.error = resultado.mensagem
                    btnCadastrar.isEnabled = true
                }
            }
        }
    }
}
