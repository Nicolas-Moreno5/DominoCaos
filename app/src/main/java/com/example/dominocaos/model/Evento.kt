package com.example.dominocaos.model

enum class TipoEvento(
    val nombre: String,
    val descripcion: String
) {
    TORMENTA_DE_FICHAS(
        "Tormenta de Fichas",
        "Todos los jugadores roban 1 ficha del pozo."
    ),
    TURNO_EXTRA(
        "Turno Extra",
        "Obtienes un turno adicional inmediatamente."
    ),
    PUNTOS_DOBLES(
        "Puntos Dobles",
        "Los puntos obtenidos se duplican durante 2 turnos."
    ),
    POZO_REDUCIDO(
        "Pozo Reducido",
        "Se descartan 2 fichas del pozo."
    )
}

data class EventoActivo(
    val tipo: TipoEvento? = null,
    val turnosRestantes: Int = 0
)
