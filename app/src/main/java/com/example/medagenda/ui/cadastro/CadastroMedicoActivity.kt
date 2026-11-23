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

/**
 * TELA DE CADASTRO DO MÉDICO
 * Campos: nome, telefone, e-mail, senha, confirmar senha, CRM e especialidade.
 *
 * Caminho: EscolhaTipoActivity ("Sou médico") -> ESTA TELA -> volta para o Login
 *
 * Funciona igual ao cadastro do paciente, com duas diferenças:
 *  - tem os campos CRM e Especialidade (só do médico);
 *  - salva na tabela de MÉDICOS, que também não aceita CRM repetido.
 *
 * Layout: res/layout/activity_cadastro_medico.xml
 */
class CadastroMedicoActivity : AppCompatActivity() {

    private lateinit var campos: CamposComuns            // os 5 campos em comum com o paciente
    private lateinit var tilCrm: TextInputLayout          // campo exclusivo do médico
    private lateinit var tilEspecialidade: TextInputLayout // campo exclusivo do médico
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

    // Executado ao tocar em "Cadastrar".
    private fun cadastrar() {
        // 1) Confere os campos em comum E os campos do médico.
        //    Valida tudo antes de decidir, para mostrar todos os erros de uma vez.
        val obrigatorio = getString(R.string.erro_obrigatorio)
        val comunsOk = campos.validar()
        val crmOk = tilCrm.validar(if (tilCrm.texto.isEmpty()) obrigatorio else null)
        val especialidadeOk = tilEspecialidade.validar(if (tilEspecialidade.texto.isEmpty()) obrigatorio else null)
        if (!comunsOk || !crmOk || !especialidadeOk) return

        // 2) Monta o médico com o que foi digitado.
        val medico = Medico(
            nome = campos.tilNome.texto,
            telefone = campos.telefoneSomenteNumeros,
            email = campos.tilEmail.texto.lowercase(),   // e-mail em minúsculas
            crm = tilCrm.texto.uppercase(),              // CRM em maiúsculas (ex.: 12345-RS)
            especialidade = tilEspecialidade.texto,
            senha = campos.tilSenha.texto
        )

        btnCadastrar.isEnabled = false   // evita cadastrar duas vezes

        // 3) Envia ao repositório (tabela de médicos) sem travar a tela.
        lifecycleScope.launch {
            when (val resultado = MedAgendaRepository.cadastrarMedico(medico)) {
                is Resultado.Sucesso -> {
                    // 4a) Deu certo: avisa e volta para o login com o e-mail preenchido.
                    Toast.makeText(this@CadastroMedicoActivity, R.string.msg_cadastro_ok, Toast.LENGTH_LONG).show()
                    voltarParaLoginComEmail(resultado.dado.email)
                }
                is Resultado.Erro -> {
                    // 4b) Deu erro: e-mail ou CRM já cadastrado.
                    //     Como pode ser qualquer um dos dois, mostra num balão (Toast).
                    Toast.makeText(this@CadastroMedicoActivity, resultado.mensagem, Toast.LENGTH_LONG).show()
                    btnCadastrar.isEnabled = true
                }
            }
        }
    }
}
