@echo off
REM Script completo para ejecutar SonarQube y verificar resultados
REM Proyecto: Karting Frontend - Sistema Arriendo

echo 🏁 ========================================
echo    ANALISIS SONARQUBE - KARTING FRONTEND
echo 🏁 ========================================
echo.

REM Paso 1: Verificar conexión a SonarQube
echo 🔧 PASO 1: Verificando conexión a SonarQube...
curl -s http://localhost:9000/api/system/status > nul
if errorlevel 1 (
    echo ❌ Error: SonarQube no está ejecutándose en http://localhost:9000
    echo.
    echo 📋 Para iniciar SonarQube:
    echo    1. Navega a tu carpeta de SonarQube
    echo    2. Ejecuta: bin\windows-x86-64\StartSonar.bat
    echo    3. Espera a que aparezca "SonarQube is up"
    echo    4. Visita: http://localhost:9000
    echo.
    pause
    exit /b 1
)
echo ✅ SonarQube está ejecutándose correctamente

REM Paso 2: Verificar que el usuario está logueado
echo.
echo 🔧 PASO 2: Verificando token de autenticación...
curl -s -H "Authorization: Bearer sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6" http://localhost:9000/api/user_tokens/search > nul
if errorlevel 1 (
    echo ❌ Error: Token inválido o expirado
    echo.
    echo 📋 Para generar un nuevo token:
    echo    1. Ve a: http://localhost:9000
    echo    2. Login como admin/admin
    echo    3. Usuario ^> Mi Cuenta ^> Seguridad
    echo    4. Genera un nuevo token
    echo.
    pause
    exit /b 1
)
echo ✅ Token de autenticación válido

REM Paso 3: Verificar dependencias del proyecto
echo.
echo 🔧 PASO 3: Verificando dependencias del proyecto...
if not exist node_modules (
    echo 📥 Instalando dependencias...
    call npm install
    if errorlevel 1 (
        echo ❌ Error instalando dependencias
        pause
        exit /b 1
    )
)
echo ✅ Dependencias instaladas

REM Paso 4: Verificar sonar-scanner
echo.
echo 🔧 PASO 4: Verificando SonarQube Scanner...
where sonar-scanner > nul 2>&1
if errorlevel 1 (
    echo ❌ Error: sonar-scanner no está instalado
    echo.
    echo 📥 Instalar SonarQube Scanner:
    echo    npm install -g sonar-scanner
    echo.
    echo ¿Quieres instalarlo ahora? (s/n)
    set /p install_scanner=
    if /i "%install_scanner%"=="s" (
        npm install -g sonar-scanner
        if errorlevel 1 (
            echo ❌ Error instalando sonar-scanner
            pause
            exit /b 1
        )
    ) else (
        echo ⚠️  Instalación cancelada
        pause
        exit /b 1
    )
)
echo ✅ SonarQube Scanner disponible

REM Paso 5: Limpiar archivos anteriores
echo.
echo 🔧 PASO 5: Limpiando archivos anteriores...
if exist eslint-report.json del eslint-report.json
if exist .scannerwork rmdir /s /q .scannerwork
echo ✅ Archivos limpiados

REM Paso 6: Generar reporte de ESLint
echo.
echo 🔧 PASO 6: Generando reporte de ESLint...
call npm run lint:report
if errorlevel 1 (
    echo ⚠️  ESLint encontró problemas, pero continuando...
) else (
    echo ✅ Reporte de ESLint generado
)

REM Paso 7: Ejecutar análisis SonarQube
echo.
echo 🚀 PASO 7: Ejecutando análisis SonarQube...
echo    Esto puede tomar unos minutos...
echo.
sonar-scanner

if errorlevel 0 (
    echo.
    echo ✅ ========================================
    echo    ANALISIS COMPLETADO EXITOSAMENTE!
    echo ✅ ========================================
    echo.
    echo 🌐 RESULTADOS DISPONIBLES EN:
    echo    http://localhost:9000/dashboard?id=kartfront
    echo.
    echo 📊 METRICAS ANALIZADAS:
    echo    - Calidad del código
    echo    - Bugs y vulnerabilidades  
    echo    - Code smells
    echo    - Complejidad ciclomática
    echo    - Duplicación de código
    echo    - Cobertura de tests
    echo.
    echo 📋 COMPONENTES ANALIZADOS:
    echo    - src/components/Calendario.jsx
    echo    - src/components/Carros.jsx
    echo    - src/components/Clientes.jsx
    echo    - src/components/Comprobante.jsx
    echo    - src/components/Reportes.jsx
    echo    - src/components/Reservar.jsx
    echo    - src/components/Tarifas.jsx
    echo    - src/services/*.js
    echo.
    echo 🎯 PROXIMOS PASOS:
    echo    1. Abre: http://localhost:9000
    echo    2. Ve a Projects ^> kartfront
    echo    3. Revisa las métricas de calidad
    echo    4. Corrige los issues encontrados
    echo.
    echo ¿Quieres abrir el dashboard ahora? (s/n)
    set /p open_browser=
    if /i "%open_browser%"=="s" (
        start http://localhost:9000/dashboard?id=kartfront
    )
) else (
    echo.
    echo ❌ ========================================
    echo    ERROR DURANTE EL ANALISIS
    echo ❌ ========================================
    echo.
    echo 🔍 POSIBLES CAUSAS:
    echo    - SonarQube no está ejecutándose
    echo    - Token inválido o expirado
    echo    - Problemas de conectividad
    echo    - Configuración incorrecta
    echo.
    echo 🔧 SOLUCION:
    echo    1. Verifica que SonarQube esté ejecutándose
    echo    2. Verifica el token en sonar-project.properties
    echo    3. Ejecuta este script nuevamente
    echo.
)

echo.
echo Presiona cualquier tecla para continuar...
pause > nul
