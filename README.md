# TOP Security — MVP académico

Aplicación Android nativa desarrollada en Kotlin y Jetpack Compose para demostrar un flujo simplificado de registro, derivación, seguimiento y cierre de incidencias comunitarias. El proyecto está pensado para una demostración académica y trabaja con información de prueba.

## Tecnologías

- Kotlin y Android Studio.
- Jetpack Compose y Material 3 para la interfaz.
- MVVM: pantallas en `ui/`, lógica de presentación en `viewmodel/`, modelos de interfaz en `model/` y acceso a datos en `data/`.
- Room para persistir la sesión, las incidencias y las acciones de trazabilidad.
- KSP para generar el código de Room.
- Recurso nativo de Android: captura de evidencia de muestra usando la aplicación de cámara del dispositivo.

## Funcionalidades incluidas

- Selección de perfil simulado: vecino/residente, operador municipal o inspector en terreno.
- Validación del alias de prueba.
- Registro de incidencia con categoría, sector de prueba, descripción y captura de imagen opcional.
- Validación de campos obligatorios del formulario.
- Lista de incidencias y filtros por estado.
- Detalle de caso con prioridad, responsable, fecha de ingreso y trazabilidad cronológica.
- Gestión de incidencias por perfiles municipales: estado, prioridad, responsable y observación.
- Modal para confirmar el cierre del caso.
- Animación de entrada/salida del bloque de gestión municipal.
- Datos sintéticos de demostración cargados solo si Room no contiene incidencias.
- Persistencia local con Room, incluyendo las acciones realizadas sobre cada caso.
- Migración de la base de datos de la versión 1 a la versión 2, conservando la tabla de sesión existente.

## Arquitectura

```text
MainActivity
  └── MainAppScreen / pantallas Compose (ui)
        └── TopSecurityViewModel (viewmodel)
              └── TopSecurityRepository (data)
                    └── Room: AppDatabase + DAOs + Entities
```

Responsabilidades principales:

- **UI:** muestra datos y notifica eventos de la persona usuaria.
- **ViewModel:** administra el estado visible y coordina el registro y la gestión del caso.
- **Repository:** centraliza las operaciones de datos y las transacciones.
- **Room:** persiste la sesión, los casos y el historial de acciones.

## Abrir y ejecutar en Android Studio

1. Descomprime el proyecto y abre la carpeta raíz que contiene `settings.gradle.kts`.
2. Usa JDK 17 como Gradle JDK.
3. En SDK Manager, instala Android SDK Platform 34 y las herramientas de compilación correspondientes.
4. Sincroniza el proyecto con Gradle. La primera sincronización puede necesitar conexión a internet para descargar dependencias.
5. Ejecuta la configuración `app` en un emulador o dispositivo Android.
6. Para compilar un APK de depuración, usa `Build > Build Bundle(s) / APK(s) > Build APK(s)` o, desde una terminal de Windows, `gradlew.bat assembleDebug`.

No se incluye `local.properties` porque ese archivo contiene la ruta local del SDK de cada integrante; Android Studio lo generará para el equipo.

## Pruebas manuales recomendadas

1. Inicia la app y entra con un alias ficticio y el perfil Vecino/Residente.
2. Intenta registrar un caso sin sector o con una descripción demasiado corta: deben aparecer mensajes de validación.
3. Registra un caso de prueba con categoría, sector ficticio y descripción.
4. Cierra y vuelve a abrir la app: la sesión y las incidencias deben seguir guardadas.
5. Cierra sesión e ingresa como Operador Municipal o Inspector en Terreno.
6. Abre el caso, cambia estado/prioridad/responsable, agrega una observación y guarda el avance.
7. Revisa el historial y confirma que se agregó la acción.
8. Cierra el caso usando el modal y comprueba que el cierre aparece en la trazabilidad.
9. Usa los filtros para buscar casos por estado.

## Límites conocidos del MVP

- El acceso por perfil es una simulación académica, no autenticación segura ni control de permisos en un servidor.
- Los datos se almacenan localmente en el dispositivo; no hay API ni sincronización con la Municipalidad.
- La imagen capturada se muestra durante el formulario, pero no se almacena como archivo ni se persiste en Room. El historial indica que se capturó una evidencia de demostración.
- El sector de ubicación debe ser ficticio; no se integra GPS ni se solicitan direcciones reales.
- Las notificaciones, la asignación municipal real y la integración con sistemas productivos quedan fuera de alcance.
- Los casos precargados, categorías, perfiles y unidades son solo ejemplos de prueba, no representan el procedimiento operativo real de la Municipalidad.

## Datos y privacidad

Usar exclusivamente alias, ubicaciones, 
descripciones e imágenes ficticias o autorizadas para pruebas.
No se registran nombres reales, RUT, teléfonos, domicilios particulares,
fotografías reales de personas, credenciales ni información operacional municipal.
