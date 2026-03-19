package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jaz.myapplicationcampuseats.model.Producto

object ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productosRef = db.collection("productos")

    fun publicarProducto(
        producto: Producto,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val doc = productosRef.document()
        val productoConId = producto.copy(id = doc.id)
        doc.set(productoConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerProductosPorCategoria(
        categoria: String,
        onResult: (List<Producto>) -> Unit
    ) {
        productosRef
            .whereEqualTo("categoria", categoria)
            .whereEqualTo("disponible", true)
            .orderBy("rating", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Producto::class.java) })
            }
    }

    fun obtenerTodosDisponibles(onResult: (List<Producto>) -> Unit) {
        productosRef
            .whereEqualTo("disponible", true)
            .orderBy("rating", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Producto::class.java) })
            }
    }

    fun buscarProductos(query: String, onResult: (List<Producto>) -> Unit) {
        // Firestore no tiene búsqueda full-text nativa;
        // buscamos por nombre con rango de prefijo (startAt / endAt)
        productosRef
            .whereEqualTo("disponible", true)
            .orderBy("nombre")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Producto::class.java) })
            }
    }

    fun obtenerProductosDelVendedor(
        vendedorId: String,
        onResult: (List<Producto>) -> Unit
    ) {
        productosRef
            .whereEqualTo("vendedorId", vendedorId)
            .get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Producto::class.java) })
            }
    }

    fun eliminarProducto(productoId: String, onSuccess: () -> Unit = {}) {
        productosRef.document(productoId).delete().addOnSuccessListener { onSuccess() }
    }

    fun toggleDisponibilidad(productoId: String, disponible: Boolean, onSuccess: () -> Unit = {}) {
        productosRef.document(productoId)
            .update("disponible", disponible)
            .addOnSuccessListener { onSuccess() }
    }
}
