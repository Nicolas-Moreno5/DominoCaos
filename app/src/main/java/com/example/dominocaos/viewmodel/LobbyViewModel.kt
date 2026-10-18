package com.example.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dominocaos.data.PartidaRepository
import com.example.dominocaos.logic.MotorJuego
import com.example.dominocaos.model.Jugador
import com.example.dominocaos.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LobbyUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val codigoSalaCreada: String? = null,
    val codigoUnido: String? = null
)

class LobbyViewModel(
    private val partidaRepository: PartidaRepository = PartidaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    fun crearPartida(usuario: Usuario, maxJugadores: Int = 4, maxRondas: Int = 10) {
        if (usuario.uid.isBlank()) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val codigo = partidaRepository.generarCodigo()
            val creador = Jugador(uid = usuario.uid, nombre = usuario.nombre)
            val nuevoEstado = MotorJuego.crearSala(
                codigo = codigo,
                creador = creador,
                maxJugadores = maxJugadores,
                maxRondas = maxRondas
            )

            val resultado = partidaRepository.crearSala(nuevoEstado)
            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            codigoSalaCreada = codigo,
                            error = null
                        )
                    }
                },
                onFailure = { excepcion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = excepcion.localizedMessage ?: "Error al crear la sala"
                        )
                    }
                }
            )
        }
    }

    fun unirse(codigo: String, usuario: Usuario) {
        val codigoLimpio = codigo.trim().uppercase()
        if (codigoLimpio.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa un código de sala válido") }
            return
        }

        if (usuario.uid.isBlank()) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val existe = partidaRepository.existeSala(codigoLimpio)
            if (!existe) {
                _uiState.update {
                    it.copy(cargando = false, error = "La sala $codigoLimpio no existe")
                }
                return@launch
            }

            val nuevoJugador = Jugador(uid = usuario.uid, nombre = usuario.nombre)
            val resultado = partidaRepository.ejecutarAccion(codigoLimpio) { estadoActual ->
                MotorJuego.unirse(estadoActual, nuevoJugador)
            }

            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            codigoUnido = codigoLimpio,
                            error = null
                        )
                    }
                },
                onFailure = { excepcion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = excepcion.localizedMessage ?: "Error al unirse a la sala"
                        )
                    }
                }
            )
        }
    }

    fun limpiarNavegacion() {
        _uiState.update {
            it.copy(
                codigoSalaCreada = null,
                codigoUnido = null
            )
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(
        private val partidaRepository: PartidaRepository = PartidaRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LobbyViewModel::class.java)) {
                return LobbyViewModel(partidaRepository) as T
            }
            throw IllegalArgumentException("ViewModel class no reconocida")
        }
    }
}
