package com.example.medagenda.model

/**
 * MODELO DO MÉDICO
 *
 * O que é: o "molde" de um médico. Equivale a uma linha da "tabela de médicos".
 * Tem os mesmos campos do paciente e mais dois: CRM e especialidade.
 *
 * Por que separado do Paciente: o pedido do projeto é que médicos e pacientes
 * fiquem em tabelas separadas, porque têm informações diferentes.
 *
 * Liga com:
 *  - CadastroMedicoActivity -> cria um Medico com o que foi digitado.
 *  - MedAgendaRepository    -> guarda o Medico na lista `medicos`.
 *  - Futuramente, a API     -> vai virar o JSON da rota /medicos.
 */
data class Medico(
    val id: Int = 0,            // definido pelo repositório na hora de salvar
    val nome: String,
    val telefone: String,       // salvo só com números
    val email: String,          // usado no login
    val crm: String,            // registro do médico no Conselho de Medicina (não pode repetir)
    val especialidade: String,  // ex.: Cardiologia
    val senha: String           // usada no login
)
