package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaz.myapplicationcampuseats.model.Producto

object ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productosRef = db.collection("productos")

    fun publicarProducto(producto: Producto, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val doc = productosRef.document()
        doc.set(producto.copy(id = doc.id))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun actualizarProducto(productoId: String, campos: Map<String, Any>, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        productosRef.document(productoId).update(campos)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerProductosPorCategoria(categoria: String, onResult: (List<Producto>) -> Unit) {
        productosRef.whereEqualTo("categoria", categoria).whereEqualTo("disponible", true).get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Producto::class.java) }.sortedByDescending { it.rating })
            }
    }

    fun obtenerTodosDisponibles(onResult: (List<Producto>) -> Unit) {
        productosRef.whereEqualTo("disponible", true).get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Producto::class.java) }.sortedByDescending { it.rating })
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun escucharProductosDisponibles(onUpdate: (List<Producto>) -> Unit): ListenerRegistration {
        return productosRef.whereEqualTo("disponible", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) { onUpdate(emptyList()); return@addSnapshotListener }
                onUpdate(snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }.sortedByDescending { it.rating })
            }
    }

    fun escucharProductosPorCategoria(categoria: String, onUpdate: (List<Producto>) -> Unit): ListenerRegistration {
        return productosRef.whereEqualTo("categoria", categoria).whereEqualTo("disponible", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) { onUpdate(emptyList()); return@addSnapshotListener }
                onUpdate(snapshot.documents.mapNotNull { it.toObject(Producto::class.java) }.sortedByDescending { it.rating })
            }
    }

    fun obtenerProductosDelVendedor(vendedorId: String, onResult: (List<Producto>) -> Unit) {
        productosRef.whereEqualTo("vendedorId", vendedorId).get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Producto::class.java) })
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun escucharProductosDelVendedor(vendedorId: String, onUpdate: (List<Producto>) -> Unit): ListenerRegistration {
        return productosRef.whereEqualTo("vendedorId", vendedorId)
            .addSnapshotListener { snapshot, _ ->
                onUpdate(snapshot?.documents?.mapNotNull { it.toObject(Producto::class.java) } ?: emptyList())
            }
    }

    fun eliminarProducto(productoId: String, onSuccess: () -> Unit = {}) {
        productosRef.document(productoId).delete().addOnSuccessListener { onSuccess() }
    }

    fun toggleDisponibilidad(productoId: String, disponible: Boolean, onSuccess: () -> Unit = {}) {
        productosRef.document(productoId).update("disponible", disponible).addOnSuccessListener { onSuccess() }
    }

    /** Ajusta el stock en +delta o -delta. Si llega a 0, marca como no disponible. */
    fun ajustarStock(productoId: String, delta: Int, onSuccess: () -> Unit = {}) {
        productosRef.document(productoId).get().addOnSuccessListener { doc ->
            val actual = (doc.getLong("cantidadDisponible") ?: -1L).toInt()
            if (actual < 0) { onSuccess(); return@addOnSuccessListener } // sin límite
            val nuevo = maxOf(0, actual + delta)
            val campos: Map<String, Any> = if (nuevo == 0)
                mapOf("cantidadDisponible" to 0, "disponible" to false)
            else
                mapOf("cantidadDisponible" to nuevo)
            productosRef.document(productoId).update(campos).addOnSuccessListener { onSuccess() }
        }
    }

    /** Descuenta stock al completarse un pedido. */
    fun descontarStockPorPedido(items: List<Map<String, Any>>) {
        items.forEach { item ->
            val productoId = item["productoId"] as? String ?: return@forEach
            val cantidad = (item["cantidad"] as? Long)?.toInt() ?: 1
            ajustarStock(productoId, -cantidad)
            // Incrementar ventas totales
            productosRef.document(productoId).update(
                "ventasTotales", com.google.firebase.firestore.FieldValue.increment(cantidad.toLong())
            )
        }
    }
}
