package com.example.dominocaos.logic

import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.EstadoSala

object DetectorFinPartida {

    fun haTerminado(estado: EstadoPartida): Boolean {
        val jugadoresActivos = estado.jugadores.filter { it.activo }
        if (jugadoresActivos.isEmpty()) return true

        val algunSinFichas = jugadoresActivos.any { it.mano.isEmpty() }
        if (algunSinFichas) return true

        if (estado.rondaActual > estado.maxRondas) return true

        if (estado.pozo.isEmpty()) {
            val algunMovimientoPosible = jugadoresActivos.any { jugador ->
                ValidadorMovimientos.tieneMovimientoValido(jugador.mano, estado.tablero)
            }
            if (!algunMovimientoPosible) return true
        }

        return false
    }

    fun determinarGanador(estado: EstadoPartida): String? {
        val candidatos = estado.jugadores.filter { it.activo }
        if (candidatos.isEmpty()) return null

        val maxPuntaje = candidatos.maxOf { it.puntaje }
        val conMaxPuntaje = candidatos.filter { it.puntaje == maxPuntaje }

        if (conMaxPuntaje.size == 1) {
            return conMaxPuntaje.first().uid
        }

        val minPoderes = conMaxPuntaje.minOf { it.poderesUsados }
        val conMinPoderes = conMaxPuntaje.filter { it.poderesUsados == minPoderes }

        if (conMinPoderes.size == 1) {
            return conMinPoderes.first().uid
        }

        return null
    }

    fun finalizar(estado: EstadoPartida): EstadoPartida {
        val ganador = determinarGanador(estado)
        return estado.copy(
            estadoSala = EstadoSala.TERMINADA,
            ganadorUid = ganador
        )
    }
}
