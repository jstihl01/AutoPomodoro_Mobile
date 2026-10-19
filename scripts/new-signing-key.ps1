$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot
$signingDirectory = Join-Path $projectRoot '.private/signing'
$keyFile = Join-Path $signingDirectory 'AutoPomodoro-release.jks'
$propertiesFile = Join-Path $signingDirectory 'keystore.properties'
if ((Test-Path $keyFile) -or (Test-Path $propertiesFile)) {
    throw 'Ya existe material de firma. No se reemplaza una clave existente.'
}
if (-not $env:JAVA_HOME -and (Test-Path 'D:/Android/Android Studio/jbr')) {
    $env:JAVA_HOME = 'D:/Android/Android Studio/jbr'
}
$keytool = Join-Path $env:JAVA_HOME 'bin/keytool.exe'
if (-not (Test-Path $keytool)) { throw 'Configura JAVA_HOME con el JDK de Android Studio.' }
New-Item -ItemType Directory -Force $signingDirectory | Out-Null
$passwordBytes = New-Object byte[] 36
[Security.Cryptography.RandomNumberGenerator]::Fill($passwordBytes)
$env:AUTOPOMODORO_SIGNING_PASSWORD = [Convert]::ToBase64String($passwordBytes)
try {
    & $keytool -genkeypair -keystore $keyFile -alias autopomodoro -keyalg RSA -keysize 3072 -validity 10000 -storetype JKS -storepass:env AUTOPOMODORO_SIGNING_PASSWORD -keypass:env AUTOPOMODORO_SIGNING_PASSWORD -dname 'CN=AutoPomodoro, OU=Android, O=jstihl01, C=ES' 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'No se ha podido generar la clave.' }
    @"
storeFile=.private/signing/AutoPomodoro-release.jks
storePassword=$env:AUTOPOMODORO_SIGNING_PASSWORD
keyAlias=autopomodoro
keyPassword=$env:AUTOPOMODORO_SIGNING_PASSWORD
"@ | Set-Content -Encoding ascii $propertiesFile
    $backupDirectory = Join-Path $projectRoot '.private/signing-backup'
    New-Item -ItemType Directory -Force $backupDirectory | Out-Null
    Compress-Archive -LiteralPath $keyFile,$propertiesFile -DestinationPath (Join-Path $backupDirectory 'AutoPomodoro-signing.zip')
    Write-Output 'Clave permanente y copia privada creadas en .private/. Conserva una copia externa segura antes de reinstalar el equipo.'
} finally {
    Remove-Item Env:AUTOPOMODORO_SIGNING_PASSWORD -ErrorAction SilentlyContinue
    [Array]::Clear($passwordBytes, 0, $passwordBytes.Length)
}
