package com.example.dominocaos.model

data class Jugador(
    val uid: String = "",
    val nombre: String = "",
    val mano: List<Ficha> = emptyList(),
    val poderes: List<Poder> = emptyList(),
    val puntaje: Int = 0,
    val poderesUsados: Int = 0,
    val activo: Boolean = true
)
