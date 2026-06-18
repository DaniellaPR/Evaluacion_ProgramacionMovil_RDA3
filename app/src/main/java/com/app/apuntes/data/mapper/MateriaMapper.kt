package com.app.apuntes.data.mapper

import com.app.apuntes.data.local.room.entity.MateriaEntity
import com.app.apuntes.domain.model.Materia

fun MateriaEntity.toDomain(): Materia = Materia(
    id = id,
    nombre = nombre,
    docente = docente,
    descripcion = descripcion
)

fun Materia.toEntity(): MateriaEntity = MateriaEntity(
    id = id,
    nombre = nombre,
    docente = docente,
    descripcion = descripcion
)
