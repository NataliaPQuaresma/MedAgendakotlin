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
 */
class CadastroPacienteActivity : AppCompatActivity() {

    private lateinit var campos: CamposComuns
    private lateinit var btnCadastrar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro_paciente)
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        campos = CamposComuns(this)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener { cadastrar() }
        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
    }

    private fun cadastrar() {
        if (!campos.validar()) return

        val paciente = Paciente(
            nome = campos.tilNome.texto,
            telefone = campos.telefoneSomenteNumeros,
            email = campos.tilEmail.texto.lowercase(),
            senha = campos.tilSenha.texto
        )

        btnCadastrar.isEnabled = false
        lifecycleScope.launch {
            when (val resultado = MedAgendaRepository.cadastrarPaciente(paciente)) {
                is Resultado.Sucesso -> {
                    Toast.makeText(this@CadastroPacienteActivity, R.string.msg_cadastro_ok, Toast.LENGTH_SHORT).show()
                    // Login automático e redirecionamento direto para a tela principal
                    MedAgendaRepository.login(paciente.email, paciente.senha)
                    val principal = Intent(this@CadastroPacienteActivity, PrincipalActivity::class.java)
                    principal.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(principal)
                    finish()
                }
                is Resultado.Erro -> {
                    // Ex.: e-mail já cadastrado — exibe o erro no próprio TextInputLayout
                    campos.tilEmail.error = resultado.mensagem
                    btnCadastrar.isEnabled = true
                }
            }
        }
    }
}
