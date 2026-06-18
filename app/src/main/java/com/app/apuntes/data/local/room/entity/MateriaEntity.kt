package com.app.apuntes.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materias")
data class MateriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val docente: String? = null,
    val descripcion: String? = null  // antes: horario
)
