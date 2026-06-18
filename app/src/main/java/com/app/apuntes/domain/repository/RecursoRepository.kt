package com.app.apuntes.domain.repository

import com.app.apuntes.domain.model.Recurso

interface RecursoRepository {
    /**
     * Busca libros en Open Library para cada materia de la lista.
     * Primero intenta en español; si no encuentra resultados, hace fallback sin filtro de idioma.
     */
    suspend fun obtenerRecursosPorMaterias(nombresMaterias: List<String>): List<Recurso>
}
