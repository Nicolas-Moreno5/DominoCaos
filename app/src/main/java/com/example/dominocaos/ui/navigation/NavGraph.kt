package com.example.dominocaos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dominocaos.ui.screens.JuegoScreen
import com.example.dominocaos.ui.screens.LobbyScreen
import com.example.dominocaos.ui.screens.LoginScreen
import com.example.dominocaos.ui.screens.RegistroScreen
import com.example.dominocaos.ui.screens.ResultadoScreen
import com.example.dominocaos.ui.screens.SalaEsperaScreen
import com.example.dominocaos.viewmodel.AuthViewModel
import com.example.dominocaos.viewmodel.JuegoViewModel
import com.example.dominocaos.viewmodel.LobbyViewModel

object Destinos {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val LOBBY = "lobby"
    const val SALA = "sala/{codigo}"
    const val JUEGO = "juego/{codigo}"
    const val RESULTADO = "resultado/{codigo}"
}

@Composable
fun DominoCaosNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory())
) {
    val authState by authViewModel.uiState.collectAsState()
    val usuario = authState.usuario

    val startDestination = if (usuario != null) Destinos.LOBBY else Destinos.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Destinos.LOGIN) {
            LoginScreen(
                estado = authState,
                onIniciarSesion = { correo, contrasena ->
                    authViewModel.iniciarSesion(correo, contrasena)
                },
                onIrARegistro = {
                    authViewModel.limpiarError()
                    navController.navigate(Destinos.REGISTRO)
                }
            )
        }

        composable(Destinos.REGISTRO) {
            RegistroScreen(
                estado = authState,
                onRegistrar = { nombre, correo, contrasena, confirmar ->
                    authViewModel.registrar(nombre, correo, contrasena, confirmar)
                },
                onVolver = {
                    authViewModel.limpiarError()
                    navController.popBackStack()
                }
            )
        }

        composable(Destinos.LOBBY) {
            val usuarioActual = usuario ?: return@composable
            val lobbyViewModel: LobbyViewModel = viewModel(factory = LobbyViewModel.Factory())
            val lobbyState by lobbyViewModel.uiState.collectAsState()

            LobbyScreen(
                estado = lobbyState,
                usuario = usuarioActual,
                onCrearPartida = { maxJ, maxR ->
                    lobbyViewModel.crearPartida(usuarioActual, maxJ, maxR)
                },
                onUnirse = { codigo ->
                    lobbyViewModel.unirse(codigo, usuarioActual)
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(Destinos.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavegarASala = { codigo ->
                    lobbyViewModel.limpiarNavegacion()
                    navController.navigate("sala/$codigo")
                }
            )
        }

        composable(
            route = Destinos.SALA,
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
            val usuarioActual = usuario ?: return@composable
            val juegoViewModel: JuegoViewModel = viewModel(
                factory = JuegoViewModel.Factory(codigo, usuarioActual.uid)
            )
            val juegoState by juegoViewModel.uiState.collectAsState()

            SalaEsperaScreen(
                estado = juegoState,
                onIniciarPartida = { juegoViewModel.iniciarPartida() },
                onSalir = {
                    juegoViewModel.abandonar()
                    navController.navigate(Destinos.LOBBY) {
                        popUpTo(Destinos.LOBBY) { inclusive = true }
                    }
                },
                onNavegarAJuego = {
                    navController.navigate("juego/$codigo") {
                        popUpTo("sala/$codigo") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Destinos.JUEGO,
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
            val usuarioActual = usuario ?: return@composable
            val juegoViewModel: JuegoViewModel = viewModel(
                factory = JuegoViewModel.Factory(codigo, usuarioActual.uid)
            )
            val juegoState by juegoViewModel.uiState.collectAsState()

            JuegoScreen(
                estado = juegoState,
                onSeleccionarFicha = { juegoViewModel.seleccionarFicha(it) },
                onSeleccionarPoder = { juegoViewModel.seleccionarPoder(it) },
                onColocarFicha = { juegoViewModel.colocarFicha(it) },
                onTomarPozo = { juegoViewModel.tomarDelPozo() },
                onPasarTurno = { juegoViewModel.pasarTurno() },
                onUsarIntercambio = { juegoViewModel.usarIntercambio(it) },
                onAbandonar = {
                    juegoViewModel.abandonar()
                    navController.navigate(Destinos.LOBBY) {
                        popUpTo(Destinos.LOBBY) { inclusive = true }
                    }
                },
                onNavegarAResultado = {
                    navController.navigate("resultado/$codigo") {
                        popUpTo("juego/$codigo") { inclusive = true }
                    }
                },
                onLimpiarEvento = {
                    juegoViewModel.limpiarEvento()
                }
            )
        }

        composable(
            route = Destinos.RESULTADO,
            arguments = listOf(navArgument("codigo") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
            val usuarioActual = usuario ?: return@composable
            val juegoViewModel: JuegoViewModel = viewModel(
                factory = JuegoViewModel.Factory(codigo, usuarioActual.uid)
            )
            val juegoState by juegoViewModel.uiState.collectAsState()

            ResultadoScreen(
                estado = juegoState,
                onReiniciar = {
                    juegoViewModel.reiniciar()
                    navController.navigate("juego/$codigo") {
                        popUpTo("resultado/$codigo") { inclusive = true }
                    }
                },
                onVolverAlMenu = {
                    navController.navigate(Destinos.LOBBY) {
                        popUpTo(Destinos.LOBBY) { inclusive = true }
                    }
                }
            )
        }
    }
}
