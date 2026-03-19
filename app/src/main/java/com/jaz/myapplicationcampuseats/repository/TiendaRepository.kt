package com.jaz.myapplicationcampuseats.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaz.myapplicationcampuseats.model.Producto
import com.jaz.myapplicationcampuseats.model.Tienda
import com.jaz.myapplicationcampuseats.model.Usuario

object TiendaRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Obtiene todas las tiendas activas (vendedores que tienen al menos 1 producto disponible).
     * Agrupa productos por vendedor y calcula métricas.
     */
    fun obtenerTiendasDisponibles(onResult: (List<Tienda>) -> Unit) {
        db.collection("productos")
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val productos = snapshot.documents
                    .mapNotNull { it.toObject(Producto::class.java) }

                // Agrupar por vendedor
                val porVendedor = productos.groupBy { it.vendedorId }

                // Obtener info de cada vendedor y construir Tienda
                val tiendas = mutableListOf<Tienda>()
                val vendedorIds = porVendedor.keys.toList()
                if (vendedorIds.isEmpty()) { onResult(emptyList()); return@addOnSuccessListener }

                var procesados = 0
                vendedorIds.forEach { vendedorId ->
                    db.collection("usuarios").document(vendedorId).get()
                        .addOnSuccessListener { userDoc ->
                            val usuario = userDoc.toObject(Usuario::class.java)
                            val prods = porVendedor[vendedorId] ?: emptyList()
                            val ratingProm = if (prods.isNotEmpty())
                                prods.map { it.rating }.average() else 0.0
                            val numResenas = prods.sumOf { it.numResenas }

                            tiendas.add(
                                Tienda(
                                    vendedorId = vendedorId,
                                    nombreVendedor = usuario?.nombre ?: prods.firstOrNull()?.nombreVendedor ?: "",
                                    fotoPerfil = usuario?.fotoPerfil ?: "",
                                    ubicacionDescripcion = usuario?.ubicacionDescripcion ?: "",
                                    preferenciaEntrega = usuario?.preferenciaEntrega ?: "cliente_recoge",
                                    ratingPromedio = ratingProm,
                                    numProductos = prods.size,
                                    numResenas = numResenas,
                                    tiempoPromedioEntregaMin = 0, // se calcula aparte si se quiere
                                    productos = prods.sortedByDescending { it.numResenas * it.rating }
                                )
                            )
                            procesados++
                            if (procesados == vendedorIds.size) {
                                onResult(tiendas.sortedByDescending { it.ratingPromedio })
                            }
                        }
                        .addOnFailureListener {
                            procesados++
                            if (procesados == vendedorIds.size) {
                                onResult(tiendas.sortedByDescending { it.ratingPromedio })
                            }
                        }
                }
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    /**
     * Obtiene la tienda de un vendedor específico con sus productos.
     */
    fun obtenerTiendaDeVendedor(vendedorId: String, onResult: (Tienda?) -> Unit) {
        db.collection("productos")
            .whereEqualTo("vendedorId", vendedorId)
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val productos = snapshot.documents
                    .mapNotNull { it.toObject(Producto::class.java) }
                    .sortedByDescending { it.numResenas * it.rating }

                db.collection("usuarios").document(vendedorId).get()
                    .addOnSuccessListener { userDoc ->
                        val usuario = userDoc.toObject(Usuario::class.java)
                        val ratingProm = if (productos.isNotEmpty())
                            productos.map { it.rating }.average() else 0.0

                        onResult(
                            Tienda(
                                vendedorId = vendedorId,
                                nombreVendedor = usuario?.nombre ?: "",
                                fotoPerfil = usuario?.fotoPerfil ?: "",
                                ubicacionDescripcion = usuario?.ubicacionDescripcion ?: "",
                                preferenciaEntrega = usuario?.preferenciaEntrega ?: "cliente_recoge",
                                ratingPromedio = ratingProm,
                                numProductos = productos.size,
                                numResenas = productos.sumOf { it.numResenas },
                                productos = productos
                            )
                        )
                    }
                    .addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener { onResult(null) }
    }
}
