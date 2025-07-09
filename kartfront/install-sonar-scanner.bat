@echo off
REM Script para instalar SonarQube Scanner globalmente

echo 🔧 Instalando SonarQube Scanner...
echo.

REM Verificar si ya está instalado
where sonar-scanner > nul 2>&1
if not errorlevel 1 (
    echo ✅ SonarQube Scanner ya está instalado
    sonar-scanner -h | findstr version
    echo.
    echo ¿Quieres reinstalarlo? (s/n)
    set /p reinstall=
    if /i not "%reinstall%"=="s" (
        echo ⚠️  Instalación cancelada
        pause
        exit /b 0
    )
)

REM Verificar Node.js
where node > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Node.js no está instalado
    echo 📥 Instala Node.js desde: https://nodejs.org/
    pause
    exit /b 1
)

REM Verificar npm
where npm > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: npm no está disponible
    pause
    exit /b 1
)

echo 📦 Instalando SonarQube Scanner via npm...
npm install -g sonar-scanner

if errorlevel 0 (
    echo.
    echo ✅ SonarQube Scanner instalado exitosamente!
    echo.
    echo 🔧 Verificando instalación...
    sonar-scanner -h | findstr version
    echo.
    echo 🎯 Ahora puedes ejecutar el análisis:
    echo    .\run-sonar-complete.bat
    echo.
) else (
    echo.
    echo ❌ Error durante la instalación
    echo.
    echo 🔧 Alternativas:
    echo    1. Descargar manualmente desde:
    echo       https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/
    echo    2. Usar Chocolatey: choco install sonarscanner-msbuild-net46
    echo.
)

pause
