package com.app.apuntes.domain.model

data class Recurso(
    val id: String,           // key de Open Library (ej. "/works/OL123W")
    val titulo: String,
    val autor: String,       // "Autor desconocido" si no viene en la respuesta
    val anio: String,        // "—" si no viene en la respuesta
    val materia: String,     // nombre de la materia que originó la búsqueda
    val url: String,         // enlace completo para visualizar/leer en la web
    val temas: String? = null, // temas/etiquetas del libro
    val fraseInicial: String? = null // primera frase del libro para dar más contexto
)
