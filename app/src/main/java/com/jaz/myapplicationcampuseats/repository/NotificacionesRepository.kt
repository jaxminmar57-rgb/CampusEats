package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jaz.myapplicationcampuseats.model.NotificacionApp

object NotificacionesRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun notifRef(uid: String) = db
        .collection("usuarios")
        .document(uid)
        .collection("notificaciones")

    /**
     * Guarda una notificación en Firestore para el usuario destinatario.
     * Se llama desde FcmRepository junto con el push.
     */
    fun guardarNotificacion(notificacion: NotificacionApp) {
        if (notificacion.uid.isEmpty()) return
        val doc = notifRef(notificacion.uid).document()
        doc.set(notificacion.copy(id = doc.id))
    }

    /** Escucha notificaciones en tiempo real. */
    fun escucharNotificaciones(
        uid: String,
        onUpdate: (List<NotificacionApp>) -> Unit
    ): ListenerRegistration {
        return notifRef(uid)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                val lista = snapshot?.documents
                    ?.mapNotNull { it.toObject(NotificacionApp::class.java) }
                    ?: emptyList()
                onUpdate(lista)
            }
    }

    fun marcarLeida(uid: String, notifId: String) {
        notifRef(uid).document(notifId).update("leida", true)
    }

    fun marcarTodasLeidas(uid: String) {
        notifRef(uid)
            .whereEqualTo("leida", false)
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()
                result.documents.forEach { batch.update(it.reference, "leida", true) }
                batch.commit()
            }
    }

    fun eliminarNotificacion(uid: String, notifId: String) {
        notifRef(uid).document(notifId).delete()
    }
}
