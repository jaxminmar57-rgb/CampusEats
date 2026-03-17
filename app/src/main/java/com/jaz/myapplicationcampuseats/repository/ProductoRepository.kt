package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Producto

/**
 * Repositorio encargado de manejar
 * todas las operaciones de productos
 * en Firebase Firestore.
 */
object ProductoRepository {

    // Instancia de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Referencia a la colección productos
    private val productosRef = db.collection("productos")

    /**
     * Guarda un nuevo producto en Firebase
     */
    fun publicarProducto(producto: Producto, onSuccess: () -> Unit, onError: (Exception) -> Unit) {

        val doc = productosRef.document()

        val productoConId = producto.copy(id = doc.id)

        doc.set(productoConId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /**
     * Obtiene todos los productos de una categoría
     */
    fun obtenerProductosPorCategoria(
        categoria: String,
        onResult: (List<Producto>) -> Unit
    ) {

        productosRef
            .whereEqualTo("categoria", categoria)
            .get()
            .addOnSuccessListener { result ->

                val lista = result.documents.mapNotNull {
                    it.toObject(Producto::class.java)
                }

                onResult(lista)
            }
    }

    /**
     * Obtiene todos los productos
     */
    fun obtenerTodosLosProductos(onResult: (List<Producto>) -> Unit) {

        productosRef
            .get()
            .addOnSuccessListener { result ->

                val lista = result.documents.mapNotNull {
                    it.toObject(Producto::class.java)
                }

                onResult(lista)
            }
    }
}