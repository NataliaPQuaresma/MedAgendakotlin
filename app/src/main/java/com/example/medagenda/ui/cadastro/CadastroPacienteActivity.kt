package com.example.medagenda.ui.cadastro

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
import kotlinx.coroutines.launch

/**
 * TELA DE CADASTRO DO PACIENTE
 * Campos: nome, telefone, e-mail, senha e confirmar senha.
 *
 * Caminho: EscolhaTipoActivity ("Sou paciente") -> ESTA TELA -> volta para o Login
 *
 * Como funciona:
 *  1) A pessoa preenche os campos e toca em "Cadastrar".
 *  2) Os campos são conferidos (CamposComuns.validar()).
 *  3) Um objeto Paciente é montado e enviado ao repositório (tabela de pacientes).
 *  4) Se deu certo, volta ao login com o e-mail preenchido. Se não, mostra o erro.
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
        setContentView(R.layout.activity_cadastro_paciente)
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
                    // 4a) Deu certo: avisa e volta para o login com o e-mail preenchido.
                    Toast.makeText(this@CadastroPacienteActivity, R.string.msg_cadastro_ok, Toast.LENGTH_LONG).show()
                    voltarParaLoginComEmail(resultado.dado.email)
                }
                is Resultado.Erro -> {
                    // 4b) Deu erro (o único possível aqui é e-mail já cadastrado):
                    //     mostra a mensagem embaixo do campo de e-mail e liga o botão de novo.
                    campos.tilEmail.error = resultado.mensagem
                    btnCadastrar.isEnabled = true
                }
            }
        }
    }
}
