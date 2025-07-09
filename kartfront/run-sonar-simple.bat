@echo off
REM Script simplificado para ejecutar SonarQube con token configurado
REM Proyecto: Karting Frontend - Sistema Arriendo

echo 🏁 Iniciando análisis de SonarQube...
echo 📊 Proyecto: Karting Frontend - Sistema Arriendo
echo 🌐 Servidor: http://localhost:9000

REM Verificar que SonarQube esté corriendo
echo 🔧 Verificando conexión a SonarQube...
curl -s http://localhost:9000 > nul
if errorlevel 1 (
    echo ❌ Error: No se puede conectar a SonarQube en http://localhost:9000
    echo    Asegúrate de que SonarQube esté ejecutándose
    pause
    exit /b 1
)
echo ✅ SonarQube está corriendo

REM Verificar dependencias
echo 📦 Verificando dependencias...
if not exist node_modules (
    echo 📥 Instalando dependencias...
    call npm install
    if errorlevel 1 (
        echo ❌ Error instalando dependencias
        pause
        exit /b 1
    )
)

REM Generar reporte de ESLint
echo 🔨 Generando reporte de ESLint...
call npm run lint -- --format json --output-file eslint-report.json
if errorlevel 1 (
    echo ⚠️  ESLint encontró problemas, pero continuando con el análisis...
)

REM Verificar sonar-scanner
echo 🔧 Verificando SonarQube Scanner...
where sonar-scanner > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: sonar-scanner no está instalado
    echo 📥 Opciones de instalación:
    echo    1. npm install -g sonar-scanner
    echo    2. Descargar desde: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/
    pause
    exit /b 1
)

REM Ejecutar análisis SonarQube (usando el token del archivo de configuración)
echo 🚀 Ejecutando análisis SonarQube...
sonar-scanner

if errorlevel 0 (
    echo ✅ Análisis completado exitosamente!
    echo 🌐 Ver resultados en: http://localhost:9000/dashboard?id=kartfront
    echo 📋 Componentes analizados:
    echo    - Sistema de Clientes
    echo    - Sistema de Reservas  
    echo    - Sistema de Karting
    echo    - Sistema de Reportes
    echo    - Sistema de Comprobantes
    echo    - Sistema de Tarifas
    echo    - Calendario
    echo.
    echo 🎯 Métricas disponibles:
    echo    - Calidad del código
    echo    - Bugs y vulnerabilidades
    echo    - Code smells
    echo    - Complejidad
    echo    - Duplicación
    pause
) else (
    echo ❌ Error durante el análisis SonarQube
    pause
    exit /b 1
)
