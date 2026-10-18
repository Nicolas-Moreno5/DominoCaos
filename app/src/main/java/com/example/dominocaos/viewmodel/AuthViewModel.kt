package com.example.dominocaos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dominocaos.data.AuthRepository
import com.example.dominocaos.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val usuario: Usuario? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val usuarioActual = authRepository.usuarioActual()
        _uiState.update { it.copy(usuario = usuarioActual) }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _uiState.update { it.copy(error = "Por favor completa todos los campos") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val resultado = authRepository.iniciarSesion(correo.trim(), contrasena)
            resultado.fold(
                onSuccess = { usuario ->
                    _uiState.update { it.copy(cargando = false, usuario = usuario, error = null) }
                },
                onFailure = { excepcion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = excepcion.localizedMessage ?: "Error al iniciar sesión"
                        )
                    }
                }
            )
        }
    }

    fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String
    ) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank() || confirmarContrasena.isBlank()) {
            _uiState.update { it.copy(error = "Por favor completa todos los campos") }
            return
        }

        if (contrasena != confirmarContrasena) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val resultado = authRepository.registrar(nombre.trim(), correo.trim(), contrasena)
            resultado.fold(
                onSuccess = { usuario ->
                    _uiState.update { it.copy(cargando = false, usuario = usuario, error = null) }
                },
                onFailure = { excepcion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            error = excepcion.localizedMessage ?: "Error al registrar usuario"
                        )
                    }
                }
            )
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.update { it.copy(usuario = null, error = null) }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(
        private val authRepository: AuthRepository = AuthRepository()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(authRepository) as T
            }
            throw IllegalArgumentException("ViewModel class no reconocida")
        }
    }
}
