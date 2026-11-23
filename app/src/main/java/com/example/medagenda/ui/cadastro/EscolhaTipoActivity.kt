package com.example.medagenda.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.medagenda.R
import com.example.medagenda.ui.comum.aplicarMargensDoSistema

/**
 * TELA DE ESCOLHA: "Sou paciente" ou "Sou médico".
 *
 * Por que existe: o projeto pede um cadastro diferente para cada tipo de usuário.
 * Esta tela só pergunta quem é a pessoa e abre o cadastro certo. Ela não salva nada.
 *
 * Caminho: LoginActivity (botão "Criar conta") -> ESTA TELA -> CadastroPaciente ou CadastroMedico
 *
 * Layout desta tela: res/layout/activity_escolha_tipo.xml
 */
class EscolhaTipoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_escolha_tipo)       // liga ao XML da tela
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        // Cartão "Sou paciente" -> abre o cadastro de paciente.
        findViewById<View>(R.id.cardPaciente).setOnClickListener {
            startActivity(Intent(this, CadastroPacienteActivity::class.java))
        }
        // Cartão "Sou médico" -> abre o cadastro de médico.
        findViewById<View>(R.id.cardMedico).setOnClickListener {
            startActivity(Intent(this, CadastroMedicoActivity::class.java))
        }
        // Botão "Voltar" -> `finish()` fecha esta tela e volta para o login.
        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
    }
}
