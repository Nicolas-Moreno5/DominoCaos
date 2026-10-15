package com.example.dominocaos.model

/**
 * Representa a un usuario registrado en el sistema.
 *
 * @property uid Identificador único de Firebase Auth.
 * @property nombre Nombre visible del jugador.
 * @property correo Correo electrónico registrado.
 */
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = ""
)
