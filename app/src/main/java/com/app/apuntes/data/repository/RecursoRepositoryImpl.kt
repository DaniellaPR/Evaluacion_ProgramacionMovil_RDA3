package com.app.apuntes.data.repository

import com.app.apuntes.data.mapper.toDomain
import com.app.apuntes.data.remote.retrofit.ApiService
import com.app.apuntes.domain.model.Recurso
import com.app.apuntes.domain.repository.RecursoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class RecursoRepositoryImpl(
    private val apiService: ApiService
) : RecursoRepository {

    override suspend fun obtenerRecursosPorMaterias(
        nombresMaterias: List<String>
    ): List<Recurso> = withContext(Dispatchers.IO) {

        if (nombresMaterias.isEmpty()) return@withContext emptyList()

        // Búsquedas paralelas: una corrutina por materia
        val resultados = nombresMaterias.map { materia ->
            async {
                buscarArticulosPorMateria(materia)
            }
        }.awaitAll()

        // Combinar, eliminar duplicados por id y limitar a 15 totales
        resultados
            .flatten()
            .distinctBy { it.id }
            .take(15)
    }

    private suspend fun buscarArticulosPorMateria(materia: String): List<Recurso> {
        return try {
            // Pedimos 5 resultados para filtrar los más relevantes
            val response = apiService.buscarArticulos(query = materia, limit = 5)
            if (response.isSuccessful) {
                val pages = response.body()?.query?.pages?.values ?: emptyList()

                // Normalizador para quitar acentos y convertir a minúsculas
                fun String.normalize(): String {
                    val map = mapOf(
                        'á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u',
                        'Á' to 'a', 'É' to 'e', 'Í' to 'i', 'Ó' to 'o', 'Ú' to 'u',
                        'ñ' to 'n', 'Ñ' to 'n'
                    )
                    return this.map { map[it] ?: it.lowercaseChar() }.joinToString("")
                }

                val normalizedMateria = materia.normalize()
                val importantWords = normalizedMateria.split(Regex("[^a-z0-9]"))
                    .filter { it.length > 2 }

                val filteredPages = if (importantWords.isNotEmpty()) {
                    val lastWord = importantWords.last()
                    // Reducir la última palabra para soportar plural/singular (ej: "interiores" -> "interior")
                    val stem = lastWord.take(if (lastWord.length >= 5) lastWord.length - 2 else lastWord.length)

                    pages.filter { page ->
                        val normalizedTitle = page.title.normalize()
                        val normalizedExtract = (page.extract ?: "").normalize()
                        normalizedTitle.contains(stem) || normalizedExtract.contains(stem)
                    }
                } else {
                    pages
                }

                filteredPages
                    .filter { it.title.isNotBlank() }
                    .map { it.toDomain(materia) }
                    .take(3) // nos quedamos con un máximo de 3 por materia
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
