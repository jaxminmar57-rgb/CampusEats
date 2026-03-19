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

    fun guardarNotificacion(notificacion: NotificacionApp) {
        if (notificacion.uid.isEmpty()) return
        val doc = notifRef(notificacion.uid).document()
        doc.set(notificacion.copy(id = doc.id))
    }

    /**
     * Sin orderBy ni limit — evita requerir índice compuesto en Firestore.
     * Ordenamos en memoria y limitamos a 50.
     */
    fun escucharNotificaciones(
        uid: String,
        onUpdate: (List<NotificacionApp>) -> Unit
    ): ListenerRegistration {
        return notifRef(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot.documents
                    .mapNotNull { it.toObject(NotificacionApp::class.java) }
                    .sortedByDescending { it.timestamp }
                    .take(50)
                onUpdate(lista)
            }
    }

    fun marcarLeida(uid: String, notifId: String) {
        notifRef(uid).document(notifId).update("leida", true)
    }

    fun marcarTodasLeidas(uid: String) {
        notifRef(uid).get().addOnSuccessListener { result ->
            val batch = db.batch()
            result.documents
                .filter { it.getBoolean("leida") == false }
                .forEach { batch.update(it.reference, "leida", true) }
            batch.commit()
        }
    }

    fun eliminarNotificacion(uid: String, notifId: String) {
        notifRef(uid).document(notifId).delete()
    }
}
