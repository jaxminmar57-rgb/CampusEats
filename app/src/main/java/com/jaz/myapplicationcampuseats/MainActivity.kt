package com.jaz.myapplicationcampuseats

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.jaz.myapplicationcampuseats.navigation.AppNavigation
import com.jaz.myapplicationcampuseats.repository.FcmRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository
import com.jaz.myapplicationcampuseats.service.NotificationHelper
import com.jaz.myapplicationcampuseats.ui.theme.CampusEatsTheme

class MainActivity : ComponentActivity() {

    // Deep link state — observable para Compose
    var deepLinkDestino = mutableStateOf<String?>(null)
    var deepLinkPedidoId = mutableStateOf<String?>(null)
    var deepLinkOtroNombre = mutableStateOf<String?>(null)

    private val permisosLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido && UsuarioRepository.hayUsuarioLogueado()) {
            FcmRepository.registrarTokenActual()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.crearCanales(this)
        leerDeepLink(intent)
        pedirPermisoNotificaciones()

        if (UsuarioRepository.hayUsuarioLogueado()) {
            FcmRepository.registrarTokenActual()
        }

        setContent {
            CampusEatsTheme {
                AppNavigation(
                    deepLinkDestino    = deepLinkDestino.value,
                    deepLinkPedidoId   = deepLinkPedidoId.value,
                    deepLinkOtroNombre = deepLinkOtroNombre.value
                )
            }
        }
    }

    // Manejar notificaciones cuando la app ya está abierta (singleTop)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerDeepLink(intent)
    }

    private fun leerDeepLink(intent: Intent?) {
        intent?.let {
            deepLinkDestino.value    = it.getStringExtra(NotificationHelper.EXTRA_DESTINO)
            deepLinkPedidoId.value   = it.getStringExtra(NotificationHelper.EXTRA_PEDIDO_ID)
            deepLinkOtroNombre.value = it.getStringExtra(NotificationHelper.EXTRA_OTRO_NOMBRE)
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
