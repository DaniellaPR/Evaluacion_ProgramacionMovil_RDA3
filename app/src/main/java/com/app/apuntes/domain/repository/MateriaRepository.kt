package com.app.apuntes.domain.repository

import com.app.apuntes.domain.model.Materia
import kotlinx.coroutines.flow.Flow

interface MateriaRepository {
    fun obtenerMaterias(): Flow<List<Materia>>
    suspend fun guardarMateria(materia: Materia)
    suspend fun eliminarMateria(id: Long)
}