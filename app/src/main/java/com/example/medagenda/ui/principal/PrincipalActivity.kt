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

/**
 * TELA PRINCIPAL (inicial depois do login)
 *
 * O que faz hoje:
 *  - Mostra "Olá, {nome}!" e se a pessoa entrou como médico ou paciente.
 *  - Botão "Sair": faz logout e volta para o login.
 *
 * Próximos passos: as funcionalidades seguintes (lista de médicos, agendamento,
 * consultas) vão partir daqui.
 *
 * De onde vêm os dados: do `MedAgendaRepository.usuarioLogado`, que foi
 * preenchido no momento do login.
 *
 * Layout: res/layout/activity_principal.xml
 */
class PrincipalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SEGURANÇA: só entra aqui quem está logado.
        // Se não houver ninguém logado (ex.: o Android fechou o app para liberar
        // memória e depois reabriu direto nesta tela), manda de volta para o login.
        val usuario = MedAgendaRepository.usuarioLogado
        if (usuario == null) {
            irParaLogin()
            return   // para aqui: não monta a tela
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        // "Olá, João Silva!": o %1$s do strings.xml é trocado pelo nome.
        findViewById<TextView>(R.id.txtOla).text = getString(R.string.principal_ola, usuario.nome)

        // Mostra uma frase diferente conforme o tipo de usuário.
        findViewById<TextView>(R.id.txtTipo).setText(
            if (usuario.tipo == TipoUsuario.MEDICO) R.string.principal_tipo_medico
            else R.string.principal_tipo_paciente
        )

        // Botão "Sair": esquece quem estava logado e volta para o login.
        findViewById<View>(R.id.btnSair).setOnClickListener {
            MedAgendaRepository.logout()
            irParaLogin()
        }
    }

    // Abre o login e apaga as telas anteriores, para o "voltar" não retornar
    // à tela principal depois de sair.
    private fun irParaLogin() {
        val login = Intent(this, LoginActivity::class.java)
        login.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(login)
        finish()
    }
}
