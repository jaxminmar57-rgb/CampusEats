package com.jaz.myapplicationcampuseats.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata

object ImageRepository {

    private val storage = FirebaseStorage.getInstance()

    /**
     * Sube imagen de producto a Firebase Storage.
     * Usa putFile directo sin continueWithTask para evitar el bug de sesión 404.
     */
    fun subirImagenProducto(
        uri: Uri,
        productoId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("productos/$productoId.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        ref.putFile(uri, metadata)
            .addOnSuccessListener {
                // Una vez subido, obtener la URL de descarga
                ref.downloadUrl
                    .addOnSuccessListener { url -> onSuccess(url.toString()) }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

    /**
     * Sube foto de perfil a Firebase Storage.
     */
    fun subirFotoPerfil(
        uri: Uri,
        userId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("perfiles/$userId.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()

        ref.putFile(uri, metadata)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { url -> onSuccess(url.toString()) }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }
}
