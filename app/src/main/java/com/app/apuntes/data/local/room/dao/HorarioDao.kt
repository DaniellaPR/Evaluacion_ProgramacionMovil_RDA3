package com.app.apuntes.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.apuntes.data.local.room.entity.HorarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HorarioDao {

    @Query("SELECT * FROM horarios ORDER BY dia ASC, horaInicio ASC")
    fun obtenerTodos(): Flow<List<HorarioEntity>>

    @Query("SELECT * FROM horarios WHERE materiaId = :materiaId")
    fun obtenerPorMateria(materiaId: Long): Flow<List<HorarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(horario: HorarioEntity): Long

    @Update
    suspend fun actualizar(horario: HorarioEntity)

    @Delete
    suspend fun eliminar(horario: HorarioEntity)

    @Query("DELETE FROM horarios WHERE id = :id")
    suspend fun eliminarPorId(id: Long)
}
