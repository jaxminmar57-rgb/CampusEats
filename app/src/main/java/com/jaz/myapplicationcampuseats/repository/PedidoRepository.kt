package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaz.myapplicationcampuseats.model.ItemCarrito
import com.jaz.myapplicationcampuseats.model.Pedido

object PedidoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val pedidosRef = db.collection("pedidos")

    fun crearPedido(
        clienteId: String,
        nombreCliente: String,
        items: List<ItemCarrito>,
        metodoPago: String,
        notas: String,
        preferenciaEntrega: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (items.isEmpty()) { onError("El carrito está vacío"); return }

        val vendedorId = items.first().vendedorId
        val nombreVendedor = items.first().nombreVendedor
        val total = items.sumOf { it.precio * it.cantidad }

        val doc = pedidosRef.document()

        // Convertir items a mapas para Firestore
        val itemsMapa = items.map { item ->
            mapOf(
                "productoId" to item.productoId,
                "nombre" to item.nombre,
                "precio" to item.precio,
                "cantidad" to item.cantidad,
                "imagenUrl" to item.imagenUrl
            )
        }

        val pedido = Pedido(
            id = doc.id,
            clienteId = clienteId,
            vendedorId = vendedorId,
            nombreCliente = nombreCliente,
            nombreVendedor = nombreVendedor,
            items = itemsMapa,
            estado = "pendiente",
            total = total,
            metodoPago = metodoPago,
            preferenciaEntrega = preferenciaEntrega,
            notas = notas
        )

        doc.set(pedido)
            .addOnSuccessListener { onSuccess(doc.id) }
            .addOnFailureListener { onError(it.message ?: "Error al crear pedido") }
    }

    // Para el CLIENTE: observa sus pedidos en tiempo real
    fun escucharPedidosCliente(
        clienteId: String,
        onUpdate: (List<Pedido>) -> Unit
    ): ListenerRegistration {
        return pedidosRef
            .whereEqualTo("clienteId", clienteId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val lista = snapshot?.documents?.mapNotNull {
                    it.toObject(Pedido::class.java)
                } ?: emptyList()
                onUpdate(lista)
            }
    }

    // Para el VENDEDOR: observa los pedidos que le llegaron en tiempo real
    fun escucharPedidosVendedor(
        vendedorId: String,
        onUpdate: (List<Pedido>) -> Unit
    ): ListenerRegistration {
        return pedidosRef
            .whereEqualTo("vendedorId", vendedorId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val lista = snapshot?.documents?.mapNotNull {
                    it.toObject(Pedido::class.java)
                } ?: emptyList()
                onUpdate(lista)
            }
    }

    // Escuchar un pedido específico en tiempo real
    fun escucharPedido(
        pedidoId: String,
        onUpdate: (Pedido?) -> Unit
    ): ListenerRegistration {
        return pedidosRef.document(pedidoId)
            .addSnapshotListener { snapshot, _ ->
                onUpdate(snapshot?.toObject(Pedido::class.java))
            }
    }

    // Vendedor: cambiar estado del pedido
    fun cambiarEstado(
        pedidoId: String,
        nuevoEstado: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        pedidosRef.document(pedidoId)
            .update("estado", nuevoEstado)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    // Cliente confirma entrega (libera pago)
    fun clienteConfirmaEntrega(pedidoId: String, onSuccess: () -> Unit = {}) {
        pedidosRef.document(pedidoId)
            .update("clienteConfirmoEntrega", true)
            .addOnSuccessListener {
                // Si el vendedor ya confirmó, marcar como completado
                pedidosRef.document(pedidoId).get().addOnSuccessListener { doc ->
                    val vendedorConfirmo = doc.getBoolean("vendedorConfirmoEntrega") ?: false
                    if (vendedorConfirmo) {
                        cambiarEstado(pedidoId, "completado")
                    }
                }
                onSuccess()
            }
    }

    // Vendedor confirma entrega
    fun vendedorConfirmaEntrega(pedidoId: String, onSuccess: () -> Unit = {}) {
        pedidosRef.document(pedidoId)
            .update("vendedorConfirmoEntrega", true)
            .addOnSuccessListener {
                pedidosRef.document(pedidoId).get().addOnSuccessListener { doc ->
                    val clienteConfirmo = doc.getBoolean("clienteConfirmoEntrega") ?: false
                    if (clienteConfirmo) {
                        cambiarEstado(pedidoId, "completado")
                    }
                }
                onSuccess()
            }
    }

    fun obtenerPedidosUsuario(
        userId: String,
        onResult: (List<Pedido>) -> Unit
    ) {
        pedidosRef
            .whereEqualTo("clienteId", userId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Pedido::class.java) })
            }
    }
}
