package com.example.dominocaos.logic

import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.Jugador
import com.example.dominocaos.model.TipoPoder
import kotlin.random.Random

object GestorPoderes {

    fun puedeUsar(jugador: Jugador, tipo: TipoPoder): Boolean {
        val poder = jugador.poderes.firstOrNull { it.tipo == tipo }
        return poder != null && poder.usosRestantes > 0
    }

    fun consumir(jugador: Jugador, tipo: TipoPoder): Jugador {
        val nuevosPoderes = jugador.poderes.map { poder ->
            if (poder.tipo == tipo && poder.usosRestantes > 0) {
                poder.copy(usosRestantes = poder.usosRestantes - 1)
            } else {
                poder
            }
        }
        return jugador.copy(
            poderes = nuevosPoderes,
            poderesUsados = jugador.poderesUsados + 1
        )
    }

    fun aplicarIntercambio(
        estado: EstadoPartida,
        indiceJugador: Int,
        fichaPropia: Ficha,
        random: Random = Random.Default
    ): EstadoPartida {
        val jugadores = estado.jugadores
        if (indiceJugador !in jugadores.indices) return estado

        val jugador = jugadores[indiceJugador]
        if (!jugador.mano.contains(fichaPropia)) return estado

        val estadoConTurnoActual = estado.copy(turnoActual = indiceJugador)
        val indiceOponente = GestorTurnos.siguienteIndice(estadoConTurnoActual)
        if (indiceOponente == indiceJugador || indiceOponente !in jugadores.indices) return estado

        val oponente = jugadores[indiceOponente]
        if (oponente.mano.isEmpty()) return estado

        val fichaOponente = oponente.mano.random(random)

        val nuevaManoJugador = jugador.mano - fichaPropia + fichaOponente
        val nuevaManoOponente = oponente.mano - fichaOponente + fichaPropia

        val nuevosJugadores = jugadores.toMutableList()
        nuevosJugadores[indiceJugador] = jugador.copy(mano = nuevaManoJugador)
        nuevosJugadores[indiceOponente] = oponente.copy(mano = nuevaManoOponente)

        return estado.copy(jugadores = nuevosJugadores)
    }
}
