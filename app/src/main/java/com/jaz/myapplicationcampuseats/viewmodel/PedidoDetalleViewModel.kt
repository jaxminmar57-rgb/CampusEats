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
    private var estadoEsperado: String? = null

    fun iniciarListener(pedidoId: String, uid: String) {
        if (pedidoId == pedidoIdActual && listener != null) return
        listener?.remove()
        pedidoIdActual = pedidoId

        listener = PedidoRepository.escucharPedido(pedidoId) { p ->
            pedido = p
            cargando = false

            // Resetear procesando cuando llega el estado que esperamos del snapshot
            if (estadoEsperado != null) {
                if (p?.estado == estadoEsperado ||
                    p?.clienteConfirmoEntrega == true ||
                    p?.vendedorConfirmoEntrega == true) {
                    procesando = false
                    estadoEsperado = null
                }
            }

            if (p != null && p.preferenciaEntrega == "cliente_recoge") {
                UsuarioRepository.obtenerUsuario(p.vendedorId, onSuccess = { v ->
                    ubicacionVendedor = v.ubicacionDescripcion
                })
            }

            if (p?.estado == "completado" && uid.isNotEmpty()) {
                ResenaRepository.yaCalificoPedido(pedidoId, uid) { yaCalificaron = it }
            }
        }
    }

    fun cambiarEstado(nuevoEstado: String, uid: String = "") {
        procesando = true
        estadoEsperado = nuevoEstado
        PedidoRepository.cambiarEstado(pedidoIdActual, nuevoEstado, canceladoPorUid = uid,
            onError = { procesando = false; estadoEsperado = null }
        )
    }

    fun clienteConfirma() {
        procesando = true
        estadoEsperado = "completado"
        PedidoRepository.clienteConfirmaEntrega(pedidoIdActual) {
            // El snapshot listener se encarga de resetear procesando
        }
    }

    fun vendedorConfirma() {
        procesando = true
        estadoEsperado = "completado"
        PedidoRepository.vendedorConfirmaEntrega(pedidoIdActual) {
            // El snapshot listener se encarga de resetear procesando
        }
    }

    fun marcarCalificado() { yaCalificaron = true }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
