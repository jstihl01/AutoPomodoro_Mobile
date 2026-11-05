# Validación del rediseño 1.1.0 — 5 de noviembre de 2026

Versión **1.1.0**, `versionCode=2`. Windows 2.0.0 se ha utilizado únicamente como referencia de diseño.

## Interfaz

- Referencia: diseño aprobado de Windows 2.0.0 (`Design.xaml`, `MainWindow.xaml`, `SettingsWindow.xaml`, `VolumeIcon.xaml`).
- Trabajo 25/5 `#24985B`, trabajo 50/10 `#C6384E`, descanso `#1E6BD3` y reloj negro plano. Ajustes `#174B80` y botones `#245FA2`.
- Fuente monoespaciada del sistema Android para MM:SS y HH:mm:ss. El tamaño se calcula con una plantilla fija; el cambio de segundos no cambia el ancho de los caracteres ni la escala.
- Engranaje vectorial, tarjetas de modo con borde blanco, botones de radio accesibles y controles redondeados sin sombras decorativas.
- Volumen con pista de 6 dp y asa circular de 22 dp, manteniendo el área táctil y la interacción accesible del Slider de Material.
- Se mantienen 25/5, 50/10, Reloj, la pantalla encendida en primer plano y el funcionamiento horario existente. No se han trasladado controles de la barra de tareas.

## Icono

El icono anterior combinaba un fondo negro, un bitmap con margen y un inset adicional del 12 %. Ahora usa un fondo vectorial con degradado que llena los 108×108 dp y un primer plano transparente con el reloj blanco dentro de la zona segura del icono adaptativo. El recurso de Android 7 también es vectorial y cubre todo el área. Se han retirado los antiguos iconos bitmap para evitar reutilizarlos por densidad.

## Audio

Se conserva la onda senoidal y su envolvente suave, frecuencias, duraciones y pausas. Se aumenta el pico de **0,35 a 0,90**, sin saturación y con aproximadamente 0,92 dB de margen hasta el máximo digital. AudioTrack reproduce en el canal multimedia, y los botones del dispositivo controlan ese mismo canal. La aplicación no cambia el volumen general del teléfono.

| Comparación con 1.0.0 | Resultado |
|---|---|
| Amplitud RMS al mismo porcentaje | 2,5715× (+8,20 dB) |
| Nuevo valor inicial 100 % frente al antiguo 60 % | 4,2859× (+12,64 dB) |
| Pico máximo de PCM de 16 bits | 29490 de 32767 |
| Muestras recortadas | 0 |

La ganancia es de la señal digital; el volumen percibido debe contrastarse con el altavoz y el ajuste multimedia del teléfono. Los niveles guardados por el usuario se conservan, incluido el silencio. Los ajustes nuevos o restablecidos usan el 100 %. «Probar volumen» sigue disponible y se desactiva en Reloj y con volumen cero.

La prueba `AudioSignalTest` genera WAV antes/después y `comparison.json` en `app/build/reports/audio/`. La copia de esta ejecución se conserva en `.private/validation-redesign/audio/` para poder escuchar y comparar.

## Comprobaciones realizadas

- 13 pruebas unitarias superadas, incluyendo comparación RMS, margen sin recorte, persistencia de un volumen previo y restablecimiento al 100 %.
- Compiladas las variantes debug, release y el APK de pruebas instrumentadas.
- `lintDebug` y `lintRelease` superados, sin errores.
- Revisión visual mediante el renderizador de Android Studio: colores planos, cifras monoespaciadas, ajustes al 100 %, fuente ampliada al 150 %, Reloj en horizontal e icono de marca.
- APK de producción firmado con la misma clave privada de 1.0.0 y código de versión incrementado, para permitir su instalación como actualización de 1.0.0.

Las pruebas instrumentadas se ejecutan en el flujo [Android](https://github.com/jstihl01/AutoPomodoro_Mobile/actions/workflows/android.yml) sobre Android 15. Incluyen cambios de modo, giro, persistencia y estado de pantalla, además de dos comprobaciones del icono: fondo opaco sin franjas negras y reloj dentro de la zona segura del launcher. Los informes y capturas de cada ejecución se conservan como artefactos de GitHub Actions.

El flujo [APK installation](https://github.com/jstihl01/AutoPomodoro_Mobile/actions/workflows/production-smoke.yml) permite seleccionar la etiqueta de release y comprueba el checksum, instalación, apertura y reinstalación del APK firmado en Android 7.0 y Android 15. La publicación se realiza después de comprobar el resultado de ambos flujos.

No se ha repetido la observación de una hora porque no se ha cambiado el motor horario ni la gestión de la pantalla encendida. La aceptación del volumen percibido y el launcher específico del teléfono físico sigue correspondiendo a la prueba del usuario.
## Artefactos

- `releases/AutoPomodoro.apk` y su checksum, adjuntos también a la release v1.1.0.
- `app/build/outputs/apk/debug/app-debug.apk` para la instalación de desarrollo separada.
- [Previsualizaciones](screenshots/redesign/) y WAV comparativos locales.

Para aceptar la actualización en el teléfono: instalar el APK en el móvil, revisar el recorte del icono del launcher real, comparar los pitidos con un mismo ajuste multimedia y comprobar selección de modo, giro, persistencia y regreso desde segundo plano.
## Recuperación de Android Studio

El arranque del IDE se recuperó conservando íntegra la caché anterior en una copia de seguridad fuera del repositorio, con autorización del usuario. Su ubicación local está registrada en `.private/studio-cache-backup.txt`. No se han borrado los datos de esa copia.

Después de recuperar el IDE se renderizaron y revisaron también el icono con máscaras circular, redondeada y cuadrada, Reloj en vertical, una pantalla pequeña y ajustes en horizontal. Las imágenes están en [screenshots/redesign/](screenshots/redesign/).
