package com.example.medagenda.ui.agendamento

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.medagenda.R
import com.example.medagenda.data.MedAgendaRepository
import com.example.medagenda.data.Resultado
import com.example.medagenda.model.Consulta
import com.example.medagenda.model.Medico
import com.example.medagenda.ui.comum.aplicarMargensDoSistema
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * Tela de Agendamento de Consultas pelo Paciente.
 *
 *
 * 1. Fluxo Guiado em 5 etapas:
 *    - Passo 1: Seleção da Especialidade (filtro dinâmico).
 *    - Passo 2: Seleção do Médico especialista disponível.
 *    - Passo 3: Escolha do dia no CalendarView nativo.
 *    - Passo 4: Exibição apenas dos horários livres em Chips clicáveis (impede conflitos).
 *    - Passo 5: Informação do motivo e confirmação com gravação no repositório.
 * 2. Atualização Reativa: ao mudar especialidade, médico ou data, a lista de horários
 *    é recalculada em tempo real consultando a agenda daquele profissional.
 */
class AgendarConsultaActivity : AppCompatActivity() {

    private lateinit var actvEspecialidade: AutoCompleteTextView
    private lateinit var actvMedico: AutoCompleteTextView
    private lateinit var calendarViewAgendamento: CalendarView
    private lateinit var txtDataSelecionadaAgendamento: TextView
    private lateinit var chipGroupHorarios: ChipGroup
    private lateinit var txtSemHorarios: TextView
    private lateinit var edtMotivo: TextInputEditText
    private lateinit var btnConfirmarAgendamento: MaterialButton

    private var medicosFiltrados: List<Medico> = emptyList()
    private var medicoSelecionado: Medico? = null
    private var dataSelecionadaIso: String = ""
    private var horarioSelecionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificação de segurança: se a sessão expirou, encerra a tela
        val usuario = MedAgendaRepository.usuarioLogado
        if (usuario == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_agendar_consulta)
        findViewById<View>(R.id.main).aplicarMargensDoSistema()

        actvEspecialidade = findViewById(R.id.actvEspecialidade)
        actvMedico = findViewById(R.id.actvMedico)
        calendarViewAgendamento = findViewById(R.id.calendarViewAgendamento)
        txtDataSelecionadaAgendamento = findViewById(R.id.txtDataSelecionadaAgendamento)
        chipGroupHorarios = findViewById(R.id.chipGroupHorarios)
        txtSemHorarios = findViewById(R.id.txtSemHorarios)
        edtMotivo = findViewById(R.id.edtMotivo)
        btnConfirmarAgendamento = findViewById(R.id.btnConfirmarAgendamento)

        // Impede escolher datas que já passaram: os dias antes de hoje ficam
        // apagados e não podem ser tocados (igual ao calendário de remarcar).
        // O "- 1000" (1 segundo) garante que o próprio dia de hoje continue liberado.
        calendarViewAgendamento.minDate = System.currentTimeMillis() - 1000

        // Inicializa o calendário com o dia de hoje
        val hoje = Calendar.getInstance()
        definirData(
            hoje.get(Calendar.YEAR),
            hoje.get(Calendar.MONTH),
            hoje.get(Calendar.DAY_OF_MONTH)
        )

        // Evento de seleção de data no calendário
        calendarViewAgendamento.setOnDateChangeListener { _, year, month, dayOfMonth ->
            definirData(year, month, dayOfMonth)
            atualizarHorariosDisponiveis()
        }

        findViewById<View>(R.id.btnVoltar).setOnClickListener { finish() }
        btnConfirmarAgendamento.setOnClickListener { confirmarAgendamento() }

