package com.example.medagenda.model

/**
 * TIPO DE USUÁRIO
 *
 * `enum` = uma lista fechada de opções. Aqui só existem duas: PACIENTE ou MEDICO.
 * Por que usar enum e não texto ("medico"): evita erro de digitação, porque o
 * Android Studio só aceita uma das duas opções.
 *
 * Liga com: PrincipalActivity, que mostra "Você entrou como médico/paciente".
 */
enum class TipoUsuario { PACIENTE, MEDICO }

/**
 * USUÁRIO LOGADO
 *
 * O que é: o resumo de quem fez login (médico ou paciente).
 *
 * Por que existe: depois do login, o app precisa saber o nome e o tipo da
 * pessoa, mas não precisa (nem deve) ficar carregando a senha por aí.
 * Por isso este modelo NÃO tem o campo senha.
 *
 * Liga com:
 *  - MedAgendaRepository.login() -> cria este objeto quando o login dá certo.
 *  - PrincipalActivity           -> lê este objeto para mostrar "Olá, {nome}!".
 */
data class UsuarioLogado(
    val id: Int,
    val nome: String,
    val email: String,
    val tipo: TipoUsuario       // diz se é médico ou paciente
)
