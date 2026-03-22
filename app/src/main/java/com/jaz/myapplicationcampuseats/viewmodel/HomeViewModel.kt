package com.jaz.myapplicationcampuseats.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.repository.ChatRepository
import com.jaz.myapplicationcampuseats.repository.NotificacionesRepository
import com.jaz.myapplicationcampuseats.repository.PedidoRepository
import com.jaz.myapplicationcampuseats.repository.ProductoRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository
import com.jaz.myapplicationcampuseats.ui.reproducirSonidoMensaje

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // ── Estado observable ─────────────────────────────────────────────────────
    var productos by mutableStateOf<List<Producto>>(emptyList())
        private set
    var cargando by mutableStateOf(true)
        private set
    // Set de vendedores abiertos (para filtrar en la UI)
    var vendedoresAbiertos by mutableStateOf<Set<String>>(emptySet())
        private set
    var pedidosActivosCliente by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var pedidosActivosVendedor by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var carritoCount by mutableStateOf(0)
        private set
    var notifNoLeidas by mutableStateOf(0)
        private set
    var negocioAbierto by mutableStateOf(false)
        private set

    val mensajesNuevosMapa = mutableStateMapOf<String, Int>()
    val ultimoLeido = mutableStateMapOf<String, Long>()
    val mensajesNuevosTotal: Int
        get() = mensajesNuevosMapa.values.sum()

    val pedidosActivos: List<Pedido>
        get() = (pedidosActivosCliente + pedidosActivosVendedor).distinctBy { it.id }

    // ── Listeners activos ─────────────────────────────────────────────────────
    private val listeners = mutableListOf<ListenerRegistration>()
    private var chatListeners = mutableListOf<ListenerRegistration>()
    private var uid = ""

    // ── Iniciar listeners ─────────────────────────────────────────────────────
    fun iniciarListeners(userId: String) {
        if (userId == uid && listeners.isNotEmpty()) return // Ya están activos
        detenerListeners()
        uid = userId

        // Productos disponibles (global, no depende de uid)
        listeners += ProductoRepository.escucharProductosDisponibles { lista ->
            productos = lista
            cargando = false
        }

        // Escuchar qué vendedores tienen negocio abierto
        listeners += FirebaseFirestore.getInstance().collection("usuarios")
            .whereEqualTo("negocioAbierto", true)
            .addSnapshotListener { snap, _ ->
                vendedoresAbiertos = snap?.documents?.mapNotNull { it.id }?.toSet() ?: emptySet()
            }

        if (uid.isEmpty()) return

        // Pedidos como cliente
        listeners += PedidoRepository.escucharPedidosCliente(uid) { lista ->
            pedidosActivosCliente = lista.filter { it.estado !in listOf("completado", "cancelado") }
            actualizarChatListeners()
        }

        // Pedidos como vendedor
        listeners += PedidoRepository.escucharPedidosVendedor(uid) { lista ->
            pedidosActivosVendedor = lista.filter { it.estado !in listOf("completado", "cancelado") }
            actualizarChatListeners()
        }

        // Carrito en tiempo real
        val carritoRef = FirebaseFirestore.getInstance()
            .collection("usuarios").document(uid).collection("carrito")
        listeners += carritoRef.addSnapshotListener { snap, _ ->
            carritoCount = snap?.documents?.sumOf {
                (it.getLong("cantidad") ?: 1L).toInt()
            } ?: 0
        }

        // Notificaciones no leídas
        listeners += NotificacionesRepository.escucharNotificaciones(uid) { lista ->
            notifNoLeidas = lista.count { !it.leida && it.tipo != "chat" }
        }

        // Estado de mi negocio (reactivo)
        listeners += FirebaseFirestore.getInstance().collection("usuarios").document(uid)
            .addSnapshotListener { snap, _ ->
                negocioAbierto = snap?.getBoolean("negocioAbierto") ?: false
            }
    }

    // ── Chat listeners (se recrean cuando cambian pedidos activos) ────────────
    private fun actualizarChatListeners() {
        chatListeners.forEach { it.remove() }
        chatListeners.clear()

        val context = getApplication<Application>().applicationContext

        pedidosActivos.forEach { pedido ->
            chatListeners += ChatRepository.escucharMensajes(pedido.id) { mensajes ->
                val leido = ultimoLeido[pedido.id] ?: 0L
                val nuevos = mensajes.count { it.autorId != uid && it.timestamp > leido }
                val anterior = mensajesNuevosMapa[pedido.id] ?: 0
                if (nuevos > anterior) reproducirSonidoMensaje(context)
                mensajesNuevosMapa[pedido.id] = nuevos
            }
        }
    }

    // ── Acciones ──────────────────────────────────────────────────────────────
    fun actualizarUbicacion(descripcion: String, onSuccess: () -> Unit = {}) {
        if (uid.isEmpty()) return
        UsuarioRepository.actualizarUbicacion(uid, descripcion, onSuccess)
    }

    fun toggleNegocioAbierto(nuevoEstado: Boolean, onSuccess: () -> Unit = {}) {
        if (uid.isEmpty()) return
        negocioAbierto = nuevoEstado // Actualización inmediata en la UI
        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
            .update("negocioAbierto", nuevoEstado)
            .addOnSuccessListener { onSuccess() }
    }

    // ── Limpieza ──────────────────────────────────────────────────────────────
    private fun detenerListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
        chatListeners.forEach { it.remove() }
        chatListeners.clear()
    }

    override fun onCleared() {
        super.onCleared()
        detenerListeners()
    }
}
