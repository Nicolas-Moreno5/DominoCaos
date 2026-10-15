package com.example.dominocaos.model

enum class EstadoSala {
    ESPERANDO,
    EN_CURSO,
    TERMINADA
}

data class EstadoPartida(
    val codigo: String = "",
    val estadoSala: EstadoSala = EstadoSala.ESPERANDO,
    val jugadores: List<Jugador> = emptyList(),
    val maxJugadores: Int = 4,
    val tablero: Tablero = Tablero(),
    val pozo: List<Ficha> = emptyList(),
    val turnoActual: Int = 0,
    val sentido: Int = 1,
    val rondaActual: Int = 1,
    val maxRondas: Int = 10,
    val eventoActivo: EventoActivo = EventoActivo(),
    val turnosJugadosEnRonda: Int = 0,
    val ganadorUid: String? = null,
    val ultimoEvento: TipoEvento? = null,
    val mensaje: String = ""
)
