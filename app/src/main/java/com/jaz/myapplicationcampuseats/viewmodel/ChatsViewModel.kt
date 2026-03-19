package com.jaz.myapplicationcampuseats.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.repository.ChatRepository
import com.jaz.myapplicationcampuseats.repository.PedidoRepository
import com.jaz.myapplicationcampuseats.ui.reproducirSonidoMensaje

class ChatsViewModel(application: Application) : AndroidViewModel(application) {

    var pedidosComoCliente by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var pedidosComoVendedor by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var cargando by mutableStateOf(true)
        private set

    val mensajesNuevos = mutableStateMapOf<String, Int>()
    val ultimoLeido = mutableStateMapOf<String, Long>()

    val todosPedidos: List<Pedido>
        get() = (pedidosComoCliente + pedidosComoVendedor).distinctBy { it.id }.sortedByDescending { it.fecha }

    val totalNuevos: Int
        get() = mensajesNuevos.values.sum()

    private val listeners = mutableListOf<ListenerRegistration>()
    private var chatListeners = mutableListOf<ListenerRegistration>()
    private var uid = ""

    fun iniciarListeners(userId: String) {
        if (userId == uid && listeners.isNotEmpty()) return
        detenerListeners()
        uid = userId
        if (uid.isEmpty()) return

        listeners += PedidoRepository.escucharPedidosCliente(uid) { lista ->
            pedidosComoCliente = lista; cargando = false
            actualizarChatListeners()
        }
        listeners += PedidoRepository.escucharPedidosVendedor(uid) { lista ->
            pedidosComoVendedor = lista
            actualizarChatListeners()
        }
    }

    private fun actualizarChatListeners() {
        chatListeners.forEach { it.remove() }
        chatListeners.clear()

        val context = getApplication<Application>().applicationContext
        val activos = todosPedidos.filter { it.estado !in listOf("completado", "cancelado") }

        activos.forEach { pedido ->
            chatListeners += ChatRepository.escucharMensajes(pedido.id) { mensajes ->
                val leido = ultimoLeido[pedido.id] ?: 0L
                val nuevos = mensajes.count { it.autorId != uid && it.timestamp > leido }
                val anterior = mensajesNuevos[pedido.id] ?: 0
                if (nuevos > anterior && anterior >= 0) reproducirSonidoMensaje(context)
                mensajesNuevos[pedido.id] = nuevos
            }
        }
    }

    fun marcarLeido(pedidoId: String) {
        ultimoLeido[pedidoId] = System.currentTimeMillis()
        mensajesNuevos[pedidoId] = 0
    }

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
