# Firma de las versiones Android

Las actualizaciones deben conservar el identificador `com.jstihl01.autopomodoro.mobile` y la misma clave de firma. Incrementa `versionCode` para cada publicación.

La clave privada está en `.private/signing/AutoPomodoro-release.jks`; el alias y las contraseñas están en `.private/signing/keystore.properties`. Gradle carga este archivo sólo cuando existe. Ninguno de estos archivos se publica en Git.

La copia privada inicial está en `.private/signing-backup/AutoPomodoro-signing.zip`. Contiene la clave y sus credenciales: guárdala también en un medio externo privado y seguro. La copia del mismo equipo no protege frente a la pérdida del disco. No adjuntes esta copia a una release ni a un issue.

Para la primera publicación, desde PowerShell 7:

```powershell
./scripts/new-signing-key.ps1
./scripts/build-release.ps1
```

Para las siguientes versiones, conserva la clave y ejecuta únicamente `build-release.ps1`. Si cambias de equipo, restaura los dos archivos dentro de `.private/signing/` antes de compilar. El script de creación se niega a sobrescribir claves existentes.

Android Studio instala la variante `debug` con sufijo `.debug`, que puede coexistir con el APK de producción. La versión de depuración usa la clave de desarrollo de Android; no sustituye la firma de producción.

Sin material de firma privado, Gradle permite compilar el proyecto y probar la variante debug. Para distribuir se debe usar el script de release, que exige la clave permanente.
