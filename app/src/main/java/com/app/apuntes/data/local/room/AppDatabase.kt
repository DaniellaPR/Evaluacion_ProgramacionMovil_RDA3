package com.app.apuntes.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.apuntes.data.local.room.dao.ApunteDao
import com.app.apuntes.data.local.room.dao.HorarioDao
import com.app.apuntes.data.local.room.dao.MateriaDao
import com.app.apuntes.data.local.room.entity.ApunteEntity
import com.app.apuntes.data.local.room.entity.HorarioEntity
import com.app.apuntes.data.local.room.entity.MateriaEntity

@Database(
    entities = [
        ApunteEntity::class,
        MateriaEntity::class,
        HorarioEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apunteDao(): ApunteDao
    abstract fun materiaDao(): MateriaDao
    abstract fun horarioDao(): HorarioDao

    companion object {
        /**
         * Migración 1→2: renombra la columna `horario` a `descripcion` en la tabla materias.
         * Se recrea la tabla para compatibilidad con SQLite < 3.25.0 (Android < API 30).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Crear tabla nueva con el esquema actualizado
                database.execSQL("""
                    CREATE TABLE materias_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        docente TEXT,
                        descripcion TEXT
                    )
                """.trimIndent())
                // 2. Copiar datos de la tabla antigua (horario → descripcion)
                database.execSQL("""
                    INSERT INTO materias_new (id, nombre, docente, descripcion)
                    SELECT id, nombre, docente, horario FROM materias
                """.trimIndent())
                // 3. Eliminar tabla antigua
                database.execSQL("DROP TABLE materias")
                // 4. Renombrar nueva tabla
                database.execSQL("ALTER TABLE materias_new RENAME TO materias")
            }
        }
    }
}
