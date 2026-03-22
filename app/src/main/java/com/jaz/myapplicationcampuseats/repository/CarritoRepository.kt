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
     * Agrega producto al carrito. Permite múltiples vendedores.
     * Si el producto ya existe, incrementa cantidad.
     * Bloquea si el vendedorId == userId (compra propia).
     */
    fun agregarProducto(
        userId: String,
        item: ItemCarrito,
        onSuccess: () -> Unit = {},
        onBloqueado: (String) -> Unit = {}
    ) {
        // Bloquear compra propia
        if (item.vendedorId == userId) {
            onBloqueado("No puedes comprar tus propios productos")
            return
        }

        val ref = carritoRef(userId)
        ref.get().addOnSuccessListener { snapshot ->
            val itemsExistentes = snapshot.documents.mapNotNull {
                it.toObject(ItemCarrito::class.java)
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

    fun eliminarItemsDeVendedor(userId: String, vendedorId: String, onDone: () -> Unit = {}) {
        carritoRef(userId).whereEqualTo("vendedorId", vendedorId).get()
            .addOnSuccessListener { snap ->
                snap.documents.forEach { it.reference.delete() }
                onDone()
            }
    }

    fun actualizarCantidad(userId: String, itemId: String, cantidad: Int) {
        if (cantidad <= 0) eliminarItem(userId, itemId)
        else carritoRef(userId).document(itemId).update("cantidad", cantidad)
    }

    fun vaciarCarrito(userId: String) {
        carritoRef(userId).get().addOnSuccessListener { result ->
            result.documents.forEach { it.reference.delete() }
        }
    }
}
