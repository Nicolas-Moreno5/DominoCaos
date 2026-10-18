package com.example.dominocaos.data

import com.example.dominocaos.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun registrar(nombre: String, correo: String, contrasena: String): Result<Usuario> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(correo, contrasena).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Usuario no obtenido tras el registro")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(nombre)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            val usuario = Usuario(
                uid = firebaseUser.uid,
                nombre = nombre,
                correo = firebaseUser.email ?: correo
            )
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(correo, contrasena).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Usuario no obtenido")
            val usuario = Usuario(
                uid = firebaseUser.uid,
                nombre = firebaseUser.displayName ?: "",
                correo = firebaseUser.email ?: correo
            )
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesionConGoogle(idToken: String): Result<Usuario> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Usuario no obtenido con Google")
            val usuario = Usuario(
                uid = firebaseUser.uid,
                nombre = firebaseUser.displayName ?: "",
                correo = firebaseUser.email ?: ""
            )
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun usuarioActual(): Usuario? {
        val firebaseUser = auth.currentUser ?: return null
        return Usuario(
            uid = firebaseUser.uid,
            nombre = firebaseUser.displayName ?: "",
            correo = firebaseUser.email ?: ""
        )
    }
}
