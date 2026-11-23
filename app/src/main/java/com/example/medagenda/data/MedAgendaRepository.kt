package com.example.medagenda.data

import com.example.medagenda.model.Consulta
import com.example.medagenda.model.Medico
import com.example.medagenda.model.Paciente
import com.example.medagenda.model.TipoUsuario
import com.example.medagenda.model.UsuarioLogado
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Ponto único de acesso aos dados do app (Padrão Repository).
 *
 * PARA A APRESENTAÇÃO:
 * - O app utiliza o Repository Pattern como camada de abstração entre as Activities/Telas
 *   e a fonte de dados.
 * - Atualmente, os dados são gerenciados em memória com métodos `suspend` assíncronos
 *   (Kotlin Coroutines). Isso simula o comportamento exato de uma API REST na nuvem.
 * - O ganho arquitetural: quando o backend definitivo (ex.: Retrofit + Spring/Node) for conectado,
 *   nenhuma tela precisará ser alterada; bastará trocar as listas em memória pelas chamadas HTTP.
 *
 * Rotas mapeadas para a futura API REST:
 *  - POST /pacientes                  -> cadastrarPaciente (cria novo paciente)
 *  - GET  /pacientes                  -> listarPacientes
 *  - POST /medicos                    -> cadastrarMedico (cria novo médico)
 *  - GET  /medicos                    -> listarMedicos
 *  - POST /login                      -> login (autenticação de médico ou paciente)
 *  - GET  /consultas?medicoId={id}    -> listarConsultasDoMedico (agenda do médico)
 *  - GET  /consultas?pacienteId={id}  -> listarConsultasDoPaciente (consultas do paciente)
 *  - POST /consultas                  -> agendarConsulta (validação de horário livre)
 *  - PUT  /consultas/{id}             -> remarcarConsulta (alteração de data/horário)
 *  - DELETE /consultas/{id}           -> cancelarConsulta (exclusão de agendamento)
 *
 * EM PALAVRAS SIMPLES:
 * - É o "banco de dados" do app: o ÚNICO lugar que guarda e busca dados.
 *   As telas nunca mexem nos dados direto; sempre pedem para o repositório.
 * - Por enquanto os dados ficam em listas na memória (somem ao fechar o app).
 * - Por que `object` e não `class`: existe UM só repositório no app inteiro.
 *   Assim, o paciente cadastrado numa tela é o mesmo que a tela de login encontra depois.
 * - Por que `suspend`: chamar uma API demora; uma função `suspend` pode esperar
 *   sem travar a tela. Já deixamos assim para a API encaixar sem mudar as telas.
 *
 * Liga com: Login, Cadastros, Principal e Agendar Consulta.
 */
object MedAgendaRepository {

    /**
     * Gera datas dinâmicas a partir da data atual do dispositivo.
     * Garante que na apresentação sempre haverá consultas para hoje, amanhã e próximos dias.
     */
    private fun dataOffset(dias: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, dias)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    /** Lista de pacientes cadastrados (simulando a tabela 'pacientes' do banco de dados). */
    // `mutableList` = lista que pode crescer (cada cadastro novo entra aqui).
    // `private` = só o repositório mexe nesta lista; as telas não têm acesso direto.
    // Já começa com pacientes de teste, para conseguir fazer login logo de cara.
    private val pacientes = mutableListOf(
        Paciente(1, "João Silva", "54999990000", "teste@email.com", "123456"),
        Paciente(2, "Mariana Costa", "54988776655", "mariana@email.com", "123456"),
        Paciente(3, "Lucas Pereira", "54977665544", "lucas@email.com", "123456"),
        Paciente(4, "Ana Souza", "54966554433", "ana@email.com", "123456"),
    )

