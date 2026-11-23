package com.example.medagenda.data.api

import android.os.Build
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * CONEXÃO COM A API
 *
 * Monta o Retrofit uma vez só (`object` = existe um só no app) e entrega
 * as rotas prontas para o repositório usar: `ApiClient.consultas.listarTodas()`.
 */
object ApiClient {

    /**
     * Endereço da API. Muda conforme onde o app está rodando:
     * - EMULADOR: 10.0.2.2 é como o emulador enxerga o computador onde a API está
     *   (dentro do emulador, "localhost" seria o próprio emulador, não o computador).
     * - CELULAR no cabo USB: 127.0.0.1, depois de rodar no computador
     *   `adb reverse tcp:5000 tcp:5000`. Esse comando faz o cabo levar o
     *   127.0.0.1:5000 do celular até a porta 5000 do computador.
     *   Precisa rodar de novo toda vez que reconectar o cabo.
     * - 5000 é a porta padrão do Flask (`python app.py`).
     */
    val URL_BASE: String =
        if (rodandoNoEmulador()) "http://10.0.2.2:5000/" else "http://127.0.0.1:5000/"

    /**
     * Descobre se o app está num emulador olhando as informações do aparelho (Build).
     * Os emuladores do Android Studio usam o hardware "ranchu" (ou "goldfish", nos
     * antigos) e têm "sdk" no nome do produto. Celulares de verdade não têm isso.
     */
    private fun rodandoNoEmulador(): Boolean =
        Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish" ||
            Build.PRODUCT.contains("sdk") || Build.FINGERPRINT.startsWith("generic")

    // Tempo máximo de espera. Curto de propósito: se a API estiver desligada,
    // o app desiste rápido e passa a usar os dados da memória.
    private val clienteHttp = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // `by lazy` = só monta na primeira vez que alguém usar.
    val consultas: ConsultasApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create())   // JSON <-> Kotlin
            .build()
            .create(ConsultasApi::class.java)                     // gera o código das rotas
    }
}
