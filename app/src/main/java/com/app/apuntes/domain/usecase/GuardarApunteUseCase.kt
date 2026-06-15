package com.app.apuntes.domain.usecase

import com.app.apuntes.domain.model.Apunte
import com.app.apuntes.domain.repository.ApunteRepository

class GuardarApunteUseCase(private val repository: ApunteRepository) {
    suspend operator fun invoke(apunte: Apunte) {
        repository.guardarApunte(apunte)
    }
}
