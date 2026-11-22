package com.example.medagenda.model

enum class TipoUsuario { PACIENTE, MEDICO }

/** Quem fez login. Não guarda a senha. */
data class UsuarioLogado(
    val id: Int,
    val nome: String,
    val email: String,
    val tipo: TipoUsuario
)
