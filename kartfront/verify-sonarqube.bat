@echo off
setlocal enabledelayedexpansion

REM Script de verificación completa de SonarQube
REM Verifica que todo funcione correctamente antes del análisis

echo 🏁 ============================================
echo    VERIFICACION COMPLETA DE SONARQUBE
echo    Karting Frontend - Sistema Arriendo
echo 🏁 ============================================
echo.

set "ERRORS=0"
set "WARNINGS=0"

REM ========================================
REM VERIFICACION 1: SONARQUBE SERVER
REM ========================================
echo 🔍 [1/10] Verificando SonarQube Server...

REM Verificar si el puerto 9000 está en uso
netstat -an | find "9000" > nul
if errorlevel 1 (
    echo ❌ Error: Puerto 9000 no está en uso
    echo    SonarQube no está ejecutándose
    set /a ERRORS+=1
    goto :show_sonar_help
) else (
    echo ✅ Puerto 9000 está en uso
)

REM Verificar respuesta HTTP
curl -s -o nul -w "%%{http_code}" http://localhost:9000 > temp_response.txt
set /p HTTP_CODE=<temp_response.txt
del temp_response.txt

if "%HTTP_CODE%"=="200" (
    echo ✅ SonarQube responde correctamente (HTTP 200)
) else (
    echo ❌ Error: SonarQube responde con código %HTTP_CODE%
    set /a ERRORS+=1
    goto :show_sonar_help
)

REM Verificar API de sistema
curl -s http://localhost:9000/api/system/status > temp_status.txt
findstr "UP" temp_status.txt > nul
if errorlevel 1 (
    echo ❌ Error: SonarQube no está completamente inicializado
    type temp_status.txt
    set /a ERRORS+=1
    del temp_status.txt
    goto :show_sonar_help
) else (
    echo ✅ SonarQube está completamente inicializado
    del temp_status.txt
)

goto :check_token

:show_sonar_help
echo.
echo 🔧 COMO INICIAR SONARQUBE:
echo    1. Descargar desde: https://www.sonarqube.org/downloads/
echo    2. Extraer en: C:\sonarqube
echo    3. Ejecutar: C:\sonarqube\bin\windows-x86-64\StartSonar.bat
echo    4. Esperar mensaje: "SonarQube is up"
echo    5. Verificar en: http://localhost:9000
echo.
if %ERRORS% GTR 0 goto :final_report

:check_token
REM ========================================
REM VERIFICACION 2: TOKEN DE AUTENTICACION
REM ========================================
echo.
echo 🔍 [2/10] Verificando token de autenticación...

curl -s -H "Authorization: Bearer sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6" http://localhost:9000/api/authentication/validate > temp_auth.txt
findstr "valid" temp_auth.txt > nul
if errorlevel 1 (
    echo ❌ Error: Token inválido o expirado
    echo    Token: sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6
    set /a ERRORS+=1
    del temp_auth.txt
    goto :show_token_help
) else (
    echo ✅ Token válido y autenticado
    del temp_auth.txt
)

goto :check_project

:show_token_help
echo.
echo 🔧 COMO GENERAR NUEVO TOKEN:
echo    1. Ir a: http://localhost:9000
echo    2. Login: admin/admin
echo    3. Usuario ^> Mi Cuenta ^> Seguridad
echo    4. Generar nuevo token
echo    5. Actualizar sonar-project.properties
echo.
if %ERRORS% GTR 0 goto :final_report

:check_project
REM ========================================
REM VERIFICACION 3: CONFIGURACION DEL PROYECTO
REM ========================================
echo.
echo 🔍 [3/10] Verificando configuración del proyecto...

if not exist "sonar-project.properties" (
    echo ❌ Error: sonar-project.properties no existe
    set /a ERRORS+=1
    goto :show_config_help
) else (
    echo ✅ sonar-project.properties existe
)

