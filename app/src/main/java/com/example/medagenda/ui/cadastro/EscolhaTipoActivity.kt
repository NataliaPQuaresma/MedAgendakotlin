package com.example.medagenda.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.medagenda.R
import com.example.medagenda.ui.comum.aplicarMargensDoSistema

/** Pergunta se quem está criando a conta é paciente ou médico. */
class EscolhaTipoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_escolha_tipo)
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        findViewById<View>(R.id.cardPaciente).setOnClickListener {
            startActivity(Intent(this, CadastroPacienteActivity::class.java))
        }
        findViewById<View>(R.id.cardMedico).setOnClickListener {
            startActivity(Intent(this, CadastroMedicoActivity::class.java))
        }
        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
    }
}
