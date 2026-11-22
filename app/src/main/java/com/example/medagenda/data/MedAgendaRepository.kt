package com.example.medagenda.data

import com.example.medagenda.model.Medico
import com.example.medagenda.model.Paciente
import com.example.medagenda.model.TipoUsuario
import com.example.medagenda.model.UsuarioLogado

/**
 * Ponto único de acesso aos dados do app.
 *
 * POR ENQUANTO os dados ficam em listas na memória (somem ao fechar o app).
 * QUANDO A API FICAR PRONTA, só o corpo destas funções muda para chamar a API
 * (ex.: Retrofit). As telas continuam iguais, pois só conhecem estas funções.
 *
 * Rotas previstas para a API (combinado com o grupo):
 *  - POST /pacientes  -> cadastrarPaciente   (400 campo vazio, 409 e-mail repetido)
 *  - GET  /pacientes  -> listarPacientes
 *  - POST /medicos    -> cadastrarMedico     (400 campo vazio, 409 e-mail ou CRM repetido)
 *  - GET  /medicos    -> listarMedicos
 *  - POST /login      -> login               (401 e-mail ou senha incorretos)
 *
 * As funções são `suspend` para que a troca pela API não exija mudar as telas.
 */
object MedAgendaRepository {

    private val pacientes = mutableListOf(
        Paciente(1, "João Silva", "54999990000", "teste@email.com", "123456")
    )
    private val medicos = mutableListOf(
        Medico(1, "Carlos Santos", "54988880000", "medico@email.com", "12345-RS", "Cardiologia", "123456")
    )

    /** Usuário que está logado no momento (null = ninguém). */
    var usuarioLogado: UsuarioLogado? = null
        private set

    private fun emailJaExiste(email: String): Boolean =
        pacientes.any { it.email.equals(email, ignoreCase = true) } ||
            medicos.any { it.email.equals(email, ignoreCase = true) }

    suspend fun cadastrarPaciente(paciente: Paciente): Resultado<Paciente> {
        if (emailJaExiste(paciente.email)) return Resultado.Erro("Este e-mail já está cadastrado.")
        val novo = paciente.copy(id = (pacientes.maxOfOrNull { it.id } ?: 0) + 1)
        pacientes.add(novo)
        return Resultado.Sucesso(novo)
    }

    suspend fun cadastrarMedico(medico: Medico): Resultado<Medico> {
        if (emailJaExiste(medico.email)) return Resultado.Erro("Este e-mail já está cadastrado.")
        if (medicos.any { it.crm.equals(medico.crm, ignoreCase = true) }) {
            return Resultado.Erro("Este CRM já está cadastrado.")
        }
        val novo = medico.copy(id = (medicos.maxOfOrNull { it.id } ?: 0) + 1)
        medicos.add(novo)
        return Resultado.Sucesso(novo)
    }

    suspend fun listarPacientes(): List<Paciente> = pacientes.toList()

    suspend fun listarMedicos(): List<Medico> = medicos.toList()

    /** Procura o e-mail e a senha nas duas tabelas e informa se é médico ou paciente. */
    suspend fun login(email: String, senha: String): Resultado<UsuarioLogado> {
        val usuario = medicos.find { it.email.equals(email, true) && it.senha == senha }
            ?.let { UsuarioLogado(it.id, it.nome, it.email, TipoUsuario.MEDICO) }
            ?: pacientes.find { it.email.equals(email, true) && it.senha == senha }
                ?.let { UsuarioLogado(it.id, it.nome, it.email, TipoUsuario.PACIENTE) }
            ?: return Resultado.Erro("E-mail ou senha incorretos.")

        usuarioLogado = usuario
        return Resultado.Sucesso(usuario)
    }

    fun logout() {
        usuarioLogado = null
    }
}