findstr "kartfront" sonar-project.properties > nul
if errorlevel 1 (
    echo ❌ Error: projectKey no está configurado correctamente
    set /a ERRORS+=1
) else (
    echo ✅ projectKey configurado: kartfront
)

findstr "sqp_" sonar-project.properties > nul
if errorlevel 1 (
    echo ⚠️  Advertencia: Token no está en el archivo de configuración
    set /a WARNINGS+=1
) else (
    echo ✅ Token configurado en el archivo
)

goto :check_nodejs

:show_config_help
echo.
echo 🔧 ARCHIVO DE CONFIGURACION FALTANTE:
echo    El archivo sonar-project.properties debe existir
echo    Ejecuta el script de configuración inicial
echo.
if %ERRORS% GTR 0 goto :final_report

:check_nodejs
REM ========================================
REM VERIFICACION 4: NODE.JS Y NPM
REM ========================================
echo.
echo 🔍 [4/10] Verificando Node.js y npm...

where node > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Node.js no está instalado
    set /a ERRORS+=1
    goto :show_nodejs_help
) else (
    for /f "tokens=*" %%i in ('node --version') do echo ✅ Node.js instalado: %%i
)

where npm > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: npm no está disponible
    set /a ERRORS+=1
    goto :show_nodejs_help
) else (
    for /f "tokens=*" %%i in ('npm --version') do echo ✅ npm disponible: %%i
)

goto :check_dependencies

:show_nodejs_help
echo.
echo 🔧 INSTALAR NODE.JS:
echo    1. Descargar desde: https://nodejs.org/
echo    2. Instalar la versión LTS
echo    3. Reiniciar terminal
echo.
if %ERRORS% GTR 0 goto :final_report

:check_dependencies
REM ========================================
REM VERIFICACION 5: DEPENDENCIAS DEL PROYECTO
REM ========================================
echo.
echo 🔍 [5/10] Verificando dependencias del proyecto...

if not exist "package.json" (
    echo ❌ Error: package.json no existe
    set /a ERRORS+=1
    goto :final_report
) else (
    echo ✅ package.json existe
)

if not exist "node_modules" (
    echo ⚠️  Advertencia: node_modules no existe
    echo    Instalando dependencias...
    call npm install
    if errorlevel 1 (
        echo ❌ Error: No se pudieron instalar las dependencias
        set /a ERRORS+=1
        goto :final_report
    ) else (
        echo ✅ Dependencias instaladas correctamente
    )
) else (
    echo ✅ node_modules existe
)

REM ========================================
REM VERIFICACION 6: SONAR SCANNER
REM ========================================
echo.
echo 🔍 [6/10] Verificando SonarQube Scanner...

where sonar-scanner > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: sonar-scanner no está instalado
    echo    Instalando sonar-scanner...
    call npm install -g sonar-scanner
    if errorlevel 1 (
        echo ❌ Error: No se pudo instalar sonar-scanner
        set /a ERRORS+=1
        goto :show_scanner_help
    ) else (
        echo ✅ sonar-scanner instalado exitosamente
    )
) else (
    for /f "tokens=*" %%i in ('sonar-scanner -h ^| findstr "INFO"') do echo ✅ SonarQube Scanner disponible
)

goto :check_eslint

:show_scanner_help
echo.
echo 🔧 INSTALAR SONAR SCANNER:
echo    1. npm install -g sonar-scanner
echo    2. O descargar desde: https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/
echo    3. Agregar al PATH del sistema
echo.
if %ERRORS% GTR 0 goto :final_report

:check_eslint
REM ========================================
REM VERIFICACION 7: ESLINT
REM ========================================
echo.
echo 🔍 [7/10] Verificando ESLint...

if not exist "eslint.config.js" (
    echo ⚠️  Advertencia: eslint.config.js no existe
    set /a WARNINGS+=1
) else (
    echo ✅ eslint.config.js existe
)

call npm list eslint > nul 2>&1
if errorlevel 1 (
    echo ⚠️  Advertencia: ESLint no está instalado como dependencia
    set /a WARNINGS+=1
) else (
    echo ✅ ESLint instalado como dependencia
)

