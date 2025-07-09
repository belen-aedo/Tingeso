@echo off
REM Verificación rápida de SonarQube - Solo lo esencial

echo 🏁 Verificación Rápida de SonarQube
echo =====================================
echo.

REM 1. Verificar SonarQube
echo 🔍 Verificando SonarQube Server...
curl -s http://localhost:9000 > nul
if errorlevel 1 (
    echo ❌ SonarQube no está ejecutándose en localhost:9000
    echo    Inicia SonarQube y vuelve a intentar
    pause
    exit /b 1
) else (
    echo ✅ SonarQube está ejecutándose
)

REM 2. Verificar token
echo 🔍 Verificando token...
curl -s -H "Authorization: Bearer sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6" http://localhost:9000/api/authentication/validate > temp_auth.txt
findstr "valid" temp_auth.txt > nul
if errorlevel 1 (
    echo ❌ Token inválido
    del temp_auth.txt
    pause
    exit /b 1
) else (
    echo ✅ Token válido
    del temp_auth.txt
)

REM 3. Verificar sonar-scanner
echo 🔍 Verificando sonar-scanner...
where sonar-scanner > nul 2>&1
if errorlevel 1 (
    echo ❌ sonar-scanner no está instalado
    echo    Ejecuta: npm install -g sonar-scanner
    pause
    exit /b 1
) else (
    echo ✅ sonar-scanner disponible
)

REM 4. Verificar archivos del proyecto
echo 🔍 Verificando archivos del proyecto...
if not exist "sonar-project.properties" (
    echo ❌ sonar-project.properties no existe
    pause
    exit /b 1
) else (
    echo ✅ sonar-project.properties existe
)

if not exist "src" (
    echo ❌ Carpeta src no existe
    pause
    exit /b 1
) else (
    echo ✅ Carpeta src existe
)

echo.
echo ✅ VERIFICACION EXITOSA!
echo    Todo está listo para el análisis SonarQube
echo.
echo 🚀 Opciones:
echo    1. Ejecutar análisis completo: .\run-sonar-complete.bat
echo    2. Ejecutar análisis simple: npm run sonar:token
echo    3. Ver resultados en: http://localhost:9000/dashboard?id=kartfront
echo.

echo ¿Ejecutar análisis ahora? (s/n)
set /p execute=
if /i "%execute%"=="s" (
    echo.
    echo 🚀 Ejecutando análisis...
    sonar-scanner -Dsonar.login=sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6
    
    if errorlevel 0 (
        echo.
        echo ✅ Análisis completado!
        echo 🌐 Ver resultados: http://localhost:9000/dashboard?id=kartfront
        echo.
        echo ¿Abrir resultados en navegador? (s/n)
        set /p open_browser=
        if /i "%open_browser%"=="s" (
            start http://localhost:9000/dashboard?id=kartfront
        )
    ) else (
        echo ❌ Error en el análisis
    )
)

pause
