package com.app.apuntes.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Dashboard

@Serializable
data object Horario

@Serializable
data object Scanner

@Serializable
data object ResultadoOCR

@Serializable
data object EditarApunteOCR

@Serializable
data object QR

@Serializable
data object Perfil

@Serializable
data class Apuntes(val materiaId: Long)

@Serializable
data class DetalleApunte(val apunteId: Long)

@Serializable
data object CrearMateria

@Serializable
data class CrearApunte(val materiaId: Long)
