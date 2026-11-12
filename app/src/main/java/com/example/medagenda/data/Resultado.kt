package com.example.medagenda.data

/** Resposta de uma operação de dados: deu certo (com o valor) ou deu erro (com a mensagem). */
sealed class Resultado<out T> {
    data class Sucesso<T>(val dado: T) : Resultado<T>()
    data class Erro(val mensagem: String) : Resultado<Nothing>()
}
