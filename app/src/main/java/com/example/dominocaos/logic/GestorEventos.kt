package com.example.dominocaos.logic

import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.EventoActivo
import com.example.dominocaos.model.TipoEvento
import kotlin.random.Random

object GestorEventos {

    fun debeOcurrir(random: Random = Random.Default): Boolean {
        return random.nextFloat() < 0.25f
    }

    fun elegirEvento(random: Random = Random.Default): TipoEvento {
        return TipoEvento.entries.random(random)
    }

    fun aplicar(
        estado: EstadoPartida,
        tipo: TipoEvento
    ): EstadoPartida {
        var pozoActual = estado.pozo.toMutableList()
        val jugadoresActuales = estado.jugadores.toMutableList()
        var nuevoEventoActivo = estado.eventoActivo

        when (tipo) {
            TipoEvento.TORMENTA_DE_FICHAS -> {
                for (i in jugadoresActuales.indices) {
                    if (jugadoresActuales[i].activo && pozoActual.isNotEmpty()) {
                        val fichaRobada = pozoActual.removeAt(0)
                        val nuevaMano = jugadoresActuales[i].mano + fichaRobada
                        jugadoresActuales[i] = jugadoresActuales[i].copy(mano = nuevaMano)
                    }
                }
            }
            TipoEvento.TURNO_EXTRA -> {
                nuevoEventoActivo = EventoActivo(tipo = TipoEvento.TURNO_EXTRA, turnosRestantes = 1)
            }
            TipoEvento.PUNTOS_DOBLES -> {
                nuevoEventoActivo = EventoActivo(tipo = TipoEvento.PUNTOS_DOBLES, turnosRestantes = 2)
            }
            TipoEvento.POZO_REDUCIDO -> {
                if (pozoActual.isNotEmpty()) {
                    val aRemover = minOf(2, pozoActual.size)
                    pozoActual = pozoActual.drop(aRemover).toMutableList()
                }
            }
        }

        return estado.copy(
            pozo = pozoActual,
            jugadores = jugadoresActuales,
            eventoActivo = nuevoEventoActivo,
            ultimoEvento = tipo
        )
    }

    fun expirar(estado: EstadoPartida): EstadoPartida {
        val evento = estado.eventoActivo
        if (evento.tipo == null || evento.turnosRestantes <= 0) {
            return estado.copy(eventoActivo = EventoActivo())
        }
        val restantes = evento.turnosRestantes - 1
        val nuevoEvento = if (restantes <= 0) EventoActivo() else evento.copy(turnosRestantes = restantes)
        return estado.copy(eventoActivo = nuevoEvento)
    }
}
