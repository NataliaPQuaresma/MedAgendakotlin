package com.example.medagenda.model

/**
 * Modelo de dados que representa uma consulta no MedAgenda.
 *
 * PARA A APRESENTAÇÃO:
 * - Esta classe funciona como o contrato de dados (DTO) entre o app e a API.
 * - Armazena as relações de chave estrangeira (medicoId, pacienteId), bem como os
 *   dados desnormalizados (nomes e especialidade) para exibição rápida em tela.
 * - As datas são mantidas em formato ISO ("yyyy-MM-dd") para ordenação e filtragem precisa,
 *   sendo convertidas para o padrão brasileiro ("dd/MM/yyyy") na interface com o usuário.
 *
 * Futuramente na API REST:
 * - Mapeia diretamente a entidade/tabela 'consultas' do banco de dados relacional.
 */
data class Consulta(
    val id: Int = 0,
    val medicoId: Int,
    val medicoNome: String = "",
    val medicoEspecialidade: String = "",
    val pacienteId: Int,
    val pacienteNome: String,
    val pacienteTelefone: String = "",
    val data: String, // Formato "yyyy-MM-dd" para ordenação e busca na API
    val horario: String, // Formato "HH:mm" (ex: "09:00", "14:30")
    val tipoConsulta: String = "Consulta de rotina",
)
