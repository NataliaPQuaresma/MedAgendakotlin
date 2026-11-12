package com.example.medagenda.ui.principal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.medagenda.R
import com.example.medagenda.data.MedAgendaRepository
import com.example.medagenda.model.TipoUsuario
import com.example.medagenda.ui.comum.aplicarMargensDoSistema
import com.example.medagenda.ui.login.LoginActivity

/** Tela principal, aberta depois do login. */
class PrincipalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sem ninguém logado (ex.: o Android fechou o app e reabriu nesta tela): volta ao login.
        val usuario = MedAgendaRepository.usuarioLogado
        if (usuario == null) {
            irParaLogin()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        findViewById<TextView>(R.id.txtOla).text = getString(R.string.principal_ola, usuario.nome)
        findViewById<TextView>(R.id.txtTipo).setText(
            if (usuario.tipo == TipoUsuario.MEDICO) R.string.principal_tipo_medico
            else R.string.principal_tipo_paciente
        )

        findViewById<View>(R.id.btnSair).setOnClickListener {
            MedAgendaRepository.logout()
            irParaLogin()
        }
    }

    private fun irParaLogin() {
        val login = Intent(this, LoginActivity::class.java)
        login.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(login)
        finish()
    }
}
