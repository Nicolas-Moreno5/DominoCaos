package com.example.dominocaos.logic

import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.EstadoSala
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.FichaColocada
import com.example.dominocaos.model.Jugador
import com.example.dominocaos.model.Lado
import com.example.dominocaos.model.Tablero
import com.example.dominocaos.model.TipoPoder
import com.example.dominocaos.model.crearPoderesIniciales
import kotlin.random.Random

object MotorJuego {

    fun crearSala(
        codigo: String,
        creador: Jugador,
        maxJugadores: Int = 4,
        maxRondas: Int = 10
    ): EstadoPartida {
        return EstadoPartida(
            codigo = codigo,
            estadoSala = EstadoSala.ESPERANDO,
            jugadores = listOf(creador),
            maxJugadores = maxJugadores,
            maxRondas = maxRondas
        )
    }

    fun unirse(estado: EstadoPartida, nuevoJugador: Jugador): Result<EstadoPartida> {
        if (estado.estadoSala != EstadoSala.ESPERANDO) {
            return Result.failure(IllegalStateException("La sala no está esperando jugadores"))
        }
        if (estado.jugadores.size >= estado.maxJugadores) {
            return Result.failure(IllegalStateException("La sala está llena"))
        }
        if (estado.jugadores.any { it.uid == nuevoJugador.uid }) {
            return Result.failure(IllegalArgumentException("El jugador ya está en la sala"))
        }
        val nuevosJugadores = estado.jugadores + nuevoJugador
        return Result.success(estado.copy(jugadores = nuevosJugadores))
    }

    fun iniciarPartida(
        estado: EstadoPartida,
        random: Random = Random.Default
    ): Result<EstadoPartida> {
        if (estado.estadoSala != EstadoSala.ESPERANDO) {
            return Result.failure(IllegalStateException("La partida no se encuentra en espera"))
        }
        if (estado.jugadores.size < 2) {
            return Result.failure(IllegalStateException("Se necesitan al menos 2 jugadores"))
        }

        val (manos, pozo) = GeneradorFichas.repartir(estado.jugadores.size, random)
        val jugadoresIniciales = estado.jugadores.mapIndexed { index, jugador ->
            jugador.copy(
                mano = manos[index],
                poderes = crearPoderesIniciales(),
                puntaje = 0,
                poderesUsados = 0,
                activo = true
            )
        }

        val nuevoEstado = estado.copy(
            estadoSala = EstadoSala.EN_CURSO,
            jugadores = jugadoresIniciales,
            pozo = pozo,
            tablero = Tablero(),
            turnoActual = 0,
            sentido = 1,
            rondaActual = 1,
            turnosJugadosEnRonda = 0,
            ganadorUid = null,
            ultimoEvento = null
        )

        return Result.success(nuevoEstado)
    }

