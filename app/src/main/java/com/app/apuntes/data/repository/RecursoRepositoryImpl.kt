package com.app.apuntes.data.repository

import com.app.apuntes.data.mapper.toDomain
import com.app.apuntes.data.remote.retrofit.ApiService
import com.app.apuntes.domain.model.Recurso
import com.app.apuntes.domain.repository.RecursoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecursoRepositoryImpl(
    private val apiService: ApiService
) : RecursoRepository {

    override suspend fun obtenerRecursos(): List<Recurso> = withContext(Dispatchers.IO) {
        val response = apiService.obtenerRecursos()
        if (response.isSuccessful) {
            response.body()?.map { it.toDomain() } ?: emptyList()
        } else {
            throw Exception("Error al cargar recursos: ${response.message()}")
        }
    }

    override suspend fun obtenerRecursoPorId(id: Int): Recurso? = withContext(Dispatchers.IO) {
        val response = apiService.obtenerRecursoPorId(id)
        if (response.isSuccessful) {
            response.body()?.toDomain()
        } else {
            null
        }
    }
}
