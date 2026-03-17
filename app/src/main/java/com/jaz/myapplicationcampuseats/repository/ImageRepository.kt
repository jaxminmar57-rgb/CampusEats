package com.jaz.myapplicationcampuseats.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object ImageRepository {

    private val storage = FirebaseStorage.getInstance()

    fun subirImagenProducto(
        uri: Uri,
        productoId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val ref = storage.reference
            .child("productos/$productoId.jpg")

        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Error subiendo imagen")
                }
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
            }
            .addOnFailureListener {
                onError(it)
            }
    }
}