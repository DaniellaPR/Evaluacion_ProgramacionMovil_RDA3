package com.app.apuntes.data.repository

import com.app.apuntes.data.local.room.dao.HorarioDao
import com.app.apuntes.data.mapper.toDomain
import com.app.apuntes.data.mapper.toEntity
import com.app.apuntes.domain.model.Horario
import com.app.apuntes.domain.repository.HorarioRepository
import kotlinx.coroutines.flow.first

class HorarioRepositoryImpl(
    private val horarioDao: HorarioDao
) : HorarioRepository {

    override suspend fun obtenerHorarios(): List<Horario> {
        return horarioDao.obtenerTodos().first().map { it.toDomain() }
    }

    override suspend fun obtenerHorarioPorMateria(materiaId: Long): List<Horario> {
        return horarioDao.obtenerPorMateria(materiaId).first().map { it.toDomain() }
    }

    override suspend fun guardarHorario(horario: Horario) {
        horarioDao.insertar(horario.toEntity())
    }

    override suspend fun eliminarHorario(id: Long) {
        horarioDao.eliminarPorId(id)
    }
}
