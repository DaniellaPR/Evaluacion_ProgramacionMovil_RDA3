package com.app.apuntes.data.repository

import com.app.apuntes.data.local.room.dao.MateriaDao
import com.app.apuntes.data.mapper.toDomain
import com.app.apuntes.data.mapper.toEntity
import com.app.apuntes.domain.model.Materia
import com.app.apuntes.domain.repository.MateriaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MateriaRepositoryImpl(
    private val materiaDao: MateriaDao
) : MateriaRepository {

    override fun obtenerMaterias(): Flow<List<Materia>> =
        materiaDao.obtenerTodas().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun guardarMateria(materia: Materia) {
        materiaDao.insertar(materia.toEntity())
    }

    override suspend fun eliminarMateria(id: Long) {
        val entity = materiaDao.obtenerPorId(id) ?: return
        materiaDao.eliminar(entity)
    }
}