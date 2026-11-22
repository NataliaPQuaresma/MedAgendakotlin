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
import com.example.medagenda.model.Medico
import com.example.medagenda.ui.comum.aplicarMargensDoSistema
import com.example.medagenda.ui.comum.texto
import com.example.medagenda.ui.comum.validar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/** Cadastro de médico: nome, telefone, e-mail, CRM, especialidade e senha. */
class CadastroMedicoActivity : AppCompatActivity() {

    private lateinit var campos: CamposComuns
    private lateinit var tilCrm: TextInputLayout
    private lateinit var tilEspecialidade: TextInputLayout
    private lateinit var btnCadastrar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro_medico)
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        campos = CamposComuns(this)
        tilCrm = findViewById(R.id.tilCrm)
        tilEspecialidade = findViewById(R.id.tilEspecialidade)
        btnCadastrar = findViewById(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener { cadastrar() }
        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
    }

    private fun cadastrar() {
        val obrigatorio = getString(R.string.erro_obrigatorio)
        val comunsOk = campos.validar()
        val crmOk = tilCrm.validar(if (tilCrm.texto.isEmpty()) obrigatorio else null)
        val especialidadeOk = tilEspecialidade.validar(if (tilEspecialidade.texto.isEmpty()) obrigatorio else null)
        if (!comunsOk || !crmOk || !especialidadeOk) return

        val medico = Medico(
            nome = campos.tilNome.texto,
            telefone = campos.telefoneSomenteNumeros,
            email = campos.tilEmail.texto.lowercase(),
            crm = tilCrm.texto.uppercase(),
            especialidade = tilEspecialidade.texto,
            senha = campos.tilSenha.texto
        )

        btnCadastrar.isEnabled = false
        lifecycleScope.launch {
            when (val resultado = MedAgendaRepository.cadastrarMedico(medico)) {
                is Resultado.Sucesso -> {
                    Toast.makeText(this@CadastroMedicoActivity, R.string.msg_cadastro_ok, Toast.LENGTH_LONG).show()
                    voltarParaLoginComEmail(resultado.dado.email)
                }
                is Resultado.Erro -> {
                    // E-mail ou CRM já cadastrado
                    Toast.makeText(this@CadastroMedicoActivity, resultado.mensagem, Toast.LENGTH_LONG).show()
                    btnCadastrar.isEnabled = true
                }
            }
        }
    }
}
