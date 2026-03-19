package com.jaz.myapplicationcampuseats.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.jaz.myapplicationcampuseats.model.Pedido
import com.jaz.myapplicationcampuseats.repository.PedidoRepository
import com.jaz.myapplicationcampuseats.repository.ResenaRepository
import com.jaz.myapplicationcampuseats.repository.UsuarioRepository

class PedidoDetalleViewModel : ViewModel() {

    var pedido by mutableStateOf<Pedido?>(null)
        private set
    var cargando by mutableStateOf(true)
        private set
    var procesando by mutableStateOf(false)
        private set
    var yaCalificaron by mutableStateOf(false)
        private set
    var ubicacionVendedor by mutableStateOf("")
        private set

    private var listener: ListenerRegistration? = null
    private var pedidoIdActual = ""

    fun iniciarListener(pedidoId: String, uid: String) {
        if (pedidoId == pedidoIdActual && listener != null) return
        listener?.remove()
        pedidoIdActual = pedidoId

        listener = PedidoRepository.escucharPedido(pedidoId) { p ->
            pedido = p
            cargando = false

            // Cargar ubicación del vendedor si el cliente recoge
            if (p != null && p.preferenciaEntrega == "cliente_recoge") {
                UsuarioRepository.obtenerUsuario(p.vendedorId, onSuccess = { v ->
                    ubicacionVendedor = v.ubicacionDescripcion
                })
            }

            // Verificar si ya calificaron
            if (p?.estado == "completado" && uid.isNotEmpty()) {
                ResenaRepository.yaCalificoPedido(pedidoId, uid) { yaCalificaron = it }
            }
        }
    }

    fun cambiarEstado(nuevoEstado: String, onDone: () -> Unit = {}) {
        procesando = true
        PedidoRepository.cambiarEstado(pedidoIdActual, nuevoEstado) {
            procesando = false; onDone()
        }
    }

    fun clienteConfirma(onDone: () -> Unit = {}) {
        procesando = true
        PedidoRepository.clienteConfirmaEntrega(pedidoIdActual) {
            procesando = false; onDone()
        }
    }

    fun vendedorConfirma(onDone: () -> Unit = {}) {
        procesando = true
        PedidoRepository.vendedorConfirmaEntrega(pedidoIdActual) {
            procesando = false; onDone()
        }
    }

    fun marcarCalificado() { yaCalificaron = true }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
