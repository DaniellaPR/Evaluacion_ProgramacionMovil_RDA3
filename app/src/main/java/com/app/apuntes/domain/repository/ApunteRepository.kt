package com.app.apuntes.domain.repository

import com.app.apuntes.domain.model.Apunte
import kotlinx.coroutines.flow.Flow

interface ApunteRepository {
    fun obtenerApuntesPorMateria(materiaId: Long): Flow<List<Apunte>>
    suspend fun guardarApunte(apunte: Apunte)
    suspend fun eliminarApunte(id: Long)
    suspend fun obtenerApuntePorId(id: Long): Apunte?
}