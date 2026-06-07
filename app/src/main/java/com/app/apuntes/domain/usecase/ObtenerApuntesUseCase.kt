package com.app.apuntes.domain.usecase

import com.app.apuntes.domain.model.Apunte
import com.app.apuntes.domain.repository.ApunteRepository
import kotlinx.coroutines.flow.Flow

class ObtenerApuntesUseCase(private val repository: ApunteRepository) {
    operator fun invoke(materiaId: Long): Flow<List<Apunte>> =
        repository.obtenerApuntesPorMateria(materiaId)
}