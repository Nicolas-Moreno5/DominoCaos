package com.example.dominocaos.logic

import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.EstadoSala
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.FichaColocada
import com.example.dominocaos.model.Jugador
import com.example.dominocaos.model.Lado
import com.example.dominocaos.model.Tablero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MotorJuegoTest {

    private val fixedRandom = Random(42)

    @Test
    fun iniciarPartida_reparte7FichasYSeteaTurno0() {
        val j1 = Jugador(uid = "1", nombre = "J1")
        val j2 = Jugador(uid = "2", nombre = "J2")
        val sala = MotorJuego.crearSala("SALA1", j1).let {
            MotorJuego.unirse(it, j2).getOrThrow()
        }

        val resultado = MotorJuego.iniciarPartida(sala, fixedRandom)
        assertTrue(resultado.isSuccess)

        val estado = resultado.getOrThrow()
        assertEquals(EstadoSala.EN_CURSO, estado.estadoSala)
        assertEquals(0, estado.turnoActual)
        assertEquals(7, estado.jugadores[0].mano.size)
        assertEquals(7, estado.jugadores[1].mano.size)
    }

    @Test
    fun jugarFicha_jugadorSinTurno_devuelveError() {
        val j1 = Jugador(uid = "1", nombre = "J1", mano = listOf(Ficha(6, 6)))
        val j2 = Jugador(uid = "2", nombre = "J2", mano = listOf(Ficha(5, 5)))
        val estado = EstadoPartida(
            estadoSala = EstadoSala.EN_CURSO,
            jugadores = listOf(j1, j2),
            turnoActual = 0
        )

        val resultado = MotorJuego.jugarFicha(estado, "2", Ficha(5, 5), Lado.IZQUIERDO, random = fixedRandom)
        assertTrue(resultado.isFailure)
        assertEquals("No es tu turno", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun jugarFicha_valida_sumaPuntajeYCambiaTurno() {
        val fichaJugador = Ficha(6, 6)
        val j1 = Jugador(uid = "1", nombre = "J1", mano = listOf(fichaJugador))
        val j2 = Jugador(uid = "2", nombre = "J2", mano = listOf(Ficha(5, 5)))
        val estado = EstadoPartida(
            estadoSala = EstadoSala.EN_CURSO,
            jugadores = listOf(j1, j2),
            turnoActual = 0
        )

        val noEventRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
            override fun nextFloat(): Float = 0.99f
        }

        val resultado = MotorJuego.jugarFicha(estado, "1", fichaJugador, Lado.IZQUIERDO, random = noEventRandom)
        assertTrue(resultado.isSuccess)

        val nuevoEstado = resultado.getOrThrow()
        assertEquals(1, nuevoEstado.turnoActual)
        assertEquals(12, nuevoEstado.jugadores[0].puntaje)
        assertEquals(0, nuevoEstado.jugadores[0].mano.size)
    }

    @Test
    fun tomarDelPozo_cuandoSiHayMovimiento_falla() {
        val j1 = Jugador(uid = "1", nombre = "J1", mano = listOf(Ficha(6, 6)))
        val j2 = Jugador(uid = "2", nombre = "J2", mano = listOf(Ficha(5, 5)))
        val tablero = Tablero(listOf(FichaColocada(Ficha(6, 0))))
        val estado = EstadoPartida(
            estadoSala = EstadoSala.EN_CURSO,
            jugadores = listOf(j1, j2),
            tablero = tablero,
            pozo = listOf(Ficha(1, 1)),
            turnoActual = 0
        )

        val resultado = MotorJuego.tomarDelPozo(estado, "1")
        assertTrue(resultado.isFailure)
        assertEquals("Tienes movimientos válidos, no puedes robar", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun abandonar_con2Jugadores_terminaLaPartida() {
        val j1 = Jugador(uid = "1", nombre = "J1")
        val j2 = Jugador(uid = "2", nombre = "J2")
        val estado = EstadoPartida(
            estadoSala = EstadoSala.EN_CURSO,
            jugadores = listOf(j1, j2),
            turnoActual = 0
        )

        val resultado = MotorJuego.abandonar(estado, "1")
        assertTrue(resultado.isSuccess)

        val finalizado = resultado.getOrThrow()
        assertEquals(EstadoSala.TERMINADA, finalizado.estadoSala)
        assertEquals("2", finalizado.ganadorUid)
    }

    @Test
    fun finPorRondasMaximas_ganaMayorPuntaje() {
        val j1 = Jugador(uid = "1", nombre = "J1", puntaje = 50, mano = listOf(Ficha(1, 1)))
        val j2 = Jugador(uid = "2", nombre = "J2", puntaje = 30, mano = listOf(Ficha(2, 2)))
        val estado = EstadoPartida(
            estadoSala = EstadoSala.EN_CURSO,
            jugadores = listOf(j1, j2),
            rondaActual = 11,
            maxRondas = 10
        )

        val terminado = DetectorFinPartida.finalizar(estado)
        assertEquals(EstadoSala.TERMINADA, terminado.estadoSala)
        assertEquals("1", terminado.ganadorUid)
    }

    @Test
    fun empate_ganaQuienUsoMenosPoderes() {
        val j1 = Jugador(uid = "1", nombre = "J1", puntaje = 40, poderesUsados = 2)
        val j2 = Jugador(uid = "2", nombre = "J2", puntaje = 40, poderesUsados = 1)
        val estado = EstadoPartida(
            estadoSala = EstadoSala.EN_CURSO,
            jugadores = listOf(j1, j2),
            rondaActual = 11,
            maxRondas = 10
        )

        val terminado = DetectorFinPartida.finalizar(estado)
        assertEquals(EstadoSala.TERMINADA, terminado.estadoSala)
        assertEquals("2", terminado.ganadorUid)
    }
}