    /** Lista de médicos cadastrados (simulando a tabela 'medicos' com múltiplas especialidades). */
    // Separada da de pacientes, como pedido no projeto (cada um tem dados diferentes).
    private val medicos = mutableListOf(
        Medico(1, "Carlos Santos", "54988880000", "medico@email.com", "12345-RS", "Cardiologia", "123456"),
        Medico(2, "Fernanda Lima", "54988880001", "fernanda@email.com", "23456-RS", "Clínica Geral", "123456"),
        Medico(3, "Roberto Matos", "54988880002", "roberto@email.com", "34567-RS", "Ortopedia", "123456"),
        Medico(4, "Camila Duarte", "54988880003", "camila@email.com", "45678-RS", "Dermatologia", "123456"),
    )

    /** Lista de consultas agendadas (compartilhada em tempo real entre médicos e pacientes). */
    private val consultas = mutableListOf(
        Consulta(
            id = 1,
            medicoId = 1,
            medicoNome = "Dr. Carlos Santos",
            medicoEspecialidade = "Cardiologia",
            pacienteId = 1,
            pacienteNome = "João Silva",
            pacienteTelefone = "(54) 99999-0000",
            data = dataOffset(0), // Hoje
            horario = "09:00",
            tipoConsulta = "Primeira Consulta",
        ),
        Consulta(
            id = 2,
            medicoId = 1,
            medicoNome = "Dr. Carlos Santos",
            medicoEspecialidade = "Cardiologia",
            pacienteId = 2,
            pacienteNome = "Mariana Costa",
            pacienteTelefone = "(54) 98877-6655",
            data = dataOffset(0), // Hoje
            horario = "14:30",
            tipoConsulta = "Retorno Cardiológico",
        ),
        Consulta(
            id = 3,
            medicoId = 1,
            medicoNome = "Dr. Carlos Santos",
            medicoEspecialidade = "Cardiologia",
            pacienteId = 3,
            pacienteNome = "Lucas Pereira",
            pacienteTelefone = "(54) 97766-5544",
            data = dataOffset(1), // Amanhã
            horario = "10:00",
            tipoConsulta = "Avaliação de Exames",
        ),
        Consulta(
            id = 4,
            medicoId = 1,
            medicoNome = "Dr. Carlos Santos",
            medicoEspecialidade = "Cardiologia",
            pacienteId = 4,
            pacienteNome = "Ana Souza",
            pacienteTelefone = "(54) 96655-4433",
            data = dataOffset(3), // Daqui a 3 dias
            horario = "15:00",
            tipoConsulta = "Consulta de Rotina",
        ),
    )

    /** Usuário atualmente autenticado na sessão ativa do app. */
    // `null` = ninguém logado.
    // `private set` = qualquer tela pode LER, mas só o repositório pode MUDAR
    // (no login e no logout). Evita que uma tela "logue" alguém por engano.
    var usuarioLogado: UsuarioLogado? = null
        private set

    /** Validação de unicidade de e-mail entre pacientes e médicos. */
    // Confere nas DUAS tabelas: a mesma pessoa não pode ter o mesmo e-mail como médico
    // e como paciente, senão o login não saberia qual conta abrir.
    // `ignoreCase = true` = trata "Teste@Email.com" e "teste@email.com" como iguais.
    private fun emailJaExiste(email: String): Boolean =
        pacientes.any { it.email.equals(email, ignoreCase = true) } ||
            medicos.any { it.email.equals(email, ignoreCase = true) }

    /** Cadastra novo paciente após validar se o e-mail não foi utilizado. */
    // Chamada pela CadastroPacienteActivity (futuro: POST /pacientes).
    suspend fun cadastrarPaciente(paciente: Paciente): Resultado<Paciente> {
        // 1) E-mail repetido? Não cadastra e devolve o erro para a tela mostrar.
        if (emailJaExiste(paciente.email)) return Resultado.Erro("Este e-mail já está cadastrado.")
        // 2) Gera o próximo id: maior id da lista + 1 (`?: 0` = se a lista estiver vazia, usa 0).
        //    `copy` cria uma cópia igual do paciente, só que com o id novo.
        val novo = paciente.copy(id = (pacientes.maxOfOrNull { it.id } ?: 0) + 1)
        // 3) Salva na "tabela" e avisa a tela que deu certo.
        pacientes.add(novo)
        return Resultado.Sucesso(novo)
    }

