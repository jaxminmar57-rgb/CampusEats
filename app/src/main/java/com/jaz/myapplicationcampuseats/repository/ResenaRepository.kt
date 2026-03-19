package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Resena

object ResenaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val resenasRef = db.collection("resenas")
    private val usuariosRef = db.collection("usuarios")

    fun enviarResena(
        resena: Resena,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val doc = resenasRef.document()
        doc.set(resena.copy(id = doc.id))
            .addOnSuccessListener {
                // Recalcular rating promedio del destinatario
                recalcularRating(resena.destinatarioId, resena.rolDestinatario)
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    private fun recalcularRating(usuarioId: String, rol: String) {
        resenasRef
            .whereEqualTo("destinatarioId", usuarioId)
            .whereEqualTo("rolDestinatario", rol)
            .get()
            .addOnSuccessListener { result ->
                val resenas = result.documents.mapNotNull { it.toObject(Resena::class.java) }
                if (resenas.isEmpty()) return@addOnSuccessListener
                val promedio = resenas.map { it.estrellas }.average()
                val campoRating = if (rol == "vendedor") "ratingVendedor" else "ratingComprador"
                val campoNum = if (rol == "vendedor") "numResenasVendedor" else "numResenasComprador"
                usuariosRef.document(usuarioId).update(
                    mapOf(campoRating to promedio, campoNum to resenas.size)
                )
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
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { it.toObject(Resena::class.java) })
            }
    }

    // Verificar si el usuario ya calificó en ese pedido (evitar duplicados)
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
