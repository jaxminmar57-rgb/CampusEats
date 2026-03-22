package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.jaz.myapplicationcampuseats.model.MensajeChat

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun chatRef(pedidoId: String) = db
        .collection("pedidos")
        .document(pedidoId)
        .collection("chat")

    fun enviarMensaje(
        pedidoId: String,
        mensaje: MensajeChat,
        destinatarioUid: String = "",
        rolRemitente: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val doc = chatRef(pedidoId).document()
        val data = hashMapOf<String, Any>(
            "id" to doc.id,
            "pedidoId" to pedidoId,
            "autorId" to mensaje.autorId,
            "autorNombre" to mensaje.autorNombre,
            "texto" to mensaje.texto,
            "timestamp" to FieldValue.serverTimestamp()
        )
        doc.set(data)
            .addOnSuccessListener {
                if (destinatarioUid.isNotEmpty() && mensaje.texto.isNotEmpty()) {
                    FcmRepository.notificarMensajeChat(
                        destinatarioUid = destinatarioUid,
                        remitenteNombre = mensaje.autorNombre,
                        pedidoId        = pedidoId,
                        mensaje         = mensaje.texto,
                        rolRemitente    = rolRemitente
                    )
                }
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun escucharMensajes(
        pedidoId: String,
        onUpdate: (List<MensajeChat>) -> Unit
    ): ListenerRegistration {
        return chatRef(pedidoId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                // Leer manualmente para soportar tanto Timestamp como Long
                val mensajes = snapshot?.documents?.mapNotNull { doc ->
                    val ts = try {
                        doc.getTimestamp("timestamp")?.toDate()?.time
                            ?: doc.getLong("timestamp")
                            ?: 0L
                    } catch (_: Exception) {
                        doc.getLong("timestamp") ?: 0L
                    }
                    if (ts == 0L) return@mapNotNull null // Ignorar docs sin timestamp aún
                    MensajeChat(
                        id = doc.getString("id") ?: doc.id,
                        pedidoId = doc.getString("pedidoId") ?: "",
                        autorId = doc.getString("autorId") ?: "",
                        autorNombre = doc.getString("autorNombre") ?: "",
                        texto = doc.getString("texto") ?: "",
                        timestamp = ts
                    )
                } ?: emptyList()
                onUpdate(mensajes)
            }
    }
}
