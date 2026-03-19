package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Resena

object ResenaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val resenasRef = db.collection("resenas")
    private val usuariosRef = db.collection("usuarios")
    private val productosRef = db.collection("productos")

    /**
     * Envía una reseña. Si tiene productoId, recalcula el rating de ESE producto.
     * Luego recalcula el rating del vendedor como promedio de todos sus productos.
     */
    fun enviarResena(
        resena: Resena,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val doc = resenasRef.document()
        doc.set(resena.copy(id = doc.id))
            .addOnSuccessListener {
                // Recalcular rating del producto específico
                if (resena.productoId.isNotEmpty()) {
                    recalcularRatingProducto(resena.productoId)
                }
                // Recalcular rating del destinatario (vendedor o comprador)
                recalcularRatingUsuario(resena.destinatarioId, resena.rolDestinatario)
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    /**
     * Envía reseñas para todos los productos de un pedido.
     * Una reseña por producto, todas con la misma calificación y comentario.
     */
    fun enviarResenaPorProductos(
        pedidoId: String,
        autorId: String,
        autorNombre: String,
        destinatarioId: String,
        rolDestinatario: String,
        productoIds: List<String>,
        estrellas: Int,
        comentario: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (productoIds.isEmpty()) {
            // Fallback: reseña sin productoId (para comprador)
            enviarResena(
                Resena(
                    pedidoId = pedidoId, autorId = autorId, autorNombre = autorNombre,
                    destinatarioId = destinatarioId, rolDestinatario = rolDestinatario,
                    estrellas = estrellas, comentario = comentario
                ),
                onSuccess = onSuccess,
                onError = onError
            )
            return
        }

        val batch = db.batch()
        productoIds.forEach { productoId ->
            val doc = resenasRef.document()
            batch.set(doc, Resena(
                id = doc.id, pedidoId = pedidoId, autorId = autorId,
                autorNombre = autorNombre, destinatarioId = destinatarioId,
                rolDestinatario = rolDestinatario, productoId = productoId,
                estrellas = estrellas, comentario = comentario
            ))
        }
        batch.commit()
            .addOnSuccessListener {
                // Recalcular rating de cada producto
                productoIds.forEach { recalcularRatingProducto(it) }
                // Recalcular rating del vendedor
                recalcularRatingUsuario(destinatarioId, rolDestinatario)
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    /**
     * Recalcula el rating de UN producto basado en sus reseñas.
     */
    private fun recalcularRatingProducto(productoId: String) {
        resenasRef
            .whereEqualTo("productoId", productoId)
            .get()
            .addOnSuccessListener { result ->
                val resenas = result.documents.mapNotNull { it.toObject(Resena::class.java) }
                if (resenas.isEmpty()) return@addOnSuccessListener
                val promedio = resenas.map { it.estrellas }.average()
                productosRef.document(productoId).update(
                    mapOf("rating" to promedio, "numResenas" to resenas.size)
                )
            }
    }

    /**
     * Recalcula el rating del usuario vendedor como promedio de TODOS sus productos.
     * Para compradores, promedia sus reseñas directas.
     */
    private fun recalcularRatingUsuario(usuarioId: String, rol: String) {
        if (rol == "vendedor") {
            // Rating vendedor = promedio de ratings de todos sus productos
            productosRef.whereEqualTo("vendedorId", usuarioId).get()
                .addOnSuccessListener { result ->
                    val productos = result.documents.mapNotNull { it.toObject(com.jaz.myapplicationcampuseats.model.Producto::class.java) }
                    val conRating = productos.filter { it.numResenas > 0 }
                    if (conRating.isEmpty()) return@addOnSuccessListener
                    val promedio = conRating.map { it.rating }.average()
                    val totalResenas = conRating.sumOf { it.numResenas }
                    usuariosRef.document(usuarioId).update(
                        mapOf("ratingVendedor" to promedio, "numResenasVendedor" to totalResenas)
                    )
                }
        } else {
            // Rating comprador = promedio de reseñas directas
            resenasRef
                .whereEqualTo("destinatarioId", usuarioId)
                .whereEqualTo("rolDestinatario", "comprador")
                .get()
                .addOnSuccessListener { result ->
                    val resenas = result.documents.mapNotNull { it.toObject(Resena::class.java) }
                    if (resenas.isEmpty()) return@addOnSuccessListener
                    val promedio = resenas.map { it.estrellas }.average()
                    usuariosRef.document(usuarioId).update(
                        mapOf("ratingComprador" to promedio, "numResenasComprador" to resenas.size)
                    )
                }
        }
    }

    fun obtenerResenasDeUsuario(
        usuarioId: String,
        rol: String,
        onResult: (List<Resena>) -> Unit
    ) {
        resenasRef
            .whereEqualTo("destinatarioId", usuarioId)
            .whereEqualTo("rolDestinatario", rol)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents
                    .mapNotNull { it.toObject(Resena::class.java) }
                    .sortedByDescending { it.fecha }
                    .take(20)
                onResult(lista)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun yaCalificoPedido(
        pedidoId: String,
        autorId: String,
        onResult: (Boolean) -> Unit
    ) {
        resenasRef
            .whereEqualTo("pedidoId", pedidoId)
            .whereEqualTo("autorId", autorId)
            .get()
            .addOnSuccessListener { onResult(!it.isEmpty) }
    }
}
