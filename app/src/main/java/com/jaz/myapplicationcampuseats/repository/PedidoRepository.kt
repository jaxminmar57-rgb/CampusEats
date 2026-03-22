package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaz.myapplicationcampuseats.model.ItemCarrito
import com.jaz.myapplicationcampuseats.model.Pedido

object PedidoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val pedidosRef = db.collection("pedidos")

    fun crearPedido(
        clienteId: String, nombreCliente: String, items: List<ItemCarrito>,
        metodoPago: String, notas: String, preferenciaEntrega: String,
        onSuccess: (String) -> Unit, onError: (String) -> Unit
    ) {
        if (items.isEmpty()) { onError("El carrito está vacío"); return }
        val vendedorId = items.first().vendedorId
        val nombreVendedor = items.first().nombreVendedor
        val total = items.sumOf { it.precio * it.cantidad }
        val doc = pedidosRef.document()
        val itemsMapa = items.map { mapOf("productoId" to it.productoId, "nombre" to it.nombre, "precio" to it.precio, "cantidad" to it.cantidad, "imagenUrl" to it.imagenUrl) }
        val pedido = Pedido(id = doc.id, clienteId = clienteId, vendedorId = vendedorId, nombreCliente = nombreCliente, nombreVendedor = nombreVendedor, items = itemsMapa, estado = "pendiente", total = total, metodoPago = metodoPago, preferenciaEntrega = preferenciaEntrega, notas = notas)
        doc.set(pedido).addOnSuccessListener {
            FcmRepository.notificarNuevoPedido(vendedorId, nombreCliente, doc.id, total)
            onSuccess(doc.id)
        }.addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun escucharPedidosCliente(clienteId: String, onUpdate: (List<Pedido>) -> Unit): ListenerRegistration {
        return pedidosRef.whereEqualTo("clienteId", clienteId).addSnapshotListener { snapshot, _ ->
            onUpdate(snapshot?.documents?.mapNotNull { it.toObject(Pedido::class.java) }?.sortedByDescending { it.fecha } ?: emptyList())
        }
    }

    fun escucharPedidosVendedor(vendedorId: String, onUpdate: (List<Pedido>) -> Unit): ListenerRegistration {
        return pedidosRef.whereEqualTo("vendedorId", vendedorId).addSnapshotListener { snapshot, _ ->
            onUpdate(snapshot?.documents?.mapNotNull { it.toObject(Pedido::class.java) }?.sortedByDescending { it.fecha } ?: emptyList())
        }
    }

    fun escucharPedido(pedidoId: String, onUpdate: (Pedido?) -> Unit): ListenerRegistration {
        return pedidosRef.document(pedidoId).addSnapshotListener { snapshot, _ ->
            onUpdate(snapshot?.toObject(Pedido::class.java))
        }
    }

    fun cambiarEstado(pedidoId: String, nuevoEstado: String, canceladoPorUid: String = "", onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        pedidosRef.document(pedidoId).update("estado", nuevoEstado).addOnSuccessListener {
            pedidosRef.document(pedidoId).get().addOnSuccessListener { doc ->
                val p = doc.toObject(Pedido::class.java) ?: return@addOnSuccessListener
                // Determinar a quién notificar (la OTRA parte)
                val destinatarioId = if (canceladoPorUid == p.clienteId) p.vendedorId else p.clienteId
                val nombreRemitente = if (canceladoPorUid == p.clienteId) p.nombreCliente else p.nombreVendedor
                FcmRepository.notificarCambioEstado(destinatarioId, nombreRemitente, pedidoId, nuevoEstado)
            }
            onSuccess()
        }.addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun clienteConfirmaEntrega(pedidoId: String, onSuccess: () -> Unit = {}) {
        pedidosRef.document(pedidoId).update("clienteConfirmoEntrega", true).addOnSuccessListener {
            pedidosRef.document(pedidoId).get().addOnSuccessListener { doc ->
                if (doc.getBoolean("vendedorConfirmoEntrega") == true) {
                    cambiarEstado(pedidoId, "completado")
                    val items = (doc.get("items") as? List<Map<String, Any>>) ?: emptyList()
                    ProductoRepository.descontarStockPorPedido(items)
                }
            }
            onSuccess()
        }
    }

    fun vendedorConfirmaEntrega(pedidoId: String, onSuccess: () -> Unit = {}) {
        pedidosRef.document(pedidoId).update("vendedorConfirmoEntrega", true).addOnSuccessListener {
            pedidosRef.document(pedidoId).get().addOnSuccessListener { doc ->
                if (doc.getBoolean("clienteConfirmoEntrega") == true) {
                    cambiarEstado(pedidoId, "completado")
                    val items = (doc.get("items") as? List<Map<String, Any>>) ?: emptyList()
                    ProductoRepository.descontarStockPorPedido(items)
                }
            }
            onSuccess()
        }
    }

    fun obtenerPedidosUsuario(userId: String, onResult: (List<Pedido>) -> Unit) {
        pedidosRef.whereEqualTo("clienteId", userId).get()
            .addOnSuccessListener { onResult(it.documents.mapNotNull { d -> d.toObject(Pedido::class.java) }.sortedByDescending { p -> p.fecha }) }
            .addOnFailureListener { onResult(emptyList()) }
    }
}
