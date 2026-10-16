package com.example.dominocaos.logic

import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.Jugador
import org.junit.Assert.assertEquals
import org.junit.Test

class GestorTurnosTest {

    private fun crearJugadores(cantidad: Int): List<Jugador> {
        return (0 until cantidad).map { index ->
            Jugador(uid = "user_$index", nombre = "Jugador $index")
        }
    }

    @Test
    fun avanzarTurno_normalCon2Jugadores_avanzaIntercalado() {
        val jugadores = crearJugadores(2)
        val estadoInicial = EstadoPartida(jugadores = jugadores, turnoActual = 0, sentido = 1)

        val estado1 = GestorTurnos.avanzarTurno(estadoInicial)
        assertEquals(1, estado1.turnoActual)

        val estado2 = GestorTurnos.avanzarTurno(estado1)
        assertEquals(0, estado2.turnoActual)
    }

    @Test
    fun avanzarTurno_normalCon3Jugadores_avanzaSecuencialmente() {
        val jugadores = crearJugadores(3)
        val estadoInicial = EstadoPartida(jugadores = jugadores, turnoActual = 0, sentido = 1)

        val estado1 = GestorTurnos.avanzarTurno(estadoInicial)
        assertEquals(1, estado1.turnoActual)

        val estado2 = GestorTurnos.avanzarTurno(estado1)
        assertEquals(2, estado2.turnoActual)

        val estado3 = GestorTurnos.avanzarTurno(estado2)
        assertEquals(0, estado3.turnoActual)
    }

    @Test
    fun avanzarTurno_sentidoInvertido_avanzaHaciaAtras() {
        val jugadores = crearJugadores(3)
        val estadoInvertido = EstadoPartida(jugadores = jugadores, turnoActual = 0, sentido = -1)

        val estado1 = GestorTurnos.avanzarTurno(estadoInvertido)
        assertEquals(2, estado1.turnoActual)

        val estado2 = GestorTurnos.avanzarTurno(estado1)
        assertEquals(1, estado2.turnoActual)
    }

    @Test
    fun avanzarTurno_conSalto_omiteAlSiguienteJugador() {
        val jugadores = crearJugadores(3)
        val estadoInicial = EstadoPartida(jugadores = jugadores, turnoActual = 0, sentido = 1)

        val estadoSalto = GestorTurnos.avanzarTurno(estadoInicial, saltar = true)
        assertEquals(2, estadoSalto.turnoActual)
    }

    @Test
    fun avanzarTurno_jugadorInactivo_loOmiteEnTurnos() {
        val jugadores = listOf(
            Jugador(uid = "1", nombre = "J1", activo = true),
            Jugador(uid = "2", nombre = "J2", activo = false),
            Jugador(uid = "3", nombre = "J3", activo = true)
        )
        val estadoInicial = EstadoPartida(jugadores = jugadores, turnoActual = 0, sentido = 1)

        val estado1 = GestorTurnos.avanzarTurno(estadoInicial)
        assertEquals(2, estado1.turnoActual)
    }

    @Test
    fun avanzarTurno_alCompletarCiclo_incrementaRonda() {
        val jugadores = crearJugadores(2)
        val estadoInicial = EstadoPartida(
            jugadores = jugadores,
            turnoActual = 0,
            rondaActual = 1,
            turnosJugadosEnRonda = 0
        )

        val estado1 = GestorTurnos.avanzarTurno(estadoInicial)
        assertEquals(1, estado1.rondaActual)
        assertEquals(1, estado1.turnosJugadosEnRonda)

        val estado2 = GestorTurnos.avanzarTurno(estado1)
        assertEquals(2, estado2.rondaActual)
        assertEquals(0, estado2.turnosJugadosEnRonda)
    }

    @Test
    fun invertirSentido_cambiaSentidoCorrectamente() {
        val estado = EstadoPartida(sentido = 1)
        val invertido = GestorTurnos.invertirSentido(estado)
        assertEquals(-1, invertido.sentido)

        val normalizado = GestorTurnos.invertirSentido(invertido)
        assertEquals(1, normalizado.sentido)
    }
}
