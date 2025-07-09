# 🏁 Guía Completa SonarQube - Karting Frontend

## 📋 Prerequisitos

### 1. SonarQube Server
- **Descargar**: https://www.sonarqube.org/downloads/
- **Ubicación**: Instalar en una carpeta (ej: `C:\sonarqube`)
- **Iniciar**: Ejecutar `bin\windows-x86-64\StartSonar.bat`
- **Verificar**: Abrir http://localhost:9000
- **Login**: admin/admin (cambiar en primer uso)

### 2. SonarQube Scanner
- **Opción 1**: `npm install -g sonar-scanner`
- **Opción 2**: Ejecutar `.\install-sonar-scanner.bat`
- **Verificar**: `sonar-scanner -h`

## 🚀 Ejecución del Análisis

### Verificación Previa (Recomendado)
```bash
# Verificación completa (10 pasos)
.\verify-sonarqube.bat

# Verificación rápida (solo lo esencial)
.\quick-sonar-check.bat
```

### Método Recomendado (Completo)
```bash
.\run-sonar-complete.bat
```

### Métodos Alternativos
```bash
# Método 1: Con verificación previa
npm run sonar:verify

# Método 2: Con npm
npm run sonar:complete

# Método 3: Simple
npm run sonar:token

# Método 4: Directo
sonar-scanner
```

## 📊 Verificar Resultados

1. **Abrir SonarQube**: http://localhost:9000
2. **Buscar proyecto**: `kartfront`
3. **Ver dashboard**: http://localhost:9000/dashboard?id=kartfront

## 🎯 Métricas Analizadas

### Calidad del Código
- **Bugs**: Errores que pueden causar problemas
- **Vulnerabilidades**: Problemas de seguridad
- **Code Smells**: Problemas de mantenibilidad

### Cobertura y Tests
- **Cobertura**: Porcentaje de código cubierto por tests
- **Tests**: Cantidad y estado de las pruebas

### Complejidad
- **Complejidad Ciclomática**: Complejidad del código
- **Duplicación**: Código duplicado

## 📁 Estructura Analizada

```
src/
├── components/
│   ├── Calendario.jsx     ✅ Análisis completo
│   ├── Carros.jsx         ✅ Análisis completo
│   ├── Clientes.jsx       ✅ Análisis completo
│   ├── Comprobante.jsx    ✅ Análisis completo
│   ├── Reportes.jsx       ✅ Análisis completo
│   ├── Reservar.jsx       ✅ Análisis completo
│   └── Tarifas.jsx        ✅ Análisis completo
├── services/
│   ├── clienteService.js  ✅ Análisis completo
│   ├── comprobantepago.service.js ✅ Análisis completo
│   ├── kart.service.js    ✅ Análisis completo
│   ├── reporte.service.js ✅ Análisis completo
│   ├── reserva.service.js ✅ Análisis completo
│   └── tarifa.service.js  ✅ Análisis completo
├── App.jsx                ✅ Análisis completo
├── main.jsx               ✅ Análisis completo
└── http-common.js         ✅ Análisis completo
```

## 🔧 Solución de Problemas

### 🔍 Script de Verificación Completa
El script `verify-sonarqube.bat` verifica automáticamente:
1. ✅ SonarQube Server (puerto 9000)
2. ✅ Token de autenticación
3. ✅ Configuración del proyecto
4. ✅ Node.js y npm
5. ✅ Dependencias del proyecto
6. ✅ SonarQube Scanner
7. ✅ ESLint
8. ✅ Archivos fuente
9. ✅ Prueba de ESLint
10. ✅ Conexión completa

### Error: "SonarQube no está ejecutándose"
1. Navegar a la carpeta de SonarQube
2. Ejecutar `bin\windows-x86-64\StartSonar.bat`
3. Esperar mensaje "SonarQube is up"
4. Verificar en http://localhost:9000

### Error: "Token inválido"
1. Ir a http://localhost:9000
2. Login con admin/admin
3. Usuario > Mi Cuenta > Seguridad
4. Generar nuevo token
5. Actualizar `sonar-project.properties`

### Error: "sonar-scanner no encontrado"
1. Ejecutar `.\install-sonar-scanner.bat`
2. O manualmente: `npm install -g sonar-scanner`
3. Verificar: `sonar-scanner -h`

### Error: "Dependencias faltantes"
1. Ejecutar `npm install`
2. Verificar `node_modules` existe
3. Ejecutar análisis nuevamente

## 📈 Interpretar Resultados

### Rating de Calidad
- **A**: Excelente (0-5% de problemas)
- **B**: Bueno (6-10% de problemas)
- **C**: Regular (11-20% de problemas)
- **D**: Pobre (21-50% de problemas)
- **E**: Muy pobre (>50% de problemas)

### Acciones Recomendadas
1. **Corregir Bugs**: Prioridad alta
2. **Resolver Vulnerabilidades**: Prioridad alta
3. **Mejorar Code Smells**: Prioridad media
4. **Reducir Duplicación**: Prioridad media
5. **Aumentar Cobertura**: Prioridad baja

## 🎯 Configuración del Proyecto

### Token Actual
```
sqp_cf490bce8d752bcddccd6f3b96ecb867ba3129d6
```

### Project Key
```
kartfront
```

### URL del Dashboard
```
http://localhost:9000/dashboard?id=kartfront
```

## 📞 Soporte

Si tienes problemas:
1. Revisar logs de SonarQube en `logs/sonar.log`
2. Verificar que todos los prerequisitos estén instalados
3. Ejecutar `.\run-sonar-complete.bat` para diagnóstico completo
