package com.example.dominocaos.logic

import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.Jugador
import com.example.dominocaos.model.Poder
import com.example.dominocaos.model.TipoPoder
import com.example.dominocaos.model.crearPoderesIniciales
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GestorPoderesTest {

    @Test
    fun puedeUsar_conUsosDisponibles_retornaTrue() {
        val jugador = Jugador(poderes = crearPoderesIniciales())
        assertTrue(GestorPoderes.puedeUsar(jugador, TipoPoder.COMODIN))
    }

    @Test
    fun puedeUsar_sinUsosDisponibles_retornaFalse() {
        val poderesAgotados = listOf(Poder(tipo = TipoPoder.COMODIN, usosRestantes = 0))
        val jugador = Jugador(poderes = poderesAgotados)
        assertFalse(GestorPoderes.puedeUsar(jugador, TipoPoder.COMODIN))
    }

    @Test
    fun consumir_descuentaUsoYIncrementaPoderesUsados() {
        val jugadorInicial = Jugador(poderes = crearPoderesIniciales(), poderesUsados = 0)

        val jugadorActualizado = GestorPoderes.consumir(jugadorInicial, TipoPoder.SALTO)

        val poderSalto = jugadorActualizado.poderes.first { it.tipo == TipoPoder.SALTO }
        assertEquals(1, poderSalto.usosRestantes)
        assertEquals(1, jugadorActualizado.poderesUsados)
    }

    @Test
    fun aplicarIntercambio_intercambiaFichasEntreManos() {
        val fichaJugador = Ficha(6, 6)
        val fichaOponente1 = Ficha(1, 1)
        val fichaOponente2 = Ficha(2, 2)

        val j1 = Jugador(uid = "j1", nombre = "Jugador 1", mano = listOf(fichaJugador, Ficha(0, 1)))
        val j2 = Jugador(uid = "j2", nombre = "Jugador 2", mano = listOf(fichaOponente1, fichaOponente2))

        val estadoInicial = EstadoPartida(jugadores = listOf(j1, j2), turnoActual = 0)

        val randomConSemilla = Random(42)
        val estadoResultado = GestorPoderes.aplicarIntercambio(
            estado = estadoInicial,
            indiceJugador = 0,
            fichaPropia = fichaJugador,
            random = randomConSemilla
        )

        val j1Nuevo = estadoResultado.jugadores[0]
        val j2Nuevo = estadoResultado.jugadores[1]

        assertFalse(j1Nuevo.mano.contains(fichaJugador))
        assertTrue(j2Nuevo.mano.contains(fichaJugador))

        assertEquals(2, j1Nuevo.mano.size)
        assertEquals(2, j2Nuevo.mano.size)
    }
}