    fun jugarFicha(
        estado: EstadoPartida,
        uid: String,
        ficha: Ficha,
        lado: Lado,
        poder: TipoPoder? = null,
        random: Random = Random.Default
    ): Result<EstadoPartida> {
        if (estado.estadoSala != EstadoSala.EN_CURSO) {
            return Result.failure(IllegalStateException("La partida no está en curso"))
        }

        val jugadorActual = estado.jugadores.getOrNull(estado.turnoActual)
            ?: return Result.failure(IllegalStateException("Índice de turno inválido"))

        if (jugadorActual.uid != uid) {
            return Result.failure(IllegalArgumentException("No es tu turno"))
        }

        if (!jugadorActual.mano.contains(ficha)) {
            return Result.failure(IllegalArgumentException("No tienes esa ficha en tu mano"))
        }

        if (poder == TipoPoder.INTERCAMBIO) {
            return Result.failure(IllegalArgumentException("El poder Intercambio debe usarse por separado"))
        }

        if (poder != null && !GestorPoderes.puedeUsar(jugadorActual, poder)) {
            return Result.failure(IllegalArgumentException("No te quedan usos para el poder seleccionado"))
        }

        val fichaColocada: FichaColocada
        if (poder == TipoPoder.COMODIN) {
            fichaColocada = if (ValidadorMovimientos.puedeColocar(ficha, estado.tablero, lado)) {
                ValidadorMovimientos.orientar(ficha, estado.tablero, lado)
            } else {
                FichaColocada(ficha)
            }
        } else {
            if (!ValidadorMovimientos.puedeColocar(ficha, estado.tablero, lado)) {
                return Result.failure(IllegalArgumentException("Movimiento no válido para este lado"))
            }
            fichaColocada = ValidadorMovimientos.orientar(ficha, estado.tablero, lado)
        }

        val fichasNuevas = if (lado == Lado.IZQUIERDO) {
            listOf(fichaColocada) + estado.tablero.fichas
        } else {
            estado.tablero.fichas + fichaColocada
        }
        val nuevoTablero = Tablero(fichas = fichasNuevas)

        val puntosObtenidos = CalculadorPuntaje.calcularPuntos(ficha, estado.eventoActivo)

        var jugadorActualizado = jugadorActual.copy(
            mano = jugadorActual.mano - ficha,
            puntaje = jugadorActual.puntaje + puntosObtenidos
        )

        if (poder != null) {
            jugadorActualizado = GestorPoderes.consumir(jugadorActualizado, poder)
        }

        val jugadoresActualizados = estado.jugadores.toMutableList()
        jugadoresActualizados[estado.turnoActual] = jugadorActualizado

        var estadoProcesado = estado.copy(
            tablero = nuevoTablero,
            jugadores = jugadoresActualizados
        )

        if (poder == TipoPoder.INVERSION) {
            estadoProcesado = GestorTurnos.invertirSentido(estadoProcesado)
        }

        val estadoSiguienteTurno = GestorTurnos.avanzarTurno(
            estadoProcesado,
            saltar = (poder == TipoPoder.SALTO)
        )

        if (DetectorFinPartida.haTerminado(estadoSiguienteTurno)) {
            return Result.success(DetectorFinPartida.finalizar(estadoSiguienteTurno))
        }

        return if (GestorEventos.debeOcurrir(random)) {
            val eventoSorteado = GestorEventos.elegirEvento(random)
            val estadoConEvento = GestorEventos.aplicar(estadoSiguienteTurno, eventoSorteado)
            if (DetectorFinPartida.haTerminado(estadoConEvento)) {
                Result.success(DetectorFinPartida.finalizar(estadoConEvento))
            } else {
                Result.success(estadoConEvento)
            }
        } else {
            Result.success(estadoSiguienteTurno.copy(ultimoEvento = null))
        }
    }

    fun tomarDelPozo(estado: EstadoPartida, uid: String): Result<EstadoPartida> {
        if (estado.estadoSala != EstadoSala.EN_CURSO) {
            return Result.failure(IllegalStateException("La partida no está en curso"))
        }

        val jugadorActual = estado.jugadores.getOrNull(estado.turnoActual)
            ?: return Result.failure(IllegalStateException("Índice de turno inválido"))

        if (jugadorActual.uid != uid) {
            return Result.failure(IllegalArgumentException("No es tu turno"))
        }

        if (ValidadorMovimientos.tieneMovimientoValido(jugadorActual.mano, estado.tablero)) {
            return Result.failure(IllegalArgumentException("Tienes movimientos válidos, no puedes robar"))
        }

        if (estado.pozo.isEmpty()) {
            return Result.failure(IllegalStateException("El pozo está vacío"))
        }

        val fichaRobada = estado.pozo.first()
        val nuevoPozo = estado.pozo.drop(1)

        val jugadorActualizado = jugadorActual.copy(
            mano = jugadorActual.mano + fichaRobada
        )

        val jugadoresActualizados = estado.jugadores.toMutableList()
        jugadoresActualizados[estado.turnoActual] = jugadorActualizado

        return Result.success(
            estado.copy(
                pozo = nuevoPozo,
                jugadores = jugadoresActualizados
            )
        )
    }

