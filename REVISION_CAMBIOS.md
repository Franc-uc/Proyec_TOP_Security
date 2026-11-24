# Revisión del proyecto TOP Security

## Cambios realizados en esta versión

1. Se amplió Room para persistir incidencias y acciones de trazabilidad, además de la sesión.
2. Se agregó una migración desde el esquema previo de Room (versión 1) al esquema nuevo (versión 2).
3. Se agregó `TopSecurityRepository` para separar el acceso a datos de la lógica de presentación.
4. El ViewModel ahora observa los flujos de Room; registrar, actualizar y cerrar un caso persiste los cambios.
5. `MainActivity` obtiene el ViewModel mediante `ViewModelProvider` y usa una instancia compartida de Room.
6. Se movió el ViewModel a un paquete llamado `viewmodel` para reflejar mejor la arquitectura.
7. El formulario permite elegir categoría y muestra validaciones e indicaciones para usar datos ficticios.
8. Se agregaron selectores de estado y prioridad en la gestión, y se muestra responsable/fecha/prioridad en el detalle.
9. Los casos precargados utilizan sectores simulados en vez de ubicaciones reales concretas.
10. Se agregó una imagen al inicio y mensajes que advierten que los datos deben ser ficticios.
11. Se quitó la dependencia de Navigation Compose porque la navegación existente se realiza mediante el estado de pantalla y la dependencia no se estaba usando.
12. Se agregó este README con arquitectura, límites y plan de pruebas.
13. Se rediseñó la interfaz de usuario incorporando una paleta de colores institucionales (`NavyDark`, `NavyMid`, `CyanGlow`, `EmeraldOk` y `AmberWarn`) y tarjetas de resumen estadístico en la vista principal.
14. Se integró el emblema de **TOP Security** (`logo_top_security`) en la cabecera del inicio de sesión y como ícono adaptativo del APK (`ic_launcher`).
15. Se implementó una pantalla de carga animada (`PantallaCargaSesion`) con transición `Crossfade` al iniciar sesión.
16. Se añadieron transiciones horizontales suaves (`AnimatedContent`) al navegar entre la lista de incidencias, el formulario de reporte y la vista de detalle.
17. Se incorporó una ventana emergente de confirmación (`AlertDialog`) antes de cerrar la sesión activa.
18. Se renombró el paquete `data` a `repository` para mantener una estructura de carpetas más ordenada y coherente con la arquitectura del proyecto.

## Revisión pendiente en el equipo

- [x] Abrir el proyecto en Android Studio y sincronizar Gradle.
- [x] Compilar con JDK 17 y Android SDK Platform 34; comprobar que el APK se genere.
- [ ] Revisar el diseño contra el MockUp de Trello.
- [ ] Confirmar en el repositorio oficial el nombre/URL y la fecha límite de los commits.
- [ ] Ejecutar la aplicación en un teléfono real no emulado.

## Limitaciones técnicas conocidas

La captura de cámara se usa como recurso nativo de demostración, pero la imagen no se persiste; los casos y sus acciones sí se guardan mediante Room. No se implementa servidor, GPS, notificaciones push ni autenticación segura.


## [24/11/2026] Integración CRUD completo con MockAPI y sincronización en Room

### Resumen
Se implementó la conexión completa entre la aplicación y la API REST externa (MockAPI) mediante Retrofit, manteniendo la sincronización reactiva con la base de datos local SQLite/Room (`top_security_db`).

### Cambios realizados

* **Capa de Red y Endpoints (Retrofit)**
    * **Incidencias:** Integración de las operaciones `GET /incidencias`, `GET /incidencias/{id}`, `POST /incidencias`, `PUT /incidencias/{id}` y `DELETE /incidencias/{id}`.
    * **Sectores:** Integración de `GET /sectores` y `POST /sectores` para la gestión dinámica de ubicaciones referenciales.
    * **Permisos (`AndroidManifest.xml`):** Habilitación de `android.permission.INTERNET` para las peticiones de red.

* **Persistencia Local y Repositorio (`AppDatabase.kt`, `TopSecurityRepository.kt`)**
    * Se agregó la consulta `eliminarPorId(id: String)` en `IncidenciaDao`, aprovechando `ForeignKey.CASCADE` para eliminar automáticamente su historial asociado en `tabla_trazabilidad`.
    * Se actualizó `eliminarIncidenciaRemota` en `TopSecurityRepository` para ejecutar el borrado tanto en Room como en MockAPI.

* **Interfaz de Usuario (`Pantallas.kt`)**
    * **`FormularioRegistroScreen`:** Incorporación de selector desplegable conectado a los sectores de la API y botón para registrar nuevos sectores directamente en el servidor.
    * **`DetalleIncidenciaScreen`:** Incorporación de botón y diálogo de confirmación para eliminar registros de forma sincronizada (servidor + local).
    * **`MainAppScreen`:** Conexión de los flujos de estado (`sectores`), recarga individual al abrir el detalle (`refrescarIncidencia`) y acción de eliminación (`eliminarIncidencia`).