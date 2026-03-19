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

    /**
     * Obtiene productos por categoría.
     * Evitamos orderBy + whereEqualTo combinados (requieren índice compuesto).
     * Ordenamos por rating en memoria.
     */
    fun obtenerProductosPorCategoria(
        categoria: String,
        onResult: (List<Producto>) -> Unit
    ) {
        productosRef
            .whereEqualTo("categoria", categoria)
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents
                    .mapNotNull { it.toObject(Producto::class.java) }
                    .sortedByDescending { it.rating }
                onResult(lista)
            }
            .addOnFailureListener {
                // Si falla, devolver lista vacía en vez de no llamar al callback
                onResult(emptyList())
            }
    }

    /**
     * Obtiene todos los productos disponibles.
     * IMPORTANTE: No usamos orderBy("rating") + whereEqualTo() juntos porque
     * Firestore requiere un índice compuesto que puede no existir.
     * Ordenamos en memoria.
     */
    fun obtenerTodosDisponibles(onResult: (List<Producto>) -> Unit) {
        productosRef
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents
                    .mapNotNull { it.toObject(Producto::class.java) }
                    .sortedByDescending { it.rating }
                onResult(lista)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * Escucha en tiempo real todos los productos disponibles.
     * Úsalo en HomeScreen para que se actualicen automáticamente.
     */
    fun escucharProductosDisponibles(
        onUpdate: (List<Producto>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return productosRef
            .whereEqualTo("disponible", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot.documents
                    .mapNotNull { it.toObject(Producto::class.java) }
                    .sortedByDescending { it.rating }
                onUpdate(lista)
            }
    }

    /**
     * Escucha en tiempo real productos de una categoría.
     */
    fun escucharProductosPorCategoria(
        categoria: String,
        onUpdate: (List<Producto>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return productosRef
            .whereEqualTo("categoria", categoria)
            .whereEqualTo("disponible", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot.documents
                    .mapNotNull { it.toObject(Producto::class.java) }
                    .sortedByDescending { it.rating }
                onUpdate(lista)
            }
    }

    fun buscarProductos(query: String, onResult: (List<Producto>) -> Unit) {
        // Búsqueda simple por prefijo en nombre (sin índice adicional)
        productosRef
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents
                    .mapNotNull { it.toObject(Producto::class.java) }
                    .filter {
                        it.nombre.contains(query, ignoreCase = true) ||
                        it.categoria.contains(query, ignoreCase = true) ||
                        it.nombreVendedor.contains(query, ignoreCase = true)
                    }
                    .sortedByDescending { it.rating }
                onResult(lista)
            }
            .addOnFailureListener { onResult(emptyList()) }
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
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun escucharProductosDelVendedor(
        vendedorId: String,
        onUpdate: (List<Producto>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return productosRef
            .whereEqualTo("vendedorId", vendedorId)
            .addSnapshotListener { snapshot, _ ->
                val lista = snapshot?.documents
                    ?.mapNotNull { it.toObject(Producto::class.java) }
                    ?: emptyList()
                onUpdate(lista)
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