        carregarDadosIniciais()
    }

    /** Atualiza a data selecionada em formato ISO ("yyyy-MM-dd") e exibe em formato amigável ao usuário. */
    private fun definirData(ano: Int, mes: Int, dia: Int) {
        dataSelecionadaIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", ano, mes + 1, dia)
        val dataBr = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, ano)
        txtDataSelecionadaAgendamento.text = getString(R.string.agendar_data_selecionada, dataBr)
    }

    /** Carrega as especialidades cadastradas no repositório para o dropdown. */
    private fun carregarDadosIniciais() {
        lifecycleScope.launch {
            val todasEspecialidades = listOf(getString(R.string.agendar_todas_especialidades)) +
                MedAgendaRepository.listarEspecialidades()

            val adapterEsp = ArrayAdapter(
                this@AgendarConsultaActivity,
                android.R.layout.simple_dropdown_item_1line,
                todasEspecialidades
            )
            actvEspecialidade.setAdapter(adapterEsp)
            actvEspecialidade.setText(todasEspecialidades.first(), false)

            carregarMedicosPorEspecialidade(todasEspecialidades.first())

            // Ao mudar a especialidade, filtra a lista de médicos abaixo
            actvEspecialidade.setOnItemClickListener { _, _, position, _ ->
                val espEscolhida = todasEspecialidades[position]
                carregarMedicosPorEspecialidade(espEscolhida)
            }
        }
    }

    /** Filtra médicos no repositório de acordo com a especialidade selecionada. */
    private fun carregarMedicosPorEspecialidade(especialidade: String) {
        lifecycleScope.launch {
            medicosFiltrados = MedAgendaRepository.listarMedicosPorEspecialidade(especialidade)

            val nomesMedicos = medicosFiltrados.map {
                "Dr(a). ${it.nome} (${it.especialidade} • CRM ${it.crm})"
            }

            val adapterMed = ArrayAdapter(
                this@AgendarConsultaActivity,
                android.R.layout.simple_dropdown_item_1line,
                nomesMedicos
            )
            actvMedico.setAdapter(adapterMed)

            if (medicosFiltrados.isNotEmpty()) {
                actvMedico.setText(nomesMedicos.first(), false)
                medicoSelecionado = medicosFiltrados.first()
                atualizarHorariosDisponiveis()
            } else {
                actvMedico.text.clear()
                medicoSelecionado = null
                atualizarHorariosDisponiveis()
            }

            actvMedico.setOnItemClickListener { _, _, pos, _ ->
                medicoSelecionado = medicosFiltrados.getOrNull(pos)
                atualizarHorariosDisponiveis()
            }
        }
    }

    /**
     * Consulta a agenda do médico selecionado para a data escolhida e renderiza
     * apenas os horários livres como Chips clicáveis.
     */
    private fun atualizarHorariosDisponiveis() {
        chipGroupHorarios.removeAllViews()
        horarioSelecionado = null

        val medico = medicoSelecionado
        if (medico == null) {
            txtSemHorarios.text = getString(R.string.erro_selecione_medico)
            txtSemHorarios.visibility = View.VISIBLE
            return
        }

        lifecycleScope.launch {
            val horarios = MedAgendaRepository.obterHorariosDisponiveis(medico.id, dataSelecionadaIso)
            if (horarios.isEmpty()) {
                txtSemHorarios.text = getString(R.string.agendar_sem_horarios)
                txtSemHorarios.visibility = View.VISIBLE
            } else {
                txtSemHorarios.visibility = View.GONE
                for (h in horarios) {
                    val chip = Chip(this@AgendarConsultaActivity).apply {
                        text = h
                        isCheckable = true
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                horarioSelecionado = h
                            } else if (horarioSelecionado == h) {
                                horarioSelecionado = null
                            }
                        }
                    }
                    chipGroupHorarios.addView(chip)
                }
            }
        }
    }

    /** Valida as seleções e persiste o novo agendamento no repositório. */
    private fun confirmarAgendamento() {
        val usuario = MedAgendaRepository.usuarioLogado ?: return
        val medico = medicoSelecionado

        if (medico == null) {
            Toast.makeText(this, R.string.erro_selecione_medico, Toast.LENGTH_SHORT).show()
            return
        }

        val horario = horarioSelecionado
        if (horario.isNullOrEmpty()) {
            Toast.makeText(this, R.string.erro_selecione_horario, Toast.LENGTH_SHORT).show()
            return
        }

        val motivo = edtMotivo.text?.toString()?.trim().let {
            if (it.isNullOrEmpty()) "Consulta de rotina" else it
        }

        val consulta = Consulta(
            medicoId = medico.id,
            medicoNome = "Dr(a). ${medico.nome}",
            medicoEspecialidade = medico.especialidade,
            pacienteId = usuario.id,
            pacienteNome = usuario.nome,
            data = dataSelecionadaIso,
            horario = horario,
            tipoConsulta = motivo
        )

        btnConfirmarAgendamento.isEnabled = false
        lifecycleScope.launch {
            when (val resultado = MedAgendaRepository.agendarConsulta(consulta)) {
                is Resultado.Sucesso -> {
                    Toast.makeText(this@AgendarConsultaActivity, R.string.msg_consulta_agendada_sucesso, Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resultado.Erro -> {
                    Toast.makeText(this@AgendarConsultaActivity, resultado.mensagem, Toast.LENGTH_LONG).show()
                    btnConfirmarAgendamento.isEnabled = true
                }
            }
        }
    }
}
