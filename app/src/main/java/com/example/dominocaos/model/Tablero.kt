package com.example.dominocaos.model

import com.google.firebase.firestore.Exclude

enum class Lado {
    IZQUIERDO,
    DERECHO
}

data class FichaColocada(
    val ficha: Ficha = Ficha()
)

data class Tablero(
    val fichas: List<FichaColocada> = emptyList()
) {
    @get:Exclude
    val extremoIzquierdo: Int?
        get() = fichas.firstOrNull()?.ficha?.a

    @get:Exclude
    val extremoDerecho: Int?
        get() = fichas.lastOrNull()?.ficha?.b

    @get:Exclude
    val esVacio: Boolean
        get() = fichas.isEmpty()
}
