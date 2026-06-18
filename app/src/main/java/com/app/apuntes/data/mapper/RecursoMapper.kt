package com.app.apuntes.data.mapper

import com.app.apuntes.data.remote.retrofit.dto.WikipediaPageDto
import com.app.apuntes.domain.model.Recurso
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun WikipediaPageDto.toDomain(materia: String): Recurso {
    val encodedTitle = try {
        URLEncoder.encode(title.replace(" ", "_"), StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        title.replace(" ", "_")
    }
    val fullUrl = "https://es.wikipedia.org/wiki/$encodedTitle"
    return Recurso(
        id = pageid.toString(),
        titulo = title,
        autor = "Wikipedia",
        anio = "Gratis",
        materia = materia,
        url = fullUrl,
        temas = "Enciclopedia Libre, Artículo Educativo",
        fraseInicial = extract?.trim()?.ifBlank { null }
    )
}
