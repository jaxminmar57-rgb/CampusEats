package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Usuario

object UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usuariosRef = db.collection("usuarios")

    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    fun registrar(
        correo: String,
        password: String,
        nombre: String,
        edad: String,
        onSuccess: (Usuario) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val usuario = Usuario(
                    uid = uid,
                    nombre = nombre,
                    correo = correo,
                    edad = edad
                )
                usuariosRef.document(uid).set(usuario)
                    .addOnSuccessListener { onSuccess(usuario) }
                    .addOnFailureListener { onError(it.message ?: "Error al guardar perfil") }
            }
            .addOnFailureListener { onError(it.message ?: "Error al registrar") }
    }

    fun login(
        correo: String,
        password: String,
        onSuccess: (Usuario) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(correo, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                obtenerUsuario(uid, onSuccess) { onError("Error al obtener perfil") }
            }
            .addOnFailureListener { onError(it.message ?: "Correo o contraseña incorrectos") }
    }

    fun obtenerUsuario(
        uid: String,
        onSuccess: (Usuario) -> Unit,
        onError: () -> Unit = {}
    ) {
        usuariosRef.document(uid).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                if (usuario != null) onSuccess(usuario) else onError()
            }
            .addOnFailureListener { onError() }
    }

    fun obtenerUsuarioActual(
        onSuccess: (Usuario) -> Unit,
        onError: () -> Unit = {}
    ) {
        val uid = currentUserId
        if (uid.isEmpty()) { onError(); return }
        obtenerUsuario(uid, onSuccess, onError)
    }

    fun actualizarPerfil(
        uid: String,
        campos: Map<String, Any>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        usuariosRef.document(uid).update(campos)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun hayUsuarioLogueado(): Boolean = auth.currentUser != null
}
