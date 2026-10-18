package com.example.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dominocaos.data.PartidaRepository
import com.example.dominocaos.logic.MotorJuego
import com.example.dominocaos.logic.ValidadorMovimientos
import com.example.dominocaos.model.EstadoPartida
import com.example.dominocaos.model.EstadoSala
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.Jugador
import com.example.dominocaos.model.Lado
import com.example.dominocaos.model.TipoPoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JuegoUiState(
    val estadoPartida: EstadoPartida? = null,
    val esMiTurno: Boolean = false,
    val miJugador: Jugador? = null,
    val fichaSeleccionada: Ficha? = null,
    val poderSeleccionado: TipoPoder? = null,
    val ladosValidos: List<Lado> = emptyList(),
    val mensajeError: String? = null,
    val cargando: Boolean = false
)

class JuegoViewModel(
    private val codigoSala: String,
    private val uidUsuario: String,
    private val partidaRepository: PartidaRepository = PartidaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(JuegoUiState())
    val uiState: StateFlow<JuegoUiState> = _uiState.asStateFlow()

    init {
        observarPartida()
    }

    private fun observarPartida() {
        viewModelScope.launch {
            partidaRepository.observarPartida(codigoSala)
                .catch { excepcion ->
                    _uiState.update {
                        it.copy(mensajeError = excepcion.localizedMessage ?: "Error al sincronizar sala")
                    }
                }
                .collect { estado ->
                    if (estado != null) {
                        val miJugador = estado.jugadores.find { it.uid == uidUsuario }
                        val esMiTurno = estado.estadoSala == EstadoSala.EN_CURSO &&
                                estado.jugadores.getOrNull(estado.turnoActual)?.uid == uidUsuario

                        _uiState.update { actual ->
                            val lados = calcularLadosValidos(
                                actual.fichaSeleccionada,
                                actual.poderSeleccionado,
                                estado
                            )
                            actual.copy(
                                estadoPartida = estado,
                                miJugador = miJugador,
                                esMiTurno = esMiTurno,
                                ladosValidos = lados
                            )
                        }
                    }
                }
        }
    }

    private fun calcularLadosValidos(
        ficha: Ficha?,
        poder: TipoPoder?,
        estado: EstadoPartida?
    ): List<Lado> {
        if (ficha == null || estado == null) return emptyList()
        if (poder == TipoPoder.COMODIN) {
            return listOf(Lado.IZQUIERDO, Lado.DERECHO)
        }
        return ValidadorMovimientos.ladosValidos(ficha, estado.tablero)
    }

    fun seleccionarFicha(ficha: Ficha?) {
        _uiState.update { actual ->
            val nuevaFicha = if (actual.fichaSeleccionada == ficha) null else ficha
            val lados = calcularLadosValidos(nuevaFicha, actual.poderSeleccionado, actual.estadoPartida)
            actual.copy(
                fichaSeleccionada = nuevaFicha,
                ladosValidos = lados
            )
        }
    }

    fun seleccionarPoder(tipo: TipoPoder?) {
        _uiState.update { actual ->
            val nuevoPoder = if (actual.poderSeleccionado == tipo) null else tipo
            val lados = calcularLadosValidos(actual.fichaSeleccionada, nuevoPoder, actual.estadoPartida)
            actual.copy(
                poderSeleccionado = nuevoPoder,
                ladosValidos = lados
            )
        }
    }

    fun colocarFicha(lado: Lado) {
        val estadoActual = _uiState.value
        val ficha = estadoActual.fichaSeleccionada ?: return

        ejecutarAccionConCargando {
            partidaRepository.ejecutarAccion(codigoSala) { estado ->
                MotorJuego.jugarFicha(
                    estado = estado,
                    uid = uidUsuario,
                    ficha = ficha,
                    lado = lado,
                    poder = estadoActual.poderSeleccionado
                )
            }
        }
        limpiarSeleccion()
    }

    fun tomarDelPozo() {
        ejecutarAccionConCargando {
            partidaRepository.ejecutarAccion(codigoSala) { estado ->
                MotorJuego.tomarDelPozo(estado, uidUsuario)
            }
        }
        limpiarSeleccion()
    }

    fun pasarTurno() {
        ejecutarAccionConCargando {
            partidaRepository.ejecutarAccion(codigoSala) { estado ->
                MotorJuego.pasarTurno(estado, uidUsuario)
            }
        }
        limpiarSeleccion()
    }

    fun usarIntercambio(fichaPropia: Ficha) {
        ejecutarAccionConCargando {
            partidaRepository.ejecutarAccion(codigoSala) { estado ->
                MotorJuego.usarIntercambio(estado, uidUsuario, fichaPropia)
            }
        }
        limpiarSeleccion()
    }

    fun iniciarPartida() {
        ejecutarAccionConCargando {
            partidaRepository.ejecutarAccion(codigoSala) { estado ->
                MotorJuego.iniciarPartida(estado)
            }
        }
    }

    fun abandonar() {
        ejecutarAccionConCargando {
            partidaRepository.ejecutarAccion(codigoSala) { estado ->
                MotorJuego.abandonar(estado, uidUsuario)
            }
        }
    }

    fun reiniciar() {
        ejecutarAccionConCargando {
            partidaRepository.ejecutarAccion(codigoSala) { estado ->
                MotorJuego.reiniciar(estado)
            }
        }
    }

    private fun ejecutarAccionConCargando(bloque: suspend () -> Result<Unit>) {
        _uiState.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            val resultado = bloque()
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(cargando = false, mensajeError = null) }
                },
                onFailure = { excepcion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            mensajeError = excepcion.localizedMessage ?: "Error al procesar la acción"
                        )
                    }
                }
            )
        }
    }

    private fun limpiarSeleccion() {
        _uiState.update {
            it.copy(
                fichaSeleccionada = null,
                poderSeleccionado = null,
                ladosValidos = emptyList()
            )
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(mensajeError = null) }
    }

    fun limpiarEvento() {
        viewModelScope.launch {
            partidaRepository.ejecutarAccionDirecta(codigoSala) { estado ->
                estado.copy(ultimoEvento = null)
            }
        }
    }

    class Factory(
        private val codigoSala: String,
        private val uidUsuario: String,
        private val partidaRepository: PartidaRepository = PartidaRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JuegoViewModel::class.java)) {
                return JuegoViewModel(codigoSala, uidUsuario, partidaRepository) as T
            }
            throw IllegalArgumentException("ViewModel class no reconocida")
        }
    }
}
