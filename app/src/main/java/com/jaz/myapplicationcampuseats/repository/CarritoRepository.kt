package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.ItemCarrito

object CarritoRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun carritoRef(userId: String) = db
        .collection("usuarios")
        .document(userId)
        .collection("carrito")

    /**
     * Agrega producto al carrito.
     * Si ya existe, incrementa cantidad.
     * Si hay items de otro vendedor, llama a onConflictoVendedor
     * para que la UI pregunte al usuario si desea vaciar y agregar.
     */
    fun agregarProducto(
        userId: String,
        item: ItemCarrito,
        onSuccess: () -> Unit = {},
        onConflictoVendedor: (vendedorActual: String) -> Unit = {}
    ) {
        val ref = carritoRef(userId)
        ref.get().addOnSuccessListener { snapshot ->
            val itemsExistentes = snapshot.documents.mapNotNull {
                it.toObject(ItemCarrito::class.java)
            }

            // Validar: si hay items de otro vendedor, notificar conflicto
            val vendedorExistente = itemsExistentes.firstOrNull()?.vendedorId
            if (vendedorExistente != null && vendedorExistente != item.vendedorId) {
                onConflictoVendedor(
                    itemsExistentes.firstOrNull()?.nombreVendedor ?: vendedorExistente
                )
                return@addOnSuccessListener
            }

            // Buscar si el producto ya está en el carrito
            val existente = itemsExistentes.firstOrNull { it.productoId == item.productoId }
            if (existente != null) {
                ref.document(existente.id)
                    .update("cantidad", existente.cantidad + 1)
                    .addOnSuccessListener { onSuccess() }
            } else {
                val doc = ref.document()
                doc.set(item.copy(id = doc.id))
                    .addOnSuccessListener { onSuccess() }
            }
        }
    }

    /**
     * Vacía el carrito y luego agrega el nuevo item (para resolver conflicto de vendedor).
     */
    fun vaciarYAgregar(userId: String, item: ItemCarrito, onSuccess: () -> Unit = {}) {
        vaciarCarrito(userId)
        val ref = carritoRef(userId)
        val doc = ref.document()
        doc.set(item.copy(id = doc.id)).addOnSuccessListener { onSuccess() }
    }

    fun obtenerCarrito(userId: String, onResult: (List<ItemCarrito>) -> Unit) {
        carritoRef(userId).get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(ItemCarrito::class.java) })
            }
            .addOnFailureListener { onResult(emptyList()) }
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
