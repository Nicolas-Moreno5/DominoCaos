package com.example.dominocaos.logic

import com.example.dominocaos.model.EventoActivo
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.TipoEvento

object CalculadorPuntaje {

    fun calcularPuntos(ficha: Ficha, eventoActivo: EventoActivo): Int {
        val puntosBase = ficha.puntos
        return if (eventoActivo.tipo == TipoEvento.PUNTOS_DOBLES) {
            puntosBase * 2
        } else {
            puntosBase
        }
    }
}
