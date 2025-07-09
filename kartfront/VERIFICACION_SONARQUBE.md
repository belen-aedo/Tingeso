# 🔍 Verificación de SonarQube - Karting Frontend

## 🚀 Uso Rápido

### Verificación Completa
```bash
.\verify-sonarqube.bat
```
**Verifica:** 10 aspectos críticos del sistema

### Verificación Rápida
```bash
.\quick-sonar-check.bat
```
**Verifica:** Solo lo esencial para ejecutar el análisis

## 📋 Qué Verifica

### 🔍 Verificación Completa (10 pasos)
1. **SonarQube Server** - Puerto 9000, respuesta HTTP, API status
2. **Token de Autenticación** - Validez y permisos
3. **Configuración del Proyecto** - sonar-project.properties, projectKey
4. **Node.js y npm** - Versiones instaladas
5. **Dependencias** - node_modules, package.json
6. **SonarQube Scanner** - Instalación y disponibilidad
7. **ESLint** - Configuración y dependencias
8. **Archivos Fuente** - Estructura src/, componentes, servicios
9. **Prueba ESLint** - Ejecución de linting
10. **Conexión Completa** - Verificación de proyecto en SonarQube

### ⚡ Verificación Rápida (4 pasos)
1. **SonarQube Server** - Disponibilidad básica
2. **Token** - Validez simple
3. **Scanner** - Comando disponible
4. **Archivos** - Configuración y fuentes

## 📊 Resultados

### ✅ Sin Errores
```
🏁 ESTADO: LISTO PARA ANALISIS
   Errores críticos: 0
   Advertencias: 0
```

### ❌ Con Errores
```
🏁 ESTADO: ERRORES ENCONTRADOS
   Errores críticos: 2
   Advertencias: 1
```

## 🔧 Soluciones Automáticas

### Script Completo
- **Instala dependencias** si faltan
- **Instala sonar-scanner** si no está disponible
- **Ofrece ejecutar análisis** si todo está bien

### Script Rápido
- **Verifica lo esencial** en pocos segundos
- **Ofrece ejecutar análisis** inmediatamente
- **Abre resultados** en navegador

## 🎯 Flujo Recomendado

```bash
# 1. Verificar que todo esté bien
.\verify-sonarqube.bat

# 2. Si hay errores, corregirlos siguiendo las instrucciones

# 3. Ejecutar análisis completo
.\run-sonar-complete.bat

# 4. Ver resultados en:
# http://localhost:9000/dashboard?id=kartfront
```

## 🚨 Errores Comunes

### "SonarQube no está ejecutándose"
**Solución:** Iniciar SonarQube
```bash
# Navegar a carpeta SonarQube
cd C:\sonarqube
# Ejecutar
bin\windows-x86-64\StartSonar.bat
```

### "Token inválido"
**Solución:** Generar nuevo token
1. Ir a http://localhost:9000
2. Login: admin/admin
3. Usuario > Mi Cuenta > Seguridad
4. Generar nuevo token

### "sonar-scanner no encontrado"
**Solución:** Instalación automática
```bash
# El script lo instala automáticamente
npm install -g sonar-scanner
```

## 📈 Métricas del Proyecto

### Archivos Analizados
- **Componentes:** 7 archivos .jsx
- **Servicios:** 6 archivos .js
- **Configuración:** App.jsx, main.jsx, http-common.js

### Configuración Actual
- **Project Key:** kartfront
- **Token:** sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6
- **URL:** http://localhost:9000/dashboard?id=kartfront
