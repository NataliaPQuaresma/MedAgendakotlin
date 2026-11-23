package com.example.medagenda.model

/**
 * MODELO DO PACIENTE
 *
 * O que é: o "molde" de um paciente. Diz quais informações todo paciente tem.
 * Equivale a uma linha da "tabela de pacientes".
 *
 * Por que `data class`: o Kotlin cria sozinho funções úteis, como o `copy()`
 * (usado no repositório para gerar uma cópia com o id preenchido).
 *
 * Liga com:
 *  - CadastroPacienteActivity -> cria um Paciente com o que foi digitado.
 *  - MedAgendaRepository      -> guarda o Paciente na lista `pacientes`.
 *  - Futuramente, a API       -> vai virar o JSON da rota /pacientes.
 */
data class Paciente(
    // Número que identifica o paciente. Começa em 0 porque quem define o
    // número de verdade é o repositório (e depois a API), na hora de salvar.
    val id: Int = 0,
    val nome: String,
    val telefone: String,   // salvo só com números, ex.: 54999990000
    val email: String,      // usado no login
    val senha: String       // usada no login
)
