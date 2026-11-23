package com.example.medagenda.ui.comum

import android.util.Patterns
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout

/*
 * FUNÇÕES DE APOIO, usadas por várias telas.
 *
 * Por que existem: em vez de repetir o mesmo código em cada tela, escrevemos uma
 * vez aqui e todas usam. Se precisar mudar (ex.: a regra do telefone), muda num lugar só.
 *
 * São "extensões" do Kotlin: funções que "grudam" em um tipo que já existe.
 * Ex.: `fun String.emailValido()` permite escrever  "abc@x.com".emailValido()
 * como se a função fosse da própria String.
 */

/**
 * Ajusta a tela para não ficar escondida atrás da barra de status (topo), da
 * barra de navegação (rodapé) nem do teclado.
 *
 * Por que precisa: as telas usam `enableEdgeToEdge()`, que faz o app ocupar a tela
 * inteira, inclusive atrás dessas barras. Esta função coloca um espaçamento
 * (padding) do tamanho exato das barras e do teclado.
 *
 * Usada em: todas as Activities, logo depois do setContentView.
 */
fun View.aplicarMargensDoSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        // Mede o tamanho das barras do sistema + o teclado (ime), se estiver aberto.
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
}

/**
 * Pega o texto digitado em um campo, já sem espaços no começo e no fim (`trim`).
 * Se o campo estiver vazio, devolve "" em vez de null (`orEmpty`), para não dar erro.
 *
 * Uso: `tilEmail.texto` em vez de `tilEmail.editText?.text?.toString()?.trim()`.
 */
val TextInputLayout.texto: String
    get() = editText?.text?.toString()?.trim().orEmpty()

/**
 * Mostra a mensagem de erro vermelha embaixo do campo (ou apaga, se for null).
 * Devolve `true` se o campo está OK e `false` se tem erro.
 *
 * Usada nas validações do Login e dos Cadastros.
 */
fun TextInputLayout.validar(mensagemErro: String?): Boolean {
    error = mensagemErro
    return mensagemErro == null
}

/**
 * Confere se o texto tem formato de e-mail (algo@algo.algo).
 * Usa um verificador que já vem pronto no Android (`Patterns.EMAIL_ADDRESS`).
 */
fun String.emailValido(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

/**
 * Confere se o telefone tem DDD: 10 dígitos (fixo) ou 11 dígitos (celular).
 * Primeiro tira tudo que não é número (parênteses, espaços, traços) e depois conta.
 * Ex.: "(54) 99999-0000" vira "54999990000", que tem 11 dígitos -> válido.
 */
fun String.telefoneValido(): Boolean = filter { it.isDigit() }.length in 10..11
