package com.example.medagenda.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ROTAS DA API DE CONSULTAS (o "cardápio" do que o app pode pedir à API)
 *
 * Cada função abaixo é uma rota do arquivo api/app.py (branch api-medagenda).
 * O Retrofit lê as anotações (@GET, @POST...) e faz a chamada HTTP sozinho:
 * a gente só escreve "o quê", e ele faz o "como".
 *
 * As funções são `suspend` porque a resposta da internet demora:
 * o app espera sem travar a tela.
 *
 * Quem usa: MedAgendaRepository (as telas nunca chamam a API direto).
 */
interface ConsultasApi {

    /** GET /consultas -> todas as consultas (usado para achar as do paciente). */
    @GET("consultas")
    suspend fun listarTodas(): List<ConsultaApi>

    /**
     * GET /medicos/{id}/consultas?data=AAAA-MM-DD -> agenda do médico.
     * Se `data` for null, o Retrofit nem manda o filtro, e vêm todas as datas.
     */
    @GET("medicos/{id}/consultas")
    suspend fun listarDoMedico(
        @Path("id") medicoId: Int,            // troca o {id} do caminho
        @Query("data") data: String? = null   // vira ?data=... no fim do endereço
    ): List<ConsultaApi>

    /** POST /consultas -> cria uma consulta e devolve com o id que o banco gerou. */
    @POST("consultas")
    suspend fun criar(@Body nova: NovaConsultaApi): ConsultaApi

    /** PUT /consultas/{id} -> remarca (nova data e hora). 409 se o horário estiver ocupado. */
    @PUT("consultas/{id}")
    suspend fun remarcar(@Path("id") consultaId: Int, @Body dados: RemarcarConsultaApi): ConsultaApi

    /** DELETE /consultas/{id} -> cancela. 404 se a consulta não existir. */
    @DELETE("consultas/{id}")
    suspend fun cancelar(@Path("id") consultaId: Int): MensagemApi
}

/*
 * FORMATO DOS DADOS QUE VÃO E VOLTAM DA API (JSON)
 *
 * A API usa nomes em "snake_case" (medico_id) e o Kotlin usa "camelCase" (medicoId).
 * O @SerializedName diz ao Gson qual nome do JSON corresponde a cada campo.
 */

/** Uma consulta como a API devolve. Ex.: {"id":1,"medico_id":1,"paciente":"Ana","data":"2026-11-25","hora":"09:00","motivo":"Rotina"} */
data class ConsultaApi(
    val id: Int,
    @SerializedName("medico_id") val medicoId: Int,
    val paciente: String,        // a API guarda o NOME do paciente
    val data: String,            // "AAAA-MM-DD"
    val hora: String,            // "HH:mm"
    val motivo: String?          // pode vir vazio (null)
)

/** O que o app manda para criar uma consulta (POST). */
data class NovaConsultaApi(
    @SerializedName("medico_id") val medicoId: Int,
    val paciente: String,
    val data: String,
    val hora: String,
    val motivo: String?
)

/** O que o app manda para remarcar (PUT). Se `motivo` for null, a API mantém o antigo. */
data class RemarcarConsultaApi(
    val data: String,
    val hora: String,
    val motivo: String? = null
)

/** Resposta simples da API, ex.: {"mensagem": "Consulta cancelada com sucesso."} */
data class MensagemApi(val mensagem: String?)
