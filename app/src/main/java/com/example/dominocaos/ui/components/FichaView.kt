package com.example.dominocaos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dominocaos.model.Ficha

@Composable
fun FichaView(
    ficha: Ficha,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    seleccionada: Boolean = false,
    habilitada: Boolean = true,
    anchoMitad: Dp = 32.dp,
    onClick: (() -> Unit)? = null
) {
    val colorBorde = if (seleccionada) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val grosorBorde = if (seleccionada) 3.dp else 1.5.dp
    val colorPuntos = MaterialTheme.colorScheme.onSurface
    val colorFondo = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = modifier
            .alpha(if (habilitada) 1f else 0.5f)
            .clickable(enabled = habilitada && onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(8.dp),
        color = colorFondo,
        border = BorderStroke(grosorBorde, colorBorde),
        shadowElevation = if (seleccionada) 6.dp else 2.dp
    ) {
        if (horizontal) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MitadFicha(
                    valor = ficha.a,
                    colorPuntos = colorPuntos,
                    modifier = Modifier
                        .size(anchoMitad)
                        .aspectRatio(1f)
                )
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MitadFicha(
                    valor = ficha.b,
                    colorPuntos = colorPuntos,
                    modifier = Modifier
                        .size(anchoMitad)
                        .aspectRatio(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MitadFicha(
                    valor = ficha.a,
                    colorPuntos = colorPuntos,
                    modifier = Modifier
                        .size(anchoMitad)
                        .aspectRatio(1f)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MitadFicha(
                    valor = ficha.b,
                    colorPuntos = colorPuntos,
                    modifier = Modifier
                        .size(anchoMitad)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
private fun MitadFicha(
    valor: Int,
    colorPuntos: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension * 0.09f
        val puntos = when (valor) {
            1 -> listOf(Offset(0.5f, 0.5f))
            2 -> listOf(Offset(0.25f, 0.25f), Offset(0.75f, 0.75f))
            3 -> listOf(Offset(0.25f, 0.25f), Offset(0.5f, 0.5f), Offset(0.75f, 0.75f))
            4 -> listOf(
                Offset(0.25f, 0.25f), Offset(0.75f, 0.25f),
                Offset(0.25f, 0.75f), Offset(0.75f, 0.75f)
            )
            5 -> listOf(
                Offset(0.25f, 0.25f), Offset(0.75f, 0.25f),
                Offset(0.5f, 0.5f),
                Offset(0.25f, 0.75f), Offset(0.75f, 0.75f)
            )
            6 -> listOf(
                Offset(0.25f, 0.22f), Offset(0.75f, 0.22f),
                Offset(0.25f, 0.50f), Offset(0.75f, 0.50f),
                Offset(0.25f, 0.78f), Offset(0.75f, 0.78f)
            )
            else -> emptyList()
        }

        for (p in puntos) {
            drawCircle(
                color = colorPuntos,
                radius = radius,
                center = Offset(p.x * size.width, p.y * size.height)
            )
        }
    }
}
