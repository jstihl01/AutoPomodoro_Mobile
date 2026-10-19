$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot
if (-not $env:JAVA_HOME -and (Test-Path 'D:/Android/Android Studio/jbr')) {
    $env:JAVA_HOME = 'D:/Android/Android Studio/jbr'
}
if (-not (Test-Path (Join-Path $projectRoot '.private/signing/keystore.properties'))) {
    throw 'Falta la clave permanente. Ejecuta scripts/new-signing-key.ps1 sólo si ésta es la primera publicación.'
}
Push-Location $projectRoot
try {
    & .\gradlew.bat testDebugUnitTest lintDebug lintRelease assembleRelease
    if ($LASTEXITCODE -ne 0) { throw 'La compilación o las comprobaciones han fallado.' }
    $output = Join-Path $projectRoot 'releases'
    New-Item -ItemType Directory -Force $output | Out-Null
    $apk = Join-Path $output 'AutoPomodoro.apk'
    Copy-Item 'app/build/outputs/apk/release/app-release.apk' $apk -Force
    $hash = (Get-FileHash $apk -Algorithm SHA256).Hash.ToLowerInvariant()
    "$hash  AutoPomodoro.apk" | Set-Content -Encoding ascii (Join-Path $output 'AutoPomodoro.apk.sha256')
    Write-Output "APK firmado: $apk"
} finally {
    Pop-Location
}
