package com.example.medagenda.model

/** Paciente cadastrado. Futuramente: tabela/rota `pacientes` da API. */
data class Paciente(
    val id: Int = 0,
    val nome: String,
    val telefone: String,
    val email: String,
    val senha: String
)
