package com.example.medagenda.model

/** Médico cadastrado. Futuramente: tabela/rota `medicos` da API. */
data class Medico(
    val id: Int = 0,
    val nome: String,
    val telefone: String,
    val email: String,
    val crm: String,
    val especialidade: String,
    val senha: String
)
