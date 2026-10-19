package com.example.dominocaos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dominocaos.model.Jugador

@Composable
fun JugadorCard(
    jugador: Jugador,
    esTurnoActual: Boolean,
    cantidadFichas: Int,
    modifier: Modifier = Modifier
) {
    val containerColor = if (esTurnoActual) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borde = if (esTurnoActual) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    val opacidad = if (jugador.activo) 1f else 0.4f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(opacidad),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borde,
        elevation = CardDefaults.cardElevation(defaultElevation = if (esTurnoActual) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jugador.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (esTurnoActual) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (esTurnoActual) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (esTurnoActual) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "TURNO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else if (!jugador.activo) {
                    Text(
                        text = "Fuera",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Puntos: ${jugador.puntaje}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esTurnoActual) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Fichas: $cantidadFichas",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (esTurnoActual) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
