package com.jaz.myapplicationcampuseats.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository
import com.jaz.myapplicationcampuseats.repository.FcmRepository
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
    const val CHATS = "chats"
    const val EDITAR_PRODUCTO = "editar_producto/{productoId}"
    const val TIENDA = "tienda/{vendedorId}"
    const val PERFIL = "perfil/{uid}"

    fun categoria(cat: String) = "categoria/$cat"
    fun pedidoDetalle(pedidoId: String) = "pedido/$pedidoId"
    fun editarProducto(productoId: String) = "editar_producto/$productoId"
    fun chat(pedidoId: String, otroNombre: String) = "chat/$pedidoId/$otroNombre"
    fun tienda(vendedorId: String) = "tienda/$vendedorId"
    fun perfil(uid: String) = "perfil/$uid"
}

@Composable
fun AppNavigation(
    deepLinkDestino: String? = null,
    deepLinkPedidoId: String? = null,
    deepLinkOtroNombre: String? = null
) {
    val navController = rememberNavController()
    // Navegar al destino del deep link (notificación)
    LaunchedEffect(deepLinkDestino, deepLinkPedidoId) {
        if (deepLinkPedidoId.isNullOrEmpty()) return@LaunchedEffect
        when (deepLinkDestino) {
            "pedido" -> navController.navigate(Routes.pedidoDetalle(deepLinkPedidoId))
            "chat"   -> {
                val otro = deepLinkOtroNombre ?: ""
                navController.navigate(Routes.chat(deepLinkPedidoId, otro))
            }
            "historial" -> navController.navigate(Routes.HISTORIAL)
        }
    }


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
                    FcmRepository.registrarTokenActual()
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
                    FcmRepository.registrarTokenActual()
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
                onMisPublicaciones = { navController.navigate(Routes.MIS_PUBLICACIONES) },
                onChats = { navController.navigate(Routes.CHATS) },
                onAbrirPedido = { pedidoId -> navController.navigate(Routes.pedidoDetalle(pedidoId)) },
                onAbrirChat = { pedidoId, otroNombre -> navController.navigate(Routes.chat(pedidoId, otroNombre)) },
                onVerTienda = { vendedorId -> navController.navigate(Routes.tienda(vendedorId)) }
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
                onVolver = { navController.popBackStack() },
                onVerTienda = { vendedorId ->
                    navController.navigate(Routes.tienda(vendedorId))
                }
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
                },
                onVerPerfil = { uid -> navController.navigate(Routes.perfil(uid)) }
            )
        }

        composable(Routes.PEDIDOS_VENDEDOR) {
            PedidosVendedorScreen(
                vendedorId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() },
                onAbrirPedido = { pedidoId -> navController.navigate(Routes.pedidoDetalle(pedidoId)) },
                onAbrirChat = { pedidoId, otroNombre -> navController.navigate(Routes.chat(pedidoId, otroNombre)) }
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
                onVolver = { navController.popBackStack() },
                onVerPedido = { navController.navigate(Routes.pedidoDetalle(pedidoId)) },
                onVerPerfil = { uid -> navController.navigate(Routes.perfil(uid)) }
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
                onAbrirPedido = { pedidoId -> navController.navigate(Routes.pedidoDetalle(pedidoId)) },
                onAbrirChat = { pedidoId, otroNombre -> navController.navigate(Routes.chat(pedidoId, otroNombre)) }
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
            NotificacionesScreen(
                userId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() },
                onAbrirPedido = { pedidoId ->
                    navController.navigate(Routes.pedidoDetalle(pedidoId))
                },
                onAbrirChat = { pedidoId, otroNombre ->
                    navController.navigate(Routes.chat(pedidoId, otroNombre))
                }
            )
        }

        composable(Routes.PUBLICAR) {
            PublicarScreen(
                userId = usuarioActual?.uid ?: "",
                nombreVendedor = usuarioActual?.nombre ?: "",
                ubicacionVendedor = usuarioActual?.ubicacionDescripcion ?: "",
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Routes.MIS_PUBLICACIONES) {
            MisPublicacionesScreen(
                vendedorId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() },
                onEditar = { producto ->
                    navController.navigate(Routes.editarProducto(producto.id))
                }
            )
        }

        composable(Routes.CHATS) {
            ChatsScreen(
                userId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() },
                onAbrirChat = { pedidoId, otroNombre ->
                    navController.navigate(Routes.chat(pedidoId, otroNombre))
                }
            )
        }

        composable(
            route = Routes.TIENDA,
            arguments = listOf(navArgument("vendedorId") { type = NavType.StringType })
        ) { backStack ->
            val vendedorId = backStack.arguments?.getString("vendedorId") ?: ""
            TiendaScreen(
                vendedorId = vendedorId,
                userId = usuarioActual?.uid ?: "",
                onVolver = { navController.popBackStack() },
                onVerPerfil = { uid -> navController.navigate(Routes.perfil(uid)) },
                onCarrito = { navController.navigate(Routes.CARRITO) }
            )
        }

        composable(
            route = Routes.PERFIL,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStack ->
            val uid = backStack.arguments?.getString("uid") ?: ""
            PerfilUsuarioScreen(
                uid = uid,
                onVolver = { navController.popBackStack() },
                onVerTienda = { vendedorId -> navController.navigate(Routes.tienda(vendedorId)) }
            )
        }
   

        composable(
            route = Routes.EDITAR_PRODUCTO,
            arguments = listOf(navArgument("productoId") { type = NavType.StringType })
        ) { backStack ->
            val productoId = backStack.arguments?.getString("productoId") ?: ""
            var productoEditar by remember { mutableStateOf<com.jaz.myapplicationcampuseats.model.Producto?>(null) }
            LaunchedEffect(productoId) {
                com.jaz.myapplicationcampuseats.repository.ProductoRepository
                    .obtenerProductosDelVendedor(usuarioActual?.uid ?: "") { lista ->
                        productoEditar = lista.firstOrNull { it.id == productoId }
                    }
            }
            if (productoEditar != null) {
                PublicarScreen(
                    userId = usuarioActual?.uid ?: "",
                    nombreVendedor = usuarioActual?.nombre ?: "",
                    ubicacionVendedor = usuarioActual?.ubicacionDescripcion ?: "",
                    onVolver = { navController.popBackStack() },
                    productoExistente = productoEditar
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(DarkBg),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = GreenBtn) }
            }
        }
    }
}