    fun pasarTurno(estado: EstadoPartida, uid: String): Result<EstadoPartida> {
        if (estado.estadoSala != EstadoSala.EN_CURSO) {
            return Result.failure(IllegalStateException("La partida no está en curso"))
        }

        val jugadorActual = estado.jugadores.getOrNull(estado.turnoActual)
            ?: return Result.failure(IllegalStateException("Índice de turno inválido"))

        if (jugadorActual.uid != uid) {
            return Result.failure(IllegalArgumentException("No es tu turno"))
        }

        if (ValidadorMovimientos.tieneMovimientoValido(jugadorActual.mano, estado.tablero)) {
            return Result.failure(IllegalArgumentException("Tienes movimientos válidos, no puedes pasar"))
        }

        if (estado.pozo.isNotEmpty()) {
            return Result.failure(IllegalArgumentException("Debes robar del pozo antes de pasar"))
        }

        val estadoSiguienteTurno = GestorTurnos.avanzarTurno(estado)

        if (DetectorFinPartida.haTerminado(estadoSiguienteTurno)) {
            return Result.success(DetectorFinPartida.finalizar(estadoSiguienteTurno))
        }

        return Result.success(estadoSiguienteTurno)
    }

    fun usarIntercambio(
        estado: EstadoPartida,
        uid: String,
        fichaPropia: Ficha,
        random: Random = Random.Default
    ): Result<EstadoPartida> {
        if (estado.estadoSala != EstadoSala.EN_CURSO) {
            return Result.failure(IllegalStateException("La partida no está en curso"))
        }

        val jugadorActual = estado.jugadores.getOrNull(estado.turnoActual)
            ?: return Result.failure(IllegalStateException("Índice de turno inválido"))

        if (jugadorActual.uid != uid) {
            return Result.failure(IllegalArgumentException("No es tu turno"))
        }

        if (!GestorPoderes.puedeUsar(jugadorActual, TipoPoder.INTERCAMBIO)) {
            return Result.failure(IllegalArgumentException("No tienes usos restantes de Intercambio"))
        }

        if (!jugadorActual.mano.contains(fichaPropia)) {
            return Result.failure(IllegalArgumentException("No tienes esa ficha en tu mano"))
        }

        val jugadorConsumido = GestorPoderes.consumir(jugadorActual, TipoPoder.INTERCAMBIO)
        val jugadoresActualizados = estado.jugadores.toMutableList()
        jugadoresActualizados[estado.turnoActual] = jugadorConsumido

        val estadoConPoderConsumido = estado.copy(jugadores = jugadoresActualizados)

        val estadoFinal = GestorPoderes.aplicarIntercambio(
            estado = estadoConPoderConsumido,
            indiceJugador = estado.turnoActual,
            fichaPropia = fichaPropia,
            random = random
        )

        return Result.success(estadoFinal)
    }

    fun abandonar(estado: EstadoPartida, uid: String): Result<EstadoPartida> {
        val indiceAbandonador = estado.jugadores.indexOfFirst { it.uid == uid }
        if (indiceAbandonador == -1) {
            return Result.failure(IllegalArgumentException("Jugador no encontrado"))
        }

        val jugadoresActualizados = estado.jugadores.toMutableList()
        val abandonador = jugadoresActualizados[indiceAbandonador]
        jugadoresActualizados[indiceAbandonador] = abandonador.copy(activo = false, mano = emptyList())

        var estadoActualizado = estado.copy(jugadores = jugadoresActualizados)

        val activos = jugadoresActualizados.count { it.activo }
        if (activos <= 1) {
            return Result.success(DetectorFinPartida.finalizar(estadoActualizado))
        }

        if (estado.turnoActual == indiceAbandonador && estado.estadoSala == EstadoSala.EN_CURSO) {
            estadoActualizado = GestorTurnos.avanzarTurno(estadoActualizado)
        }

        if (DetectorFinPartida.haTerminado(estadoActualizado)) {
            return Result.success(DetectorFinPartida.finalizar(estadoActualizado))
        }

        return Result.success(estadoActualizado)
    }

    fun reiniciar(
        estado: EstadoPartida,
        random: Random = Random.Default
    ): Result<EstadoPartida> {
        val jugadoresRestablecidos = estado.jugadores.map {
            it.copy(
                mano = emptyList(),
                poderes = crearPoderesIniciales(),
                puntaje = 0,
                poderesUsados = 0,
                activo = true
            )
        }
        val estadoReinicio = estado.copy(
            estadoSala = EstadoSala.ESPERANDO,
            jugadores = jugadoresRestablecidos,
            pozo = emptyList(),
            tablero = Tablero(),
            ganadorUid = null,
            ultimoEvento = null
        )
        return iniciarPartida(estadoReinicio, random)
    }
}
