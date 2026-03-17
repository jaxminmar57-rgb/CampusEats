package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.ItemCarrito

object CarritoRepository {

    private val db = FirebaseFirestore.getInstance()

    private val userId
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val carritoRef
        get() = db.collection("usuarios")
            .document(userId)
            .collection("carrito")

    fun agregarProducto(item: ItemCarrito) {

        val doc = carritoRef.document()

        val itemConId = item.copy(id = doc.id)

        doc.set(itemConId)

    }

    fun obtenerCarrito(onResult: (List<ItemCarrito>) -> Unit) {

        carritoRef
            .get()
            .addOnSuccessListener { result ->

                val lista = result.documents.mapNotNull {

                    it.toObject(ItemCarrito::class.java)

                }

                onResult(lista)

            }

    }

    fun eliminarItem(id: String) {

        carritoRef.document(id).delete()

    }

}