package com.example.dominocaos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ControlesPartida(
    fichasEnPozo: Int,
    puedeTomarPozo: Boolean,
    puedePasarTurno: Boolean,
    modifier: Modifier = Modifier,
    mostrarReiniciar: Boolean = false,
    onTomarPozo: () -> Unit = {},
    onPasarTurno: () -> Unit = {},
    onAbandonar: () -> Unit = {},
    onReiniciar: () -> Unit = {}
) {
    var mostrarDialogoAbandonar by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onTomarPozo,
            enabled = puedeTomarPozo,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Robar ($fichasEnPozo)", maxLines = 1)
        }

        Button(
            onClick = onPasarTurno,
            enabled = puedePasarTurno,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Pasar", maxLines = 1)
        }

        if (mostrarReiniciar) {
            OutlinedButton(
                onClick = onReiniciar,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Reiniciar", maxLines = 1)
            }
        }

        OutlinedButton(
            onClick = { mostrarDialogoAbandonar = true },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Salir"
            )
        }
    }

    if (mostrarDialogoAbandonar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAbandonar = false },
            title = { Text("¿Abandonar la partida?") },
            text = { Text("Si abandonas la partida, quedarás fuera del juego.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoAbandonar = false
                        onAbandonar()
                    }
                ) {
                    Text("Abandonar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAbandonar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
