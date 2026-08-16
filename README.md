# SafePass 2026 - Sistema de Gestión de Check-in

Repositorio oficial para el examen práctico de **Programación Móvil (RDA-1)**. Esta aplicación demuestra la implementación de un sistema de gestión de asistentes, aplicando principios de programación segura en **Kotlin** y diseño de interfaces con **Jetpack Compose**.

---

## Descripción del Proyecto
**SafePass 2026** es una solución móvil diseñada para el registro y validación de asistentes a eventos. El desarrollo se centró en la integridad de los datos y la gestión de estados reactivos, asegurando una ejecución estable mediante el uso de operadores de seguridad de Kotlin.

---

## Tecnologías y Conceptos Implementados

El desarrollo integra prácticas de programación moderna bajo los siguientes estándares:

* **Arquitectura de Datos:** Uso de `data class` inmutables para la representación de asistentes.
* **Gestión de Estados:** Implementación de `sealed class` (`RegistroState`) para un control exhaustivo de estados mediante la expresión `when`.
* **Programación Funcional:** 
  * **Scope Functions:** `let`, `apply`, `run` para manipulación contextual de objetos.
  * **Extension Functions:** Para mejorar la legibilidad y modularidad del código.
  * **Higher-order functions:** Para lógica de negocio desacoplada.
* **Seguridad:** Validación de entradas con `toIntOrNull()` y `toDoubleOrNull()`, combinados con operadores de seguridad de Kotlin (`?.`, `?:`).
* **UI:** Interfaz desarrollada en **Jetpack Compose** mediante `Scaffold` y `Column`.

---

## Especificaciones Técnicas
* **Lenguaje:** Kotlin
* **IDE:** Android Studio
* **JDK:** Java 21
* **Target SDK:** API 36 (Android 16)
* **UI Framework:** Jetpack Compose

---

## Estructura del Informe
El desarrollo incluyó un informe académico enfocado en:
1. **Arquitectura:** Flujo de datos y control de estados.
2. **Seguridad de Interfaz:** Análisis del uso de `sealed class` para prevenir estados inconsistentes en la UI.
3. **Evidencia de Pruebas:** Capturas de los estados *Idle*, *Success* y *Error* en emulador (API 36).

---

## Autores
* **Materia:** Programación Móvil (RDA-1)
* **Integrantes del Grupo:**
  * Yanick Maila
  * Daniela Pozo
  * Edwin Israel
  * Melany Analuisa
  * Rodrigo Lucano
