package com.example.medagenda.data

/**
 * RESULTADO DE UMA OPERAÇÃO
 *
 * O que é: a "resposta" que o repositório devolve para as telas.
 * Só pode ser uma de duas coisas:
 *  - Sucesso -> deu certo, e traz o dado (ex.: o paciente que foi salvo).
 *  - Erro    -> deu errado, e traz a mensagem (ex.: "Este e-mail já está cadastrado.").
 *
 * Por que existe: assim a tela sempre sabe se deu certo ou não, e o que mostrar.
 * É parecido com uma resposta de API: código 200 (sucesso) ou 400/401/409 (erro).
 *
 * `sealed class` = classe "fechada": só existem esses dois tipos de resultado.
 * Por isso, nas telas, o `when` obriga a tratar os dois casos (Sucesso e Erro).
 *
 * `<T>` = "qualquer tipo". Permite usar a mesma classe para Paciente, Medico, etc.
 *
 * Liga com: MedAgendaRepository (que cria) e as telas de Login e Cadastro (que leem).
 */
sealed class Resultado<out T> {
    data class Sucesso<T>(val dado: T) : Resultado<T>()
    data class Erro(val mensagem: String) : Resultado<Nothing>()
}
