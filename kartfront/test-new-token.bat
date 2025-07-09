@echo off
REM Script para probar el nuevo token de SonarQube

echo 🔍 Probando nuevo token SonarQube...
echo Token: sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6
echo.

REM Probar conexión básica
echo 🔧 1. Verificando SonarQube Server...
curl -s http://localhost:9000 > nul
if errorlevel 1 (
    echo ❌ SonarQube no está ejecutándose
    echo    Inicia SonarQube en localhost:9000
    pause
    exit /b 1
) else (
    echo ✅ SonarQube está ejecutándose
)

REM Probar token
echo 🔧 2. Verificando token...
curl -s -H "Authorization: Bearer sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6" http://localhost:9000/api/authentication/validate > temp_token_test.txt
findstr "valid" temp_token_test.txt > nul
if errorlevel 1 (
    echo ❌ Token inválido o expirado
    type temp_token_test.txt
    del temp_token_test.txt
    pause
    exit /b 1
) else (
    echo ✅ Token válido y funcionando
    del temp_token_test.txt
)

REM Probar permisos
echo 🔧 3. Verificando permisos...
curl -s -H "Authorization: Bearer sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6" http://localhost:9000/api/projects/search > temp_permissions.txt
if errorlevel 1 (
    echo ❌ Error verificando permisos
    del temp_permissions.txt
    pause
    exit /b 1
) else (
    echo ✅ Permisos correctos
    del temp_permissions.txt
)

echo.
echo ✅ TOKEN FUNCIONANDO CORRECTAMENTE!
echo.
echo 🚀 Opciones disponibles:
echo    1. Verificación completa: .\verify-sonarqube.bat
echo    2. Verificación rápida: .\quick-sonar-check.bat
echo    3. Análisis completo: .\run-sonar-complete.bat
echo    4. Análisis directo: npm run sonar:token
echo.
echo 📊 Resultados en: http://localhost:9000/dashboard?id=kartfront
echo.

echo ¿Ejecutar verificación completa ahora? (s/n)
set /p run_verify=
if /i "%run_verify%"=="s" (
    echo.
    echo 🔍 Ejecutando verificación completa...
    call .\verify-sonarqube.bat
)

pause