    /** Cadastra novo médico após validar e-mail e CRM. */
    // Chamada pela CadastroMedicoActivity (futuro: POST /medicos).
    // Igual ao paciente, mas também confere o CRM, que é único para cada médico.
    suspend fun cadastrarMedico(medico: Medico): Resultado<Medico> {
        if (emailJaExiste(medico.email)) return Resultado.Erro("Este e-mail já está cadastrado.")
        if (medicos.any { it.crm.equals(medico.crm, ignoreCase = true) }) {
            return Resultado.Erro("Este CRM já está cadastrado.")
        }
        val novo = medico.copy(id = (medicos.maxOfOrNull { it.id } ?: 0) + 1)
        medicos.add(novo)
        return Resultado.Sucesso(novo)
    }

    // LISTAR (futuro: GET /pacientes e GET /medicos): servem para consultar as tabelas.
    // `toList()` devolve uma CÓPIA, para ninguém de fora alterar a lista original.
    suspend fun listarPacientes(): List<Paciente> = pacientes.toList()

    suspend fun listarMedicos(): List<Medico> = medicos.toList()

    /** Retorna as especialidades distintas existentes entre os médicos cadastrados. */
    suspend fun listarEspecialidades(): List<String> =
        medicos.map { it.especialidade }.distinct().sorted()

    /**
     * Filtra médicos por especialidade selecionada.
     * Se vazio ou "Todas", retorna todos os profissionais disponíveis.
     */
    suspend fun listarMedicosPorEspecialidade(especialidade: String): List<Medico> =
        if (especialidade.isBlank() || especialidade.equals("Todas as especialidades", ignoreCase = true)) {
            medicos.toList()
        } else {
            medicos.filter { it.especialidade.equals(especialidade, ignoreCase = true) }
        }

    /** Retorna as consultas do médico autenticado, ordenadas cronologicamente por data e hora. */
    suspend fun listarConsultasDoMedico(medicoId: Int): List<Consulta> =
        consultas.filter { it.medicoId == medicoId }
            .sortedWith(compareBy({ it.data }, { it.horario }))

    /** Retorna as consultas do paciente autenticado, ordenadas por data e hora. */
    suspend fun listarConsultasDoPaciente(pacienteId: Int): List<Consulta> =
        consultas.filter { it.pacienteId == pacienteId }
            .sortedWith(compareBy({ it.data }, { it.horario }))

    /** Retorna as consultas de um médico para um dia específico (usado no clique do calendário). */
    suspend fun listarConsultasDoMedicoPorData(medicoId: Int, data: String): List<Consulta> =
        consultas.filter { it.medicoId == medicoId && it.data == data }
            .sortedBy { it.horario }

    /**
     * REGRA DE NEGÓCIO ESSENCIAL:
     * Calcula os horários livres de um médico em uma determinada data.
     * Compara a grade de horários da clínica com os agendamentos já existentes,
     * impedindo duplicidades e conflitos de agenda.
     */
    suspend fun obterHorariosDisponiveis(medicoId: Int, data: String): List<String> {
        val horariosPadrao = listOf(
            "08:00", "09:00", "10:00", "11:00",
            "13:30", "14:30", "15:30", "16:30", "17:30",
        )
        val horariosOcupados = consultas
            .filter { it.medicoId == medicoId && it.data == data }
            .map { it.horario }
            .toSet()
        return horariosPadrao.filter { it !in horariosOcupados }
    }

