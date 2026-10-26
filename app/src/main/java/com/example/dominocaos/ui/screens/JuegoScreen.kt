package com.example.dominocaos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dominocaos.model.EstadoSala
import com.example.dominocaos.model.Ficha
import com.example.dominocaos.model.Lado
import com.example.dominocaos.model.TipoPoder
import com.example.dominocaos.ui.components.ControlesPartida
import com.example.dominocaos.ui.components.EventoDialog
import com.example.dominocaos.ui.components.FichaView
import com.example.dominocaos.ui.components.JugadorCard
import com.example.dominocaos.ui.components.PoderesPanel
import com.example.dominocaos.ui.components.TableroView
import com.example.dominocaos.viewmodel.JuegoUiState

@Composable
fun JuegoScreen(
    estado: JuegoUiState,
    onSeleccionarFicha: (Ficha) -> Unit,
    onSeleccionarPoder: (TipoPoder) -> Unit,
    onColocarFicha: (Lado) -> Unit,
    onTomarPozo: () -> Unit,
    onPasarTurno: () -> Unit,
    onUsarIntercambio: (Ficha) -> Unit,
    onAbandonar: () -> Unit,
    onNavegarAResultado: () -> Unit,
    onLimpiarEvento: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val partida = estado.estadoPartida
    val miJugador = estado.miJugador
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(partida?.estadoSala) {
        if (partida?.estadoSala == EstadoSala.TERMINADA) {
            onNavegarAResultado()
        }
    }

    LaunchedEffect(estado.mensajeError) {
        if (estado.mensajeError != null) {
            snackbarHostState.showSnackbar(estado.mensajeError)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabecera e Indicador de Ronda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sala: ${partida?.codigo ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if ((partida?.sentido ?: 1) >= 0) "Sentido: Horario ↻" else "Sentido: Invertido ↺",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Ronda ${partida?.rondaActual ?: 1}/${partida?.maxRondas ?: 10}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Fila de Jugadores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            partida?.jugadores?.forEachIndexed { index, jugador ->
                val esEsteTurno = index == partida.turnoActual
                JugadorCard(
                    jugador = jugador,
                    esTurnoActual = esEsteTurno,
                    cantidadFichas = jugador.mano.size,
                    modifier = Modifier.width(150.dp)
                )
            }
        }

        // Tablero
        TableroView(
            tablero = partida?.tablero ?: com.example.dominocaos.model.Tablero(),
            ladosResaltados = if (estado.esMiTurno) estado.ladosValidos else emptyList(),
            onLadoClick = onColocarFicha
        )

        // Banner de Evento Activo (si hay)
        partida?.eventoActivo?.tipo?.let { tipoEvento ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Evento: ${tipoEvento.nombre}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "${partida.eventoActivo.turnosRestantes} t. restantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Panel de Poderes
        PoderesPanel(
            poderes = miJugador?.poderes ?: emptyList(),
            poderSeleccionado = estado.poderSeleccionado,
            habilitado = estado.esMiTurno,
            onSeleccionar = onSeleccionarPoder
        )

        // Mi Mano
        Text(
            text = "Mi Mano (${miJugador?.mano?.size ?: 0} fichas)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            miJugador?.mano?.forEach { ficha ->
                val estaSeleccionada = estado.fichaSeleccionada == ficha
                FichaView(
                    ficha = ficha,
                    horizontal = false,
                    seleccionada = estaSeleccionada,
                    habilitada = estado.esMiTurno,
                    onClick = {
                        if (estado.poderSeleccionado == TipoPoder.INTERCAMBIO) {
                            onUsarIntercambio(ficha)
                        } else {
                            onSeleccionarFicha(ficha)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Controles
        val tieneMovimientoValido = miJugador != null &&
                com.example.dominocaos.logic.ValidadorMovimientos.tieneMovimientoValido(
                    miJugador.mano,
                    partida?.tablero ?: com.example.dominocaos.model.Tablero()
                )

        val pozoVacio = (partida?.pozo?.size ?: 0) == 0

        ControlesPartida(
            fichasEnPozo = partida?.pozo?.size ?: 0,
            puedeTomarPozo = estado.esMiTurno && !tieneMovimientoValido && !pozoVacio,
            puedePasarTurno = estado.esMiTurno && !tieneMovimientoValido && pozoVacio,
            onTomarPozo = onTomarPozo,
            onPasarTurno = onPasarTurno,
            onAbandonar = onAbandonar
        )
    }

    // Diálogo de Evento cuando un evento ocurrió
    partida?.ultimoEvento?.let { evento ->
        EventoDialog(
            evento = evento,
            onContinuar = {
                onLimpiarEvento()
            }
        )
    }
}
