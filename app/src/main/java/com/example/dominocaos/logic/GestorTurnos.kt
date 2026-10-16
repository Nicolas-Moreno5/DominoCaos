package com.example.dominocaos.logic

import com.example.dominocaos.model.EventoActivo
import com.example.dominocaos.model.EstadoPartida

object GestorTurnos {

    fun siguienteIndice(estado: EstadoPartida, saltar: Boolean = false): Int {
        val jugadores = estado.jugadores
        if (jugadores.isEmpty()) return 0
        val total = jugadores.size
        val paso = if (estado.sentido >= 0) 1 else -1
        val saltosAEncontrar = if (saltar) 2 else 1

        var actual = estado.turnoActual
        var encontrados = 0

        repeat(total) {
            actual = ((actual + paso) % total + total) % total
            if (jugadores[actual].activo) {
                encontrados++
                if (encontrados == saltosAEncontrar) {
                    return actual
                }
            }
        }
        return actual
    }

    fun avanzarTurno(estado: EstadoPartida, saltar: Boolean = false): EstadoPartida {
        val nuevoIndice = siguienteIndice(estado, saltar)
        val jugadoresActivos = estado.jugadores.count { it.activo }
        val nuevosTurnosEnRonda = estado.turnosJugadosEnRonda + 1

        val (nuevaRonda, turnosRestantesRonda) = if (jugadoresActivos > 0 && nuevosTurnosEnRonda >= jugadoresActivos) {
            Pair(estado.rondaActual + 1, 0)
        } else {
            Pair(estado.rondaActual, nuevosTurnosEnRonda)
        }

        val eventoActualizado = if (estado.eventoActivo.turnosRestantes > 0) {
            val restantes = estado.eventoActivo.turnosRestantes - 1
            if (restantes <= 0) EventoActivo() else estado.eventoActivo.copy(turnosRestantes = restantes)
        } else {
            estado.eventoActivo
        }

        return estado.copy(
            turnoActual = nuevoIndice,
            rondaActual = nuevaRonda,
            turnosJugadosEnRonda = turnosRestantesRonda,
            eventoActivo = eventoActualizado
        )
    }

    fun invertirSentido(estado: EstadoPartida): EstadoPartida {
        return estado.copy(sentido = estado.sentido * -1)
    }
}
