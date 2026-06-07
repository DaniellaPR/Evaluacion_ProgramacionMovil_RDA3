package com.app.apuntes.domain.usecase

import com.app.apuntes.domain.model.Materia
import com.app.apuntes.domain.repository.MateriaRepository
import kotlinx.coroutines.flow.Flow

class ObtenerMateriasUseCase(private val repository: MateriaRepository) {
    operator fun invoke(): Flow<List<Materia>> = repository.obtenerMaterias()
}