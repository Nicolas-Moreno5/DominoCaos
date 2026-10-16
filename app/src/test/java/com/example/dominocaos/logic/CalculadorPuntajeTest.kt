package com.example.dominocaos.logic

import com.example.dominocaos.model.EventoActivo
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.TipoEvento
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadorPuntajeTest {

    @Test
    fun calcularPuntos_sinEvento_retornaPuntosBase() {
        val ficha = Ficha(4, 5)
        val eventoSinTipo = EventoActivo()

        val puntos = CalculadorPuntaje.calcularPuntos(ficha, eventoSinTipo)

        assertEquals(9, puntos)
    }

    @Test
    fun calcularPuntos_conPuntosDobles_retornaPuntosDuplicados() {
        val ficha = Ficha(4, 5)
        val eventoPuntosDobles = EventoActivo(tipo = TipoEvento.PUNTOS_DOBLES, turnosRestantes = 2)

        val puntos = CalculadorPuntaje.calcularPuntos(ficha, eventoPuntosDobles)

        assertEquals(18, puntos)
    }

    @Test
    fun calcularPuntos_conOtroEvento_retornaPuntosBase() {
        val ficha = Ficha(3, 3)
        val eventoTormenta = EventoActivo(tipo = TipoEvento.TORMENTA_DE_FICHAS, turnosRestantes = 1)

        val puntos = CalculadorPuntaje.calcularPuntos(ficha, eventoTormenta)

        assertEquals(6, puntos)
    }
}
