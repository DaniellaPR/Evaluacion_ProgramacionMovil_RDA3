package com.app.apuntes.data.repository

import com.app.apuntes.data.local.room.dao.ApunteDao
import com.app.apuntes.data.mapper.toDomain
import com.app.apuntes.data.mapper.toEntity
import com.app.apuntes.domain.model.Apunte
import com.app.apuntes.domain.repository.ApunteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ApunteRepositoryImpl(
    private val apunteDao: ApunteDao
) : ApunteRepository {

    override fun obtenerApuntesPorMateria(materiaId: Long): Flow<List<Apunte>> =
        apunteDao.obtenerPorMateria(materiaId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun guardarApunte(apunte: Apunte) {
        apunteDao.insertar(apunte.toEntity())
    }

    override suspend fun eliminarApunte(id: Long) {
        apunteDao.eliminarPorId(id)
    }

    override suspend fun obtenerApuntePorId(id: Long): Apunte? =
        apunteDao.obtenerPorId(id)?.toDomain()
}