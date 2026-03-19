package com.jaz.myapplicationcampuseats

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.jaz.myapplicationcampuseats.navigation.AppNavigation
import com.jaz.myapplicationcampuseats.repository.FcmRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository
import com.jaz.myapplicationcampuseats.service.NotificationHelper

class MainActivity : ComponentActivity() {

    // Parámetros de deep link desde notificación
    var deepLinkDestino: String? = null
    var deepLinkPedidoId: String? = null
    var deepLinkOtroNombre: String? = null

    // Launcher para pedir permiso de notificaciones en Android 13+
    private val permisosLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            // Permiso concedido — registrar token FCM
            if (UsuarioRepository.hayUsuarioLogueado()) {
                FcmRepository.registrarTokenActual()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear canales de notificación
        NotificationHelper.crearCanales(this)

        // Leer deep link de la notificación si la app fue abierta desde una
        intent?.let {
            deepLinkDestino   = it.getStringExtra(NotificationHelper.EXTRA_DESTINO)
            deepLinkPedidoId  = it.getStringExtra(NotificationHelper.EXTRA_PEDIDO_ID)
            deepLinkOtroNombre = it.getStringExtra(NotificationHelper.EXTRA_OTRO_NOMBRE)
        }

        // Pedir permiso de notificaciones (Android 13+)
        pedirPermisoNotificaciones()

        // Registrar token FCM si hay sesión activa
        if (UsuarioRepository.hayUsuarioLogueado()) {
            FcmRepository.registrarTokenActual()
        }

        setContent {
            AppNavigation(
                deepLinkDestino    = deepLinkDestino,
                deepLinkPedidoId   = deepLinkPedidoId,
                deepLinkOtroNombre = deepLinkOtroNombre
            )
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permisosLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
