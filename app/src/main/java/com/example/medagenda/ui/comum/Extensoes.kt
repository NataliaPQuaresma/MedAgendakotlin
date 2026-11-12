package com.example.medagenda.ui.comum

import android.util.Patterns
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout

/** Evita que a tela fique por baixo da barra de status e de navegação. */
fun View.aplicarMargensDoSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
        insets
    }
}

/** Texto digitado no campo, sem espaços nas pontas. */
val TextInputLayout.texto: String
    get() = editText?.text?.toString()?.trim().orEmpty()

/** Mostra (ou limpa, se null) a mensagem de erro do campo. Retorna true se o campo está válido. */
fun TextInputLayout.validar(mensagemErro: String?): Boolean {
    error = mensagemErro
    return mensagemErro == null
}

fun String.emailValido(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

/** Telefone com DDD: 10 ou 11 dígitos (ignora parênteses, espaços e traços). */
fun String.telefoneValido(): Boolean = filter { it.isDigit() }.length in 10..11
