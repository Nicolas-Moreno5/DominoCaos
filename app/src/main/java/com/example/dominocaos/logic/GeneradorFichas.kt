package com.example.dominocaos.logic

import com.example.dominocaos.model.Ficha
import kotlin.random.Random

object GeneradorFichas {

    fun crearSet(): List<Ficha> {
        val fichas = mutableListOf<Ficha>()
        for (a in 0..6) {
            for (b in a..6) {
                fichas.add(Ficha(a, b))
            }
        }
        return fichas
    }

    fun barajar(fichas: List<Ficha>, random: Random = Random.Default): List<Ficha> {
        return fichas.shuffled(random)
    }

    fun repartir(
        cantidadJugadores: Int,
        random: Random = Random.Default
    ): Pair<List<List<Ficha>>, List<Ficha>> {
        val fichasBarajadas = barajar(crearSet(), random)
        val manos = mutableListOf<List<Ficha>>()
        var indiceActual = 0

        repeat(cantidadJugadores) {
            val mano = fichasBarajadas.subList(indiceActual, indiceActual + 7)
            manos.add(mano)
            indiceActual += 7
        }

        val pozo = fichasBarajadas.subList(indiceActual, fichasBarajadas.size)
        return Pair(manos, pozo)
    }
}
