package com.app.apuntes.data.mapper

import com.app.apuntes.data.remote.retrofit.dto.RecursoDto
import com.app.apuntes.domain.model.Recurso

fun RecursoDto.toDomain(): Recurso = Recurso(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    userId = userId
)