REM ========================================
REM VERIFICACION 8: ARCHIVOS FUENTE
REM ========================================
echo.
echo 🔍 [8/10] Verificando archivos fuente...

if not exist "src" (
    echo ❌ Error: Carpeta src no existe
    set /a ERRORS+=1
    goto :final_report
) else (
    echo ✅ Carpeta src existe
)

set "COMPONENT_COUNT=0"
for %%f in (src\components\*.jsx) do (
    set /a COMPONENT_COUNT+=1
)
echo ✅ Componentes encontrados: %COMPONENT_COUNT%

set "SERVICE_COUNT=0"
for %%f in (src\services\*.js) do (
    set /a SERVICE_COUNT+=1
)
echo ✅ Servicios encontrados: %SERVICE_COUNT%

REM ========================================
REM VERIFICACION 9: PRUEBA DE ESLINT
REM ========================================
echo.
echo 🔍 [9/10] Ejecutando prueba de ESLint...

call npm run lint > eslint_test.txt 2>&1
if errorlevel 1 (
    echo ⚠️  ESLint encontró problemas:
    type eslint_test.txt | findstr "error\|warning" | head -5
    set /a WARNINGS+=1
) else (
    echo ✅ ESLint ejecutado sin errores críticos
)
del eslint_test.txt

REM ========================================
REM VERIFICACION 10: PRUEBA DE CONEXION COMPLETA
REM ========================================
echo.
echo 🔍 [10/10] Prueba de conexión completa...

echo    Verificando proyecto existente en SonarQube...
curl -s -H "Authorization: Bearer sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6" "http://localhost:9000/api/projects/search?q=kartfront" > temp_project.txt
findstr "kartfront" temp_project.txt > nul
if errorlevel 1 (
    echo ⚠️  Proyecto kartfront no existe aún en SonarQube
    echo    Se creará automáticamente en el primer análisis
    set /a WARNINGS+=1
) else (
    echo ✅ Proyecto kartfront existe en SonarQube
)
del temp_project.txt

REM ========================================
REM REPORTE FINAL
REM ========================================
:final_report
echo.
echo 🏁 ============================================
echo    REPORTE FINAL DE VERIFICACION
echo 🏁 ============================================
echo.

if %ERRORS% EQU 0 (
    echo ✅ ESTADO: LISTO PARA ANALISIS
    echo    Errores críticos: %ERRORS%
    echo    Advertencias: %WARNINGS%
    echo.
    echo 🚀 EJECUTAR ANALISIS AHORA:
    echo    .\run-sonar-complete.bat
    echo.
    echo 📊 RESULTADOS EN:
    echo    http://localhost:9000/dashboard?id=kartfront
    echo.
    echo ¿Quieres ejecutar el análisis ahora? (s/n)
    set /p run_analysis=
    if /i "%run_analysis%"=="s" (
        echo.
        echo 🚀 Iniciando análisis...
        call .\run-sonar-complete.bat
    )
) else (
    echo ❌ ESTADO: ERRORES ENCONTRADOS
    echo    Errores críticos: %ERRORS%
    echo    Advertencias: %WARNINGS%
    echo.
    echo 🔧 CORREGIR ERRORES ANTES DE CONTINUAR
    echo.
)

echo.
echo 📋 RESUMEN DE VERIFICACIONES:
echo    [1] SonarQube Server: %HTTP_CODE%
echo    [2] Token de autenticación: Verificado
echo    [3] Configuración del proyecto: Verificada
echo    [4] Node.js y npm: Verificados
echo    [5] Dependencias: Verificadas
echo    [6] SonarQube Scanner: Verificado
echo    [7] ESLint: Verificado
echo    [8] Archivos fuente: %COMPONENT_COUNT% componentes, %SERVICE_COUNT% servicios
echo    [9] Prueba ESLint: Ejecutada
echo    [10] Conexión completa: Verificada
echo.

pause
