# Pruebas y aceptación

## Comprobaciones automáticas

Las pruebas unitarias cubren límites 25/5 y 50/10, redondeo de fracciones de segundo, medianoche, reloj de 24 horas, cambios de zona y fuente de tiempo sustituible. También guardan ajustes en un DataStore real, lo cierran y lo vuelven a abrir para verificar persistencia, límites del volumen y restablecimiento.

Las pruebas de transiciones comprueban los cambios de fase, ausencia de duplicados y silencio ante apertura, retorno, cambio de modo, salto horario o cambio de zona. Los tonos se verifican por duración, número de repeticiones, pausas y envolvente suave.

Las pruebas instrumentadas comprueban los controles, la ausencia de fases en Reloj, el restablecimiento, recreación de actividad, giro, texto ampliado y la bandera que mantiene encendida la pantalla. Generan capturas reales de los tres modos.

```powershell
./gradlew.bat testDebugUnitTest lintDebug lintRelease assembleDebug assembleDebugAndroidTest
./gradlew.bat connectedDebugAndroidTest
```

## Prueba prolongada

La prueba `clockKeepsScreenAwakeForOneHour` se habilita expresamente:

```powershell
./gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.soakMinutes=60
```

Selecciona Reloj, reduce el apagado por inactividad a 15 segundos y desactiva la opción de permanecer encendido al cargar. Durante 60 minutos comprueba cada minuto que la pantalla sigue despierta, que el reloj se actualiza y que no muestra fases. Después sale a Inicio y comprueba que la pantalla puede apagarse. Restaura los ajustes del emulador al terminar.

Esta prueba modifica temporalmente la configuración del dispositivo de prueba: ejecútala preferentemente en un emulador dedicado.

## Aceptación en tu teléfono

- Instalar desde el botón del README y abrir sin errores.
- Comprobar legibilidad desde lejos en vertical y horizontal, especialmente en Reloj.
- Verificar que modo y volumen se conservan al cerrar y reabrir.
- Escuchar la prueba sonora y los avisos con el volumen habitual del teléfono; confirmar silencio con volumen 0 y en Reloj.
- Dejar Reloj abierto con el apagado habitual del móvil y comprobar que permanece visible.
- Pulsar Inicio y bloquear; confirmar ausencia de avisos y sincronización al regresar.
- Instalar una actualización firmada con la misma clave y verificar que conserva los ajustes.

La verificación en emulador y la comprobación de firma no sustituyen esta aceptación en el dispositivo físico del usuario.

## Resultados de la primera versión

Validación del 19 de octubre de 2026:

| Comprobación | Resultado |
|---|---|
| Pruebas unitarias | 12 pruebas superadas; sin fallos. |
| Análisis de Android | `lintDebug` y `lintRelease` superados, sin errores. |
| Interfaz y ciclo de vida en Android 15 | 5 pruebas superadas; la prueba prolongada se ejecuta por separado. |
| Capturas | Tres modos, reloj horizontal y texto ampliado al 150 %, revisados visualmente. |
| APK firmado | Firma v2 válida, certificado RSA de 3072 bits; mínimo Android 7.0. |
| Instalación y reinstalación del APK | Superadas en Android 7.0 y Android 15. |
| Reloj permanente durante 60 minutos | [Registro de la ejecución prolongada](https://github.com/jstihl01/AutoPomodoro_Mobile/actions/runs/39319799879), con comprobaciones cada minuto y prueba de apagado al salir. |

[Ejecución de interfaz y compilación](https://github.com/jstihl01/AutoPomodoro_Mobile/actions/runs/39319798331), [ejecuciones de instalación del APK firmado](https://github.com/jstihl01/AutoPomodoro_Mobile/actions/workflows/production-smoke.yml).

La clave de producción tiene la huella SHA-256 de certificado `49bf8bf80793b35c59ce11e158de225de10d3c5664b8b08f873716bfb8cdbc8b`. Es una huella pública, distinta del checksum de cada APK.
