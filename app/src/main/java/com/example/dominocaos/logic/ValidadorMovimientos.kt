package com.example.dominocaos.logic

import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.FichaColocada
import com.example.dominocaos.model.Lado
import com.example.dominocaos.model.Tablero

object ValidadorMovimientos {

    fun puedeColocar(ficha: Ficha, tablero: Tablero, lado: Lado): Boolean {
        if (tablero.esVacio) return true
        val extremo = if (lado == Lado.IZQUIERDO) tablero.extremoIzquierdo else tablero.extremoDerecho
        return extremo != null && (ficha.a == extremo || ficha.b == extremo)
    }

    fun ladosValidos(ficha: Ficha, tablero: Tablero): List<Lado> {
        return Lado.entries.filter { puedeColocar(ficha, tablero, it) }
    }

    fun orientar(ficha: Ficha, tablero: Tablero, lado: Lado): FichaColocada {
        if (tablero.esVacio) return FichaColocada(ficha)

        val fichaOrientada = when (lado) {
            Lado.IZQUIERDO -> {
                val extremo = tablero.extremoIzquierdo
                if (ficha.b == extremo) ficha else ficha.voltear()
            }
            Lado.DERECHO -> {
                val extremo = tablero.extremoDerecho
                if (ficha.a == extremo) ficha else ficha.voltear()
            }
        }
        return FichaColocada(fichaOrientada)
    }

    fun tieneMovimientoValido(mano: List<Ficha>, tablero: Tablero): Boolean {
        if (tablero.esVacio) return mano.isNotEmpty()
        return mano.any { ficha ->
            puedeColocar(ficha, tablero, Lado.IZQUIERDO) || puedeColocar(ficha, tablero, Lado.DERECHO)
        }
    }
}
