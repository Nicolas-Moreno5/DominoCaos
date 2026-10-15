package com.example.dominocaos.model

enum class TipoPoder {
    COMODIN,
    SALTO,
    INTERCAMBIO,
    INVERSION
}

data class Poder(
    val tipo: TipoPoder = TipoPoder.COMODIN,
    val usosRestantes: Int = 2
)

fun crearPoderesIniciales(): List<Poder> = TipoPoder.entries.map { Poder(tipo = it, usosRestantes = 2) }
