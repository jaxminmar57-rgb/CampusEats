package com.jaz.myapplicationcampuseats.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository
import com.jaz.myapplicationcampuseats.model.Usuario
import com.jaz.myapplicationcampuseats.ui.*

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val HOME = "home"
    const val CATEGORIA = "categoria/{categoria}"
    const val CARRITO = "carrito"
    const val PEDIDO_DETALLE = "pedido/{pedidoId}"
    const val PEDIDOS_VENDEDOR = "pedidos_vendedor"
    const val CHAT = "chat/{pedidoId}/{otroNombre}"
    const val CUENTA = "cuenta"
    const val HISTORIAL = "historial"
    const val AJUSTES = "ajustes"
    const val NOTIFICACIONES = "notificaciones"
    const val PUBLICAR = "publicar"
    const val MIS_PUBLICACIONES = "mis_publicaciones"

    fun categoria(cat: String) = "categoria/$cat"
    fun pedidoDetalle(pedidoId: String) = "pedido/$pedidoId"
    fun chat(pedidoId: String, otroNombre: String) = "chat/$pedidoId/$otroNombre"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Usuario logueado en memoria (evita leer Firestore en cada pantalla)
    var usuarioActual by remember { mutableStateOf<Usuario?>(null) }

    val startDestination = if (UsuarioRepository.hayUsuarioLogueado()) {
        Routes.HOME
    } else {
        Routes.SPLASH
    }

    // Si hay sesión activa, cargar perfil una sola vez
    LaunchedEffect(Unit) {
        if (UsuarioRepository.hayUsuarioLogueado()) {
            UsuarioRepository.obtenerUsuarioActual(
                onSuccess = { usuarioActual = it }
            )
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SPLASH) {
            SplashScreen(onFinish = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onRegistrarme = { navController.navigate(Routes.REGISTRO) },
                onEntrar = { usuario ->
                    usuarioActual = usuario
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTRO) {
            RegistroScreen(
                onVolver = { navController.popBackStack() },
                onEntrar = { usuario ->
                    usuarioActual = usuario
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTRO) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                usuario = usuarioActual,
                onCarrito = { navController.navigate(Routes.CARRITO) },
                onCerrarSesion = {
                    UsuarioRepository.cerrarSesion()
                    usuarioActual = null
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onCuenta = { navController.navigate(Routes.CUENTA) },
                onHistorial = { navController.navigate(Routes.HISTORIAL) },
                onCategoria = { cat -> navController.navigate(Routes.categoria(cat)) },
                onPublicar = { navController.navigate(Routes.PUBLICAR) },
                onNotificaciones = { navController.navigate(Routes.NOTIFICACIONES) },
                onPedidosVendedor = { navController.navigate(Routes.PEDIDOS_VENDEDOR) },
                onAjustes = { navController.navigate(Routes.AJUSTES) },
                onMisPublicaciones = { navController.navigate(Routes.MIS_PUBLICACIONES) }
            )
        }

        composable(
            route = Routes.CATEGORIA,
            arguments = listOf(navArgument("categoria") { type = NavType.StringType })
        ) { backStack ->
            val categoria = backStack.arguments?.getString("categoria") ?: ""
            CategoriaScreen(
                categoria = categoria,
                usuarioId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Routes.CARRITO) {
            CarritoScreen(
                userId = usuarioActual?.uid ?: "",
                nombreCliente = usuarioActual?.nombre ?: "",
                onVolver = { navController.popBackStack() },
                onPedidoCreado = { pedidoId ->
                    navController.navigate(Routes.pedidoDetalle(pedidoId)) {
                        popUpTo(Routes.CARRITO) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.PEDIDO_DETALLE,
            arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
        ) { backStack ->
            val pedidoId = backStack.arguments?.getString("pedidoId") ?: ""
            PedidoDetalleScreen(
                pedidoId = pedidoId,
                usuarioActual = usuarioActual,
                onVolver = { navController.popBackStack() },
                onChat = { otroNombre ->
                    navController.navigate(Routes.chat(pedidoId, otroNombre))
                }
            )
        }

        composable(Routes.PEDIDOS_VENDEDOR) {
            PedidosVendedorScreen(
                vendedorId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() },
                onAbrirPedido = { pedidoId ->
                    navController.navigate(Routes.pedidoDetalle(pedidoId))
                }
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(
                navArgument("pedidoId") { type = NavType.StringType },
                navArgument("otroNombre") { type = NavType.StringType }
            )
        ) { backStack ->
            val pedidoId = backStack.arguments?.getString("pedidoId") ?: ""
            val otroNombre = backStack.arguments?.getString("otroNombre") ?: ""
            ChatScreen(
                pedidoId = pedidoId,
                usuarioActual = usuarioActual,
                otroNombre = otroNombre,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Routes.CUENTA) {
            CuentaScreen(
                usuario = usuarioActual,
                onVolver = { navController.popBackStack() },
                onUsuarioActualizado = { usuarioActual = it }
            )
        }

        composable(Routes.HISTORIAL) {
            HistorialScreen(
                userId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() },
                onAbrirPedido = { pedidoId ->
                    navController.navigate(Routes.pedidoDetalle(pedidoId))
                }
            )
        }

        composable(Routes.AJUSTES) {
            AjustesScreen(
                usuario = usuarioActual,
                onVolver = { navController.popBackStack() },
                onUsuarioActualizado = { usuarioActual = it },
                onCerrarSesion = {
                    UsuarioRepository.cerrarSesion()
                    usuarioActual = null
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NOTIFICACIONES) {
            NotificacionesScreen(onVolver = { navController.popBackStack() })
        }

        composable(Routes.PUBLICAR) {
            PublicarScreen(
                userId = usuarioActual?.uid ?: "",
                nombreVendedor = usuarioActual?.nombre ?: "",
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Routes.MIS_PUBLICACIONES) {
            MisPublicacionesScreen(
                vendedorId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
