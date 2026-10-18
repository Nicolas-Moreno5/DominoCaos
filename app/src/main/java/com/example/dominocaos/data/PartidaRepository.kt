package com.example.dominocaos.data

import com.example.dominocaos.model.EstadoPartida
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PartidaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLECCION_PARTIDAS = "partidas"
    }

    fun generarCodigo(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..4)
            .map { caracteres.random() }
            .joinToString("")
    }

    suspend fun crearSala(estado: EstadoPartida): Result<Unit> {
        return try {
            firestore.collection(COLECCION_PARTIDAS)
                .document(estado.codigo)
                .set(estado)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observarPartida(codigo: String): Flow<EstadoPartida?> = callbackFlow {
        val docRef = firestore.collection(COLECCION_PARTIDAS).document(codigo)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val estado = snapshot?.toObject(EstadoPartida::class.java)
            trySend(estado)
        }
        awaitClose { listener.remove() }
    }

    suspend fun ejecutarAccion(
        codigo: String,
        accion: (EstadoPartida) -> Result<EstadoPartida>
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection(COLECCION_PARTIDAS).document(codigo)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val estadoActual = snapshot.toObject(EstadoPartida::class.java)
                    ?: throw IllegalStateException("La sala $codigo no existe")

                val resultadoAccion = accion(estadoActual)
                val nuevoEstado = resultadoAccion.getOrThrow()
                transaction.set(docRef, nuevoEstado)
                null
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ejecutarAccionDirecta(
        codigo: String,
        accion: (EstadoPartida) -> EstadoPartida
    ): Result<Unit> {
        return ejecutarAccion(codigo) { estado -> Result.success(accion(estado)) }
    }

    suspend fun existeSala(codigo: String): Boolean {
        return try {
            val document = firestore.collection(COLECCION_PARTIDAS)
                .document(codigo)
                .get()
                .await()
            document.exists()
        } catch (_: Exception) {
            false
        }
    }
}
