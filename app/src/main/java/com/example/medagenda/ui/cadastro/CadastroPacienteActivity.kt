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

/** Cadastro de paciente: nome, telefone, e-mail e senha. */
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
                    Toast.makeText(this@CadastroPacienteActivity, R.string.msg_cadastro_ok, Toast.LENGTH_LONG).show()
                    voltarParaLoginComEmail(resultado.dado.email)
                }
                is Resultado.Erro -> {
                    // Ex.: e-mail já cadastrado — mostra no próprio campo
                    campos.tilEmail.error = resultado.mensagem
                    btnCadastrar.isEnabled = true
                }
            }
        }
    }
}
