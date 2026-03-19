package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaz.myapplicationcampuseats.model.MensajeChat

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()

    // Los mensajes viven bajo pedidos/{pedidoId}/chat
    private fun chatRef(pedidoId: String) = db
        .collection("pedidos")
        .document(pedidoId)
        .collection("chat")

    fun enviarMensaje(
        pedidoId: String,
        mensaje: MensajeChat,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val doc = chatRef(pedidoId).document()
        doc.set(mensaje.copy(id = doc.id, pedidoId = pedidoId))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun escucharMensajes(
        pedidoId: String,
        onUpdate: (List<MensajeChat>) -> Unit
    ): ListenerRegistration {
        return chatRef(pedidoId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val mensajes = snapshot?.documents?.mapNotNull {
                    it.toObject(MensajeChat::class.java)
                } ?: emptyList()
                onUpdate(mensajes)
            }
    }
}
