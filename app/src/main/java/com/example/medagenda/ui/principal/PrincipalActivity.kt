package com.example.medagenda.ui.principal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.medagenda.R
import com.example.medagenda.data.MedAgendaRepository
import com.example.medagenda.data.Resultado
import com.example.medagenda.model.Consulta
import com.example.medagenda.model.TipoUsuario
import com.example.medagenda.ui.agendamento.AgendarConsultaActivity
import com.example.medagenda.ui.comum.aplicarMargensDoSistema
import com.example.medagenda.ui.login.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * Tela Principal do MedAgenda.
 *
 *
 * 1. Interface Adaptativa por Perfil de Usuário:
 *    - Se Médico: exibe o painel de agenda com CalendarView, filtro de consultas do dia
 *      e lista cronológica de todos os pacientes agendados.
 *    - Se Paciente: exibe atalho direto para agendar nova consulta e a lista de suas consultas ativas,
 *      com opção de editar data/horário ou cancelar com um toque.
 * 2. Ciclo de Vida Reativo (onResume):
 *    - Sempre que o paciente volta da tela de agendamento ou o médico volta para o app,
 *      as consultas são recarregadas do repositório garantindo que a tela sempre reflita
 *      o estado mais atualizado dos dados.
 *
 * EM PALAVRAS SIMPLES:
 * - É a tela que abre depois do login (ou depois do cadastro do paciente).
 * - Mostra "Olá, {nome}!" e, conforme o tipo de usuário, a agenda do médico
 *   ou as consultas do paciente.
 * - Os dados de quem entrou vêm do `MedAgendaRepository.usuarioLogado`,
 *   preenchido no momento do login.
 * - Layout: res/layout/activity_principal.xml
 */
class PrincipalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificação de autenticação: se a sessão for nula, redireciona ao Login
        // (ex.: o Android fechou o app para liberar memória e reabriu direto nesta tela)
        val usuario = MedAgendaRepository.usuarioLogado
        if (usuario == null) {
            irParaLogin()
            return   // para aqui: não monta a tela
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)       // liga ao XML da tela
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        // "Olá, João Silva!": o %1$s do strings.xml é trocado pelo nome.
        findViewById<TextView>(R.id.txtOla).text = getString(R.string.principal_ola, usuario.nome)
        // Frase diferente conforme o tipo de usuário (médico ou paciente).
        findViewById<TextView>(R.id.txtTipo).setText(
            if (usuario.tipo == TipoUsuario.MEDICO) R.string.principal_tipo_medico
            else R.string.principal_tipo_paciente
        )

        val layoutAgendaMedico = findViewById<LinearLayout>(R.id.layoutAgendaMedico)
        val layoutPaciente = findViewById<LinearLayout>(R.id.layoutPaciente)

        // Roteamento de visualização baseado no perfil do usuário logado
        if (usuario.tipo == TipoUsuario.MEDICO) {
            layoutAgendaMedico.visibility = View.VISIBLE
            layoutPaciente.visibility = View.GONE
            carregarAgendaMedico(usuario.id)
        } else {
            layoutAgendaMedico.visibility = View.GONE
            layoutPaciente.visibility = View.VISIBLE
            findViewById<MaterialButton>(R.id.btnNovaConsulta).setOnClickListener {
                startActivity(Intent(this, AgendarConsultaActivity::class.java))
            }
            carregarConsultasPaciente(usuario.id)
        }

        // Botão "Sair": esquece quem estava logado e volta para o login.
        findViewById<View>(R.id.btnSair).setOnClickListener {
            MedAgendaRepository.logout()
            irParaLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza a lista automaticamente ao retornar de outra tela (ex.: após agendar)
        val usuario = MedAgendaRepository.usuarioLogado
        if (usuario != null) {
            if (usuario.tipo == TipoUsuario.PACIENTE) {
                carregarConsultasPaciente(usuario.id)
            } else {
                carregarAgendaMedico(usuario.id)
            }
        }
    }

    /**
     * Carrega as consultas do paciente logado.
     * Cada item inflado é clicável para abrir o diálogo de gerenciamento (remarcar ou cancelar).
     */
    private fun carregarConsultasPaciente(pacienteId: Int) {
        val containerConsultasPaciente = findViewById<LinearLayout>(R.id.containerConsultasPaciente)
        val txtSemConsultasPaciente = findViewById<TextView>(R.id.txtSemConsultasPaciente)

        lifecycleScope.launch {
            val consultas = MedAgendaRepository.listarConsultasDoPaciente(pacienteId)
            avisarSeSemApi()
            containerConsultasPaciente.removeAllViews()

            if (consultas.isEmpty()) {
                txtSemConsultasPaciente.visibility = View.VISIBLE
            } else {
                txtSemConsultasPaciente.visibility = View.GONE
                val inflater = LayoutInflater.from(this@PrincipalActivity)
                for (consulta in consultas) {
                    val itemView = inflater.inflate(R.layout.item_consulta, containerConsultasPaciente, false)
                    val partes = consulta.data.split("-")
                    val dataFormatada = if (partes.size == 3) "${partes[2]}/${partes[1]}" else consulta.data
                    val txtData = itemView.findViewById<TextView>(R.id.txtItemData)
                    txtData.visibility = View.VISIBLE
                    txtData.text = dataFormatada

                    itemView.findViewById<TextView>(R.id.txtItemHorario).text = consulta.horario
                    itemView.findViewById<TextView>(R.id.txtItemNomePaciente).text =
                        if (consulta.medicoNome.isNotEmpty()) consulta.medicoNome else getString(R.string.agenda_paciente_nome, consulta.pacienteNome)
                    itemView.findViewById<TextView>(R.id.txtItemDetalhes).text =
                        if (consulta.medicoEspecialidade.isNotEmpty()) {
                            "${consulta.medicoEspecialidade} • ${consulta.tipoConsulta}"
                        } else {
                            consulta.tipoConsulta
                        }

                    // Permite ao paciente clicar no card para editar ou cancelar
                    itemView.isClickable = true
                    itemView.isFocusable = true
                    itemView.setOnClickListener {
                        mostrarDialogEditarConsulta(consulta, pacienteId)
                    }

                    containerConsultasPaciente.addView(itemView)
                }
            }
        }
    }

    /**
     * Abre diálogo modal para o paciente:
     * - Remarcar: escolhe nova data no DatePickerDialog nativo e novo horário livre em Chips.
     * - Cancelar: remove o agendamento com confirmação de segurança.
     * As alterações sincronizam imediatamente com a agenda do médico correspondente.
     */
    private fun mostrarDialogEditarConsulta(consulta: Consulta, pacienteId: Int) {
        val viewDialog = layoutInflater.inflate(R.layout.dialog_editar_consulta, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(viewDialog)
            .create()

        val txtDialogMedico = viewDialog.findViewById<TextView>(R.id.txtDialogMedico)
        val txtDialogAtual = viewDialog.findViewById<TextView>(R.id.txtDialogAtual)
        val btnEscolherNovaData = viewDialog.findViewById<MaterialButton>(R.id.btnEscolherNovaData)
        val txtNovaDataEscolhida = viewDialog.findViewById<TextView>(R.id.txtNovaDataEscolhida)
        val txtLabelHorariosLivres = viewDialog.findViewById<TextView>(R.id.txtLabelHorariosLivres)
        val chipGroupNovosHorarios = viewDialog.findViewById<ChipGroup>(R.id.chipGroupNovosHorarios)
        val txtSemHorariosDialog = viewDialog.findViewById<TextView>(R.id.txtSemHorariosDialog)
        val btnConfirmarRemarcacao = viewDialog.findViewById<MaterialButton>(R.id.btnConfirmarRemarcacao)
        val btnCancelarConsulta = viewDialog.findViewById<MaterialButton>(R.id.btnCancelarConsulta)
        val btnFecharDialog = viewDialog.findViewById<MaterialButton>(R.id.btnFecharDialog)

        txtDialogMedico.text = "${consulta.medicoNome} • ${consulta.medicoEspecialidade}"
        val partesAtual = consulta.data.split("-")
        val dataBrAtual = if (partesAtual.size == 3) "${partesAtual[2]}/${partesAtual[1]}/${partesAtual[0]}" else consulta.data
        txtDialogAtual.text = getString(R.string.gerenciar_agendado_para, dataBrAtual, consulta.horario)

        var novaDataIso: String? = null
        var novoHorarioEscolhido: String? = null

        // Abre o seletor nativo de calendário (DatePickerDialog)
        btnEscolherNovaData.setOnClickListener {
            val cal = Calendar.getInstance()
            if (partesAtual.size == 3) {
                cal.set(partesAtual[0].toInt(), partesAtual[1].toInt() - 1, partesAtual[2].toInt())
            }

            val dpd = DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    val dataIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", ano, mes + 1, dia)
                    val dataBr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, ano)
                    novaDataIso = dataIso
                    novoHorarioEscolhido = null

                    txtNovaDataEscolhida.text = getString(R.string.gerenciar_nova_data_selecionada, dataBr)
                    txtNovaDataEscolhida.visibility = View.VISIBLE
                    txtLabelHorariosLivres.visibility = View.VISIBLE
                    btnConfirmarRemarcacao.visibility = View.GONE
                    chipGroupNovosHorarios.removeAllViews()

                    // Busca horários livres do médico na nova data escolhida
                    lifecycleScope.launch {
                        val horarios = MedAgendaRepository.obterHorariosDisponiveis(consulta.medicoId, dataIso)
                        if (horarios.isEmpty()) {
                            txtSemHorariosDialog.visibility = View.VISIBLE
                        } else {
                            txtSemHorariosDialog.visibility = View.GONE
                            for (h in horarios) {
                                val chip = Chip(this@PrincipalActivity).apply {
                                    text = h
                                    isCheckable = true
                                    setOnCheckedChangeListener { _, isChecked ->
                                        if (isChecked) {
                                            novoHorarioEscolhido = h
                                            btnConfirmarRemarcacao.visibility = View.VISIBLE
                                        } else if (novoHorarioEscolhido == h) {
                                            novoHorarioEscolhido = null
                                            btnConfirmarRemarcacao.visibility = View.GONE
                                        }
                                    }
                                }
                                chipGroupNovosHorarios.addView(chip)
                            }
                        }
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            // Impede agendamento em datas passadas
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000
            dpd.show()
        }

        // Confirmação da alteração de data e horário
        btnConfirmarRemarcacao.setOnClickListener {
            val data = novaDataIso
            val hora = novoHorarioEscolhido
            if (data == null || hora == null) {
                Toast.makeText(this, R.string.erro_selecione_horario, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                when (val res = MedAgendaRepository.remarcarConsulta(consulta.id, data, hora)) {
                    is Resultado.Sucesso -> {
                        Toast.makeText(this@PrincipalActivity, R.string.gerenciar_consulta_remarcada, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        carregarConsultasPaciente(pacienteId)
                    }
                    is Resultado.Erro -> {
                        Toast.makeText(this@PrincipalActivity, res.mensagem, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Cancelamento da consulta com confirmação do usuário
        btnCancelarConsulta.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.gerenciar_confirmar_cancelamento_titulo)
                .setMessage(R.string.gerenciar_confirmar_cancelamento_msg)
                .setPositiveButton(R.string.gerenciar_sim_cancelar) { _, _ ->
                    lifecycleScope.launch {
                        when (val res = MedAgendaRepository.cancelarConsulta(consulta.id)) {
                            is Resultado.Sucesso -> {
                                Toast.makeText(this@PrincipalActivity, R.string.gerenciar_consulta_cancelada, Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                carregarConsultasPaciente(pacienteId)
                            }
                            is Resultado.Erro -> {
                                Toast.makeText(this@PrincipalActivity, res.mensagem, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .setNegativeButton(R.string.gerenciar_nao, null)
                .show()
        }

        btnFecharDialog.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /**
     * Configura a tela do médico:
     * - CalendarView interativo: ao tocar em um dia, filtra as consultas daquele dia.
     * - Lista de todas as consultas agendadas com atalho de clique para selecionar o dia no calendário.
     */
    private fun carregarAgendaMedico(medicoId: Int) {
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val txtDataSelecionada = findViewById<TextView>(R.id.txtDataSelecionada)
        val containerConsultasDoDia = findViewById<LinearLayout>(R.id.containerConsultasDoDia)
        val txtSemConsultasDia = findViewById<TextView>(R.id.txtSemConsultasDia)
        val txtTituloProximas = findViewById<TextView>(R.id.txtTituloProximas)
        val containerTodasConsultas = findViewById<LinearLayout>(R.id.containerTodasConsultas)

        lifecycleScope.launch {
            val consultas = MedAgendaRepository.listarConsultasDoMedico(medicoId)
            avisarSeSemApi()

            val atualizarDia = { ano: Int, mes: Int, dia: Int ->
                val dataIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", ano, mes + 1, dia)
                val dataBr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, ano)
                txtDataSelecionada.text = getString(R.string.agenda_consultas_do_dia, dataBr)

                val consultasDoDia = consultas.filter { it.data == dataIso }
                containerConsultasDoDia.removeAllViews()

                if (consultasDoDia.isEmpty()) {
                    txtSemConsultasDia.visibility = View.VISIBLE
                } else {
                    txtSemConsultasDia.visibility = View.GONE
                    val inflater = LayoutInflater.from(this@PrincipalActivity)
                    for (consulta in consultasDoDia) {
                        val itemView = inflater.inflate(R.layout.item_consulta, containerConsultasDoDia, false)
                        itemView.findViewById<TextView>(R.id.txtItemHorario).text = consulta.horario
                        itemView.findViewById<TextView>(R.id.txtItemNomePaciente).text =
                            getString(R.string.agenda_paciente_nome, consulta.pacienteNome)
                        itemView.findViewById<TextView>(R.id.txtItemDetalhes).text =
                            if (consulta.pacienteTelefone.isNotEmpty()) {
                                "${consulta.tipoConsulta} • ${consulta.pacienteTelefone}"
                            } else {
                                consulta.tipoConsulta
                            }
                        containerConsultasDoDia.addView(itemView)
                    }
                }
            }

            // Exibe a lista de todas as consultas agendadas do médico
            containerTodasConsultas.removeAllViews()
            if (consultas.isEmpty()) {
                txtTituloProximas.visibility = View.GONE
                containerTodasConsultas.visibility = View.GONE
            } else {
                txtTituloProximas.visibility = View.VISIBLE
                containerTodasConsultas.visibility = View.VISIBLE
                val inflater = LayoutInflater.from(this@PrincipalActivity)
                for (consulta in consultas) {
                    val itemView = inflater.inflate(R.layout.item_consulta, containerTodasConsultas, false)
                    val partes = consulta.data.split("-")
                    val dataFormatada = if (partes.size == 3) "${partes[2]}/${partes[1]}" else consulta.data
                    val txtData = itemView.findViewById<TextView>(R.id.txtItemData)
                    txtData.visibility = View.VISIBLE
                    txtData.text = dataFormatada

                    itemView.findViewById<TextView>(R.id.txtItemHorario).text = consulta.horario
                    itemView.findViewById<TextView>(R.id.txtItemNomePaciente).text =
                        getString(R.string.agenda_paciente_nome, consulta.pacienteNome)
                    itemView.findViewById<TextView>(R.id.txtItemDetalhes).text =
                        if (consulta.pacienteTelefone.isNotEmpty()) {
                            "${consulta.tipoConsulta} • ${consulta.pacienteTelefone}"
                        } else {
                            consulta.tipoConsulta
                        }

                    // Ao tocar em um paciente na lista, o calendário salta para a data da consulta
                    itemView.setOnClickListener {
                        if (partes.size == 3) {
                            val ano = partes[0].toInt()
                            val mes = partes[1].toInt() - 1
                            val dia = partes[2].toInt()
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, ano)
                                set(Calendar.MONTH, mes)
                                set(Calendar.DAY_OF_MONTH, dia)
                            }
                            calendarView.date = cal.timeInMillis
                            atualizarDia(ano, mes, dia)
                        }
                    }
                    containerTodasConsultas.addView(itemView)
                }
            }

            // Ouve cliques nas células de dias do calendário
            calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                atualizarDia(year, month, dayOfMonth)
            }

            // Inicializa com o dia atual selecionado
            val hoje = Calendar.getInstance()
            atualizarDia(
                hoje.get(Calendar.YEAR),
                hoje.get(Calendar.MONTH),
                hoje.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    // Se o repositório não conseguiu falar com a API, avisa (uma vez só) que o app
    // está mostrando os dados de exemplo da memória.
    private fun avisarSeSemApi() {
        if (MedAgendaRepository.precisaAvisarSemApi()) {
            Toast.makeText(this, R.string.aviso_sem_api, Toast.LENGTH_LONG).show()
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
