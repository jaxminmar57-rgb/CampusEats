package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.ItemCarrito
import com.jaz.myapplicationcampuseats.model.Pedido

/**
 * Maneja la creación y consulta de pedidos en Firebase
 */
object PedidoRepository {

    private val db = FirebaseFirestore.getInstance()

    private val pedidosRef = db.collection("pedidos")

    /**
     * Crear pedido a partir del carrito
     */
    fun crearPedido(
        items: List<ItemCarrito>,
        onSuccess: () -> Unit
    ) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (items.isEmpty()) return

        val vendedorId = items.first().vendedorId

        val total = items.sumOf { it.precio * it.cantidad }

        val pedidoDoc = pedidosRef.document()

        val pedido = Pedido(

            id = pedidoDoc.id,

            usuarioId = userId,

            vendedorId = vendedorId,

            estado = "pendiente",

            total = total

        )

        pedidoDoc.set(pedido).addOnSuccessListener {

            onSuccess()

        }

    }

    /**
     * Obtener pedidos del usuario
     */
    fun obtenerPedidosUsuario(onResult: (List<Pedido>) -> Unit) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        pedidosRef
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { result ->

                val lista = result.documents.mapNotNull {

                    it.toObject(Pedido::class.java)

                }

                onResult(lista)

            }

    }

}