package com.example.dominocaos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dominocaos.model.Poder
import com.example.dominocaos.model.TipoPoder

@Composable
fun PoderesPanel(
    poderes: List<Poder>,
    modifier: Modifier = Modifier,
    poderSeleccionado: TipoPoder? = null,
    habilitado: Boolean = true,
    onSeleccionar: (TipoPoder) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = "Poderes de Caos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                poderes.forEach { poder ->
                    val esSeleccionado = poderSeleccionado == poder.tipo
                    val sePuedeUsar = habilitado && poder.usosRestantes > 0

                    BotonPoderItem(
                        poder = poder,
                        seleccionado = esSeleccionado,
                        habilitado = sePuedeUsar,
                        modifier = Modifier.weight(1f),
                        onClick = { onSeleccionar(poder.tipo) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonPoderItem(
    poder: Poder,
    seleccionado: Boolean,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (nombre, icono) = when (poder.tipo) {
        TipoPoder.COMODIN -> "Comodín" to Icons.Default.AutoAwesome
        TipoPoder.SALTO -> "Salto" to Icons.Default.SkipNext
        TipoPoder.INTERCAMBIO -> "Cambio" to Icons.Default.SwapHoriz
        TipoPoder.INVERSION -> "Inversión" to Icons.Default.Repeat
    }

    val fondoColor = if (seleccionado) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contenidoColor = if (seleccionado) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borde = if (seleccionado) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else null

    Surface(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = fondoColor,
        contentColor = contenidoColor,
        border = borde,
        shadowElevation = if (seleccionado) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = nombre,
                modifier = Modifier.size(20.dp),
                tint = if (habilitado) contenidoColor else MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = nombre,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )

            Text(
                text = "${poder.usosRestantes} usos",
                style = MaterialTheme.typography.bodySmall,
                color = if (habilitado) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        }
    }
}
