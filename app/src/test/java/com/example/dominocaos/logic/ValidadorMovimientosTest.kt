package com.example.dominocaos.logic

import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.FichaColocada
import com.example.dominocaos.model.Lado
import com.example.dominocaos.model.Tablero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorMovimientosTest {

    @Test
    fun puedeColocar_tableroVacio_aceptaCualquierFicha() {
        val tablero = Tablero()
        val ficha = Ficha(6, 6)

        assertTrue(ValidadorMovimientos.puedeColocar(ficha, tablero, Lado.IZQUIERDO))
        assertTrue(ValidadorMovimientos.puedeColocar(ficha, tablero, Lado.DERECHO))
    }

    @Test
    fun puedeColocar_tableroConFichas_validaExtremosCorrectamente() {
        val tablero = Tablero(listOf(FichaColocada(Ficha(3, 5))))

        assertTrue(ValidadorMovimientos.puedeColocar(Ficha(3, 1), tablero, Lado.IZQUIERDO))
        assertFalse(ValidadorMovimientos.puedeColocar(Ficha(3, 1), tablero, Lado.DERECHO))

        assertTrue(ValidadorMovimientos.puedeColocar(Ficha(2, 5), tablero, Lado.DERECHO))
        assertFalse(ValidadorMovimientos.puedeColocar(Ficha(2, 5), tablero, Lado.IZQUIERDO))

        assertFalse(ValidadorMovimientos.puedeColocar(Ficha(1, 4), tablero, Lado.IZQUIERDO))
        assertFalse(ValidadorMovimientos.puedeColocar(Ficha(1, 4), tablero, Lado.DERECHO))
    }

    @Test
    fun ladosValidos_retornaLadosCorrectos() {
        val tablero = Tablero(listOf(FichaColocada(Ficha(3, 3))))
        val fichaCoincideAmbos = Ficha(3, 4)

        val lados = ValidadorMovimientos.ladosValidos(fichaCoincideAmbos, tablero)
        assertEquals(2, lados.size)
        assertTrue(lados.contains(Lado.IZQUIERDO))
        assertTrue(lados.contains(Lado.DERECHO))
    }

    @Test
    fun orientar_orientaYVolteaSegunLado() {
        val tablero = Tablero(listOf(FichaColocada(Ficha(3, 5))))

        val orientadaIzquierda = ValidadorMovimientos.orientar(Ficha(3, 1), tablero, Lado.IZQUIERDO)
        assertEquals(Ficha(1, 3), orientadaIzquierda.ficha)

        val orientadaIzquierdaSinVolteo = ValidadorMovimientos.orientar(Ficha(1, 3), tablero, Lado.IZQUIERDO)
        assertEquals(Ficha(1, 3), orientadaIzquierdaSinVolteo.ficha)

        val orientadaDerecha = ValidadorMovimientos.orientar(Ficha(2, 5), tablero, Lado.DERECHO)
        assertEquals(Ficha(5, 2), orientadaDerecha.ficha)

        val orientadaDerechaSinVolteo = ValidadorMovimientos.orientar(Ficha(5, 2), tablero, Lado.DERECHO)
        assertEquals(Ficha(5, 2), orientadaDerechaSinVolteo.ficha)
    }

    @Test
    fun tieneMovimientoValido_evaluaManoCorrectamente() {
        val tablero = Tablero(listOf(FichaColocada(Ficha(3, 5))))

        val manoValida = listOf(Ficha(1, 4), Ficha(3, 2))
        val manoInvalida = listOf(Ficha(1, 4), Ficha(2, 6))

        assertTrue(ValidadorMovimientos.tieneMovimientoValido(manoValida, tablero))
        assertFalse(ValidadorMovimientos.tieneMovimientoValido(manoInvalida, tablero))
    }
}