    /**
     * Agenda uma nova consulta.
     * Valida atomicamente se o médico está livre no dia/horário antes de confirmar.
     */
    suspend fun agendarConsulta(consulta: Consulta): Resultado<Consulta> {
        val medico = medicos.find { it.id == consulta.medicoId }
            ?: return Resultado.Erro("Médico não encontrado.")

        // Validação de concorrência: garante que outro paciente não ocupou o mesmo horário
        val jaOcupado = consultas.any {
            it.medicoId == consulta.medicoId && it.data == consulta.data && it.horario == consulta.horario
        }
        if (jaOcupado) {
            return Resultado.Erro("Este horário já está ocupado para o médico selecionado.")
        }

        val novo = consulta.copy(
            id = (consultas.maxOfOrNull { it.id } ?: 0) + 1,
            medicoNome = if (consulta.medicoNome.isEmpty()) "Dr(a). ${medico.nome}" else consulta.medicoNome,
            medicoEspecialidade = if (consulta.medicoEspecialidade.isEmpty()) medico.especialidade else consulta.medicoEspecialidade,
        )
        consultas.add(novo)
        return Resultado.Sucesso(novo)
    }

    /**
     * Cancela uma consulta agendada pelo paciente.
     * A exclusão no repositório sincroniza em tempo real com a agenda do médico.
     */
    suspend fun cancelarConsulta(consultaId: Int): Resultado<Unit> {
        val removido = consultas.removeAll { it.id == consultaId }
        return if (removido) Resultado.Sucesso(Unit)
        else Resultado.Erro("Consulta não encontrada.")
    }

    /**
     * Remarca uma consulta existente para uma nova data e horário.
     * Valida se o novo horário escolhido está livre na agenda do médico.
     */
    suspend fun remarcarConsulta(
        consultaId: Int,
        novaData: String,
        novoHorario: String,
        novoMotivo: String? = null
    ): Resultado<Consulta> {
        val index = consultas.indexOfFirst { it.id == consultaId }
        if (index == -1) return Resultado.Erro("Consulta não encontrada.")
        val atual = consultas[index]

        // Verifica se o médico tem disponibilidade no novo horário (desconsiderando a própria consulta)
        val jaOcupado = consultas.any {
            it.id != consultaId && it.medicoId == atual.medicoId && it.data == novaData && it.horario == novoHorario
        }
        if (jaOcupado) {
            return Resultado.Erro("Este horário já está ocupado para o médico nesta data.")
        }

        val atualizada = atual.copy(
            data = novaData,
            horario = novoHorario,
            tipoConsulta = novoMotivo ?: atual.tipoConsulta
        )
        consultas[index] = atualizada
        return Resultado.Sucesso(atualizada)
    }

    /**
     * Autenticação unificada: verifica nas listas de médicos e pacientes.
     * Retorna um objeto UsuarioLogado sem expor a senha em memória.
     *
     * Como funciona (futuro: POST /login):
     *  1) Procura na tabela de MÉDICOS alguém com esse e-mail e essa senha.
     *  2) Se não achar, procura na tabela de PACIENTES.
     *  3) Se não achar em nenhuma, devolve erro.
     * Assim, a mesma tela de login serve para os dois tipos de usuário.
     */
    suspend fun login(email: String, senha: String): Resultado<UsuarioLogado> {
        // `find` = procura o primeiro que atende a condição (ou null se não achar).
        // `?.let { ... }` = SE achou, transforma em UsuarioLogado (sem a senha).
        // `?:` = "se não achou, tenta o próximo"; o último `?:` desiste e devolve o erro.
        val usuario = medicos.find { it.email.equals(email, true) && it.senha == senha }
            ?.let { UsuarioLogado(it.id, it.nome, it.email, TipoUsuario.MEDICO) }
            ?: pacientes.find { it.email.equals(email, true) && it.senha == senha }
                ?.let { UsuarioLogado(it.id, it.nome, it.email, TipoUsuario.PACIENTE) }
            ?: return Resultado.Erro("E-mail ou senha incorretos.")

        // Guarda quem entrou, para a tela principal saber o nome e o tipo.
        usuarioLogado = usuario
        return Resultado.Sucesso(usuario)
    }

    /** Encerra a sessão ativa. */
    // "Esquece" quem estava logado. Chamado pelo botão "Sair" da tela principal.
    fun logout() {
        usuarioLogado = null
    }
}
