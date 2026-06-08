package com.app.apuntes.domain.repository

import com.app.apuntes.domain.model.Recurso

interface RecursoRepository {
    suspend fun obtenerRecursos(): List<Recurso>
    suspend fun obtenerRecursoPorId(id: Int): Recurso?
}
