@echo off
REM Script para ejecutar análisis SonarQube en Windows
REM Proyecto: Karting Frontend - Sistema Arriendo
REM Uso: run-sonar.bat "tu_token_aqui"

if "%~1"=="" (
    echo ❌ Error: Token requerido
    echo Uso: run-sonar.bat "tu_token_aqui"
    echo.
    echo Para obtener un token:
    echo 1. Ve a http://localhost:9000
    echo 2. Login como admin
    echo 3. Ve a User ^> My Account ^> Security
    echo 4. Genera un nuevo token
    exit /b 1
)

set SONAR_TOKEN=%~1
set SONAR_URL=http://localhost:9000

echo 🏁 Iniciando análisis de SonarQube...
echo 📊 Proyecto: Karting Frontend - Sistema Arriendo
echo 🌐 Servidor: %SONAR_URL%

REM Verificar que SonarQube esté corriendo
echo 🔧 Verificando conexión a SonarQube...
curl -s %SONAR_URL% > nul
if errorlevel 1 (
    echo ❌ Error: No se puede conectar a SonarQube en %SONAR_URL%
    echo    Asegúrate de que SonarQube esté ejecutándose
    exit /b 1
)
echo ✅ SonarQube está corriendo

REM Verificar Node.js y npm
echo 🔧 Verificando Node.js y npm...
where node > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Node.js no está instalado
    echo 📥 Instala Node.js desde: https://nodejs.org/
    exit /b 1
)

where npm > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: npm no está disponible
    exit /b 1
)

REM Instalar dependencias si es necesario
echo 📦 Verificando dependencias...
if not exist node_modules (
    echo 📥 Instalando dependencias...
    call npm install
    if errorlevel 1 (
        echo ❌ Error instalando dependencias
        exit /b 1
    )
)

REM Generar reporte de ESLint
echo 🔨 Generando reporte de ESLint...
c.\verify-sonarqube.batall npm run lint -- --format json --output-file eslint-report.json
if errorlevel 1 (
    echo ⚠️  ESLint encontró problemas, pero continuando con el análisis...
)

REM Verificar sonar-scanner
echo 🔧 Verificando SonarQube Scanner...
where sonar-scanner > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: sonar-scanner no está instalado
    echo 📥 Opciones de instalación:
    echo    1. Descargar desde: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/
    echo    2. Con npm: npm install -g sonar-scanner
    echo    3. Con chocolatey: choco install sonarscanner-msbuild-net46
    exit /b 1
)

REM Ejecutar análisis SonarQube
echo 🚀 Ejecutando análisis SonarQube...
sonar-scanner -Dsonar.login=%SONAR_TOKEN%

if errorlevel 0 (
    echo ✅ Análisis completado exitosamente!
    echo 🌐 Ver resultados en: %SONAR_URL%/dashboard?id=kartfront
    echo 📋 Componentes analizados:
    echo    - Sistema de Clientes
    echo    - Sistema de Reservas
    echo    - Sistema de Karting
    echo    - Sistema de Reportes
    echo    - Sistema de Comprobantes
    echo    - Sistema de Tarifas
) else (
    echo ❌ Error durante el análisis SonarQube
    exit /b 1
)