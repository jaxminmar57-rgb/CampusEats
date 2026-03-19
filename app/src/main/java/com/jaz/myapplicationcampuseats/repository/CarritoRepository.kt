package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.ItemCarrito

object CarritoRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun carritoRef(userId: String) = db
        .collection("usuarios")
        .document(userId)
        .collection("carrito")

    fun agregarProducto(userId: String, item: ItemCarrito) {
        val ref = carritoRef(userId)
        // Si ya existe el producto, aumenta cantidad
        ref.whereEqualTo("productoId", item.productoId).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    val cantidadActual = doc.getLong("cantidad")?.toInt() ?: 1
                    ref.document(doc.id).update("cantidad", cantidadActual + 1)
                } else {
                    val doc = ref.document()
                    doc.set(item.copy(id = doc.id))
                }
            }
    }

    fun obtenerCarrito(userId: String, onResult: (List<ItemCarrito>) -> Unit) {
        carritoRef(userId).get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(ItemCarrito::class.java) })
            }
    }

    fun eliminarItem(userId: String, itemId: String) {
        carritoRef(userId).document(itemId).delete()
    }

    fun actualizarCantidad(userId: String, itemId: String, cantidad: Int) {
        if (cantidad <= 0) {
            eliminarItem(userId, itemId)
        } else {
            carritoRef(userId).document(itemId).update("cantidad", cantidad)
        }
    }

    fun vaciarCarrito(userId: String) {
        carritoRef(userId).get().addOnSuccessListener { result ->
            result.documents.forEach { it.reference.delete() }
        }
    }
}
