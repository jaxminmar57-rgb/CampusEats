package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    /**
     * Listener tiempo real para el CLIENTE.
     * Sin orderBy para evitar requerir índice compuesto — ordenamos en memoria.
     */
    fun escucharPedidosCliente(
        clienteId: String,
        onUpdate: (List<Pedido>) -> Unit
    ): ListenerRegistration {
        return pedidosRef
            .whereEqualTo("clienteId", clienteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) { onUpdate(emptyList()); return@addSnapshotListener }
                val lista = snapshot.documents
                    .mapNotNull { it.toObject(Pedido::class.java) }
                    .sortedByDescending { it.fecha }
                onUpdate(lista)
            }
    }

    /**
     * Listener tiempo real para el VENDEDOR.
     * Sin orderBy — ordenamos en memoria.
     */
    fun escucharPedidosVendedor(
        vendedorId: String,
        onUpdate: (List<Pedido>) -> Unit
    ): ListenerRegistration {
        return pedidosRef
            .whereEqualTo("vendedorId", vendedorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) { onUpdate(emptyList()); return@addSnapshotListener }
                val lista = snapshot.documents
                    .mapNotNull { it.toObject(Pedido::class.java) }
                    .sortedByDescending { it.fecha }
                onUpdate(lista)
            }
    }

    /** Escucha un pedido específico en tiempo real. */
    fun escucharPedido(
        pedidoId: String,
        onUpdate: (Pedido?) -> Unit
    ): ListenerRegistration {
        return pedidosRef.document(pedidoId)
            .addSnapshotListener { snapshot, _ ->
                onUpdate(snapshot?.toObject(Pedido::class.java))
            }
    }

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

    fun clienteConfirmaEntrega(pedidoId: String, onSuccess: () -> Unit = {}) {
        pedidosRef.document(pedidoId)
            .update("clienteConfirmoEntrega", true)
            .addOnSuccessListener {
                pedidosRef.document(pedidoId).get().addOnSuccessListener { doc ->
                    val vendedorConfirmo = doc.getBoolean("vendedorConfirmoEntrega") ?: false
                    if (vendedorConfirmo) cambiarEstado(pedidoId, "completado")
                }
                onSuccess()
            }
    }

    fun vendedorConfirmaEntrega(pedidoId: String, onSuccess: () -> Unit = {}) {
        pedidosRef.document(pedidoId)
            .update("vendedorConfirmoEntrega", true)
            .addOnSuccessListener {
                pedidosRef.document(pedidoId).get().addOnSuccessListener { doc ->
                    val clienteConfirmo = doc.getBoolean("clienteConfirmoEntrega") ?: false
                    if (clienteConfirmo) cambiarEstado(pedidoId, "completado")
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
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents
                    .mapNotNull { it.toObject(Pedido::class.java) }
                    .sortedByDescending { it.fecha }
                onResult(lista)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}
