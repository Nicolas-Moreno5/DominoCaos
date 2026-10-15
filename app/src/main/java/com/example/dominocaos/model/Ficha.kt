package com.example.dominocaos.model

import com.google.firebase.firestore.Exclude

data class Ficha(
    val a: Int = 0,
    val b: Int = 0
) {
    @get:Exclude
    val puntos: Int
        get() = a + b

    @get:Exclude
    val esDoble: Boolean
        get() = a == b

    fun voltear(): Ficha = Ficha(b, a)
}
