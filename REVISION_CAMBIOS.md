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

## Revisión pendiente en el equipo

- Abrir el proyecto en Android Studio y sincronizar Gradle.
- Compilar con JDK 17 y Android SDK Platform 34; comprobar que el APK se genere.
- Ejecutar el plan de pruebas del README en un emulador o teléfono.
- Revisar el diseño contra el MockUp de Trello.
- Confirmar en el repositorio oficial el nombre/URL y la fecha límite de los commits.
- Grabar el video requerido y completar la documentación de la entrega.

## Limitaciones técnicas conocidas

La captura de cámara se usa como recurso nativo de demostración, pero la imagen no se persiste; los casos y sus acciones sí se guardan mediante Room. No se implementa servidor, GPS, notificaciones push ni autenticación segura.

La fuente original se recibió como RAR. Este entorno no dispone de Android SDK configurado ni de acceso de red para descargar dependencias, por lo que la versión modificada no pudo compilarse aquí. La compilación final debe comprobarse en Android Studio antes de entregar.
