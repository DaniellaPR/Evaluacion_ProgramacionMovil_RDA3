# Student Management & Academic Assistant App (Android)

Aplicación móvil nativa para Android enfocada en la gestión académica de estudiantes universitarios. Permite administrar materias, horarios de clase, apuntes, recursos educativos y escaneo/gestión de contenido visual.

El proyecto fue desarrollado como examen práctico final para la materia de Programación Móvil en la Pontificia Universidad Católica del Ecuador (PUCE).

---

## Arquitectura y Principios de Diseño

El proyecto sigue los principios de **Clean Architecture** junto con el patrón de arquitectura **MVVM (Model-View-ViewModel)** recomendado por Google para Android.

### Capas del Sistema:
* **Presentation Layer:** Desarrollada íntegramente con **Jetpack Compose** (UI 100% declarativa) y navegación tipada.
* **Domain Layer:** Contiene el modelo de dominio puro, casos de uso (`ObtenerMateriasUseCase`, `ObtenerApuntesUseCase`, etc.) e interfaces de repositorios. Libre de dependencias del framework de Android.
* **Data Layer:** Implementación de repositorios, fuentes de datos locales con **Room** y remotas con **Retrofit**.

---

## Características Principales

* **Dashboard Unificado:** Vista principal con acceso rápido a las materias matriculadas y recursos educativos recomendados.
* **Gestión de Materias y Horarios:** Registro de asignaturas, docentes y asignación de bloques horarios por día y aula.
* **Módulo de Apuntes:** Creación, edición y consulta detallada de notas asociadas a cada materia.
* **Integración de Cámara / Scanner / QR:** Captura y procesamiento visual para escaneo de apontes y lectura de códigos QR.
* **Consumo de Recursos Educativos:** Integración con API remota vía cliente REST (Retrofit).
* **Persistencia Local:** Almacenamiento fuera de línea completo y reactivo mediante Room.

---

## Stack Tecnológico

* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose
* **Arquitectura:** Clean Architecture + MVVM
* **Inyección de Dependencias / Proveedor de BD:** Singleton Thread-Safe (`DatabaseProvider`)
* **Base de Datos Local:** Room Database 2.6.1 (con DAOs orientados a Flow y KSP 2.0.0)
* **Conectividad / Red:** Retrofit 2.9.0 + Kotlinx Serialization
* **Manejo de Estado y Concurrencia:** Kotlin Coroutines, StateFlow, SharedFlow
* **Navegación:** Type-Safe Jetpack Compose Navigation (`@Serializable` routes)

---

## Modelo de Base de Datos (Room)

La base de datos local relaciona las entidades principales del dominio educativo mediante referencias de clave foránea (`materiaId`):

```
materias (1) ──────── (N) apuntes
materias (1) ──────── (N) horarios
```

### Tabla: `materias`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| id | LONG | PK autoincremental | Identificador único de la materia |
| nombre | TEXT | NOT NULL | Nombre de la asignatura |
| docente | TEXT | NULLABLE | Nombre del docente |
| horario | TEXT | NULLABLE | Descripción rápida del horario |

### Tabla: `horarios`
| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| id | LONG | PK autoincremental | Identificador único del registro de horario |
| materiaId | LONG | NOT NULL (FK) | Referencia a la materia correspondiente |
| dia | TEXT | NOT NULL | Día de la semana |
| horaInicio | TEXT | NOT NULL | Hora de inicio de clase |
| horaFin | TEXT | NOT NULL | Hora de fin de clase |
| aula | TEXT | NULLABLE | Aula o taller asignado |

---

## Resiliencia de Estado e Integración Reactiva

### Manejo de Ciclo de Vida y Estado UI
Se implementó `ViewModel` con `StateFlow` expuesto de forma inmutable (`val uiState: StateFlow<MateriasUiState> = _uiState`). De esta manera, el estado de la pantalla sobrevive a cambios de configuración como la rotación del dispositivo. Los componentes `@Composable` se suscriben mediante `collectAsState()`, garantizando la consistencia de la UI.

### Flujo Reactivo de Datos
`DatabaseProvider` asegura una instancia única e inmutable de la base de datos aplicando el patrón Singleton Thread-Safe (`@Volatile` + `synchronized`).

Los repositorios concretos (`MateriaRepositoryImpl`, `ApunteRepositoryImpl`) exponen flujos `Flow` de Room y utilizan mappers (`toDomain` / `toEntity`) para desacoplar la base de datos de los modelos del dominio. La recolección continua con `viewModelScope` permite actualización automática e instantánea en componentes como `LazyColumn` en las vistas `DashboardScreen` y `ApuntesScreen`.

---

## Estructura de Commits y Colaboración

El desarrollo se organizó bajo convenciones de Conventional Commits:

* `build:` Configuración inicial de dependencias (`libs.versions.toml`), KSP, Room, Retrofit y serialización.
* `feat:` Implementación de esquemas Room, DAOs con Flow y serialización de rutas.
* `feat:` Navegación tipada con rutas `@Serializable` (Dashboard, Apuntes, Detalle, Horario, Perfil, Scanner, QR).
* `feat:` Formulario de creación de materias/horarios y validación de campos.
* `feat:` Cliente HTTP con Retrofit para consumo de repositorios remotos.

---

## Créditos y Equipo

Proyecto realizado para la Pontificia Universidad Católica del Ecuador (PUCE) - Facultad de Hábitat, Infraestructura y Creatividad.

**Docente:** Ing. Juan Francisco Chafla Altamirano  
**Integrantes:**
* Daniela Pozo
* Israel Hernández
* Yanick Maila
* Melany Analuisa
* Rodrigo Lucano
