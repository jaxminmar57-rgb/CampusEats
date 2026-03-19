package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Usuario

object UsuarioRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("usuarios")

    fun registrar(
        nombre: String, correo: String, password: String,
        onSuccess: (Usuario) -> Unit, onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener { result ->
                val uid     = result.user?.uid ?: return@addOnSuccessListener
                val usuario = Usuario(uid = uid, nombre = nombre, correo = correo)
                usersRef.document(uid).set(usuario)
                    .addOnSuccessListener { onSuccess(usuario) }
                    .addOnFailureListener { onError(it.message ?: "Error") }
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun login(correo: String, password: String, onSuccess: (Usuario) -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(correo, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                obtenerUsuario(uid, onSuccess = onSuccess, onError = { onError(it) })
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun obtenerUsuario(uid: String, onSuccess: (Usuario) -> Unit, onError: (String) -> Unit = {}) {
        usersRef.document(uid).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                if (usuario != null) onSuccess(usuario) else onError("Usuario no encontrado")
            }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun obtenerUsuarioActual(onSuccess: (Usuario) -> Unit, onError: (String) -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        obtenerUsuario(uid, onSuccess, onError)
    }

    fun actualizarPerfil(
        uid: String, campos: Map<String, Any>,
        onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}
    ) {
        usersRef.document(uid).update(campos)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    /** Actualiza rápidamente la ubicación del vendedor (descripción de texto). */
    fun actualizarUbicacion(
        uid: String,
        descripcion: String,
        onSuccess: () -> Unit = {}
    ) {
        usersRef.document(uid)
            .update("ubicacionDescripcion", descripcion)
            .addOnSuccessListener { onSuccess() }
    }

    fun cerrarSesion() { auth.signOut() }

    fun hayUsuarioLogueado(): Boolean = auth.currentUser != null
}
