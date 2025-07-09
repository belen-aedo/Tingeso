# Análisis SonarQube - Karting Frontend

## 🏁 Sistema de Arriendo de Karting

Este proyecto es un sistema frontend para el arriendo de karting desarrollado en React con Vite.

## 📋 Componentes Principales

- **Sistema de Clientes**: Gestión de usuarios y validación de RUT
- **Sistema de Reservas**: Manejo de reservas con validaciones de fechas y horarios
- **Sistema de Karting**: Gestión de vehículos y estados
- **Sistema de Reportes**: Generación de reportes mensuales e individuales
- **Sistema de Comprobantes**: Manejo de comprobantes de pago
- **Sistema de Tarifas**: Configuración de precios
- **Calendario**: Vista de reservas con funcionalidad de navegación

## 🔧 Configuración SonarQube

### Prerrequisitos

1. **SonarQube Server**: Debe estar ejecutándose en `http://localhost:9000`
2. **Node.js**: Versión 16+ instalada
3. **SonarQube Scanner**: Instalado globalmente
4. **Token de autenticación**: Generado desde SonarQube

### Instalación del Scanner

```bash
# Opción 1: NPM
npm install -g sonar-scanner

# Opción 2: Chocolatey (Windows)
choco install sonarscanner-msbuild-net46

# Opción 3: Descarga directa
# https://docs.sonarqube.org/latest/analysis/scan/sonarscanner/
```

### Uso

#### Windows
```bash
# Ejecutar análisis completo
.\run-sonar.bat "tu_token_aqui"

# O usar npm script
npm run sonar:windows "tu_token_aqui"
```

#### Linux/macOS
```bash
# Generar reporte ESLint
npm run lint:report

# Ejecutar análisis
sonar-scanner -Dsonar.login="tu_token_aqui"
```

## 📊 Métricas Analizadas

- **Calidad del código**: Bugs, vulnerabilidades, code smells
- **Cobertura**: Cobertura de tests (cuando estén disponibles)
- **Duplicación**: Código duplicado
- **Complejidad**: Complejidad ciclomática
- **Mantenibilidad**: Métricas de deuda técnica

## 🎯 Configuración Específica

### Archivos Incluidos
- `src/components/`: Todos los componentes React
- `src/services/`: Servicios de API
- `src/`: Archivos principales de la aplicación

### Archivos Excluidos
- `node_modules/`: Dependencias
- `dist/`, `build/`: Archivos de construcción
- `nginx/`: Configuración de servidor
- `public/`: Archivos estáticos
- `*.config.js`: Archivos de configuración

## 🚀 Resultados

Una vez completado el análisis, los resultados estarán disponibles en:
`http://localhost:9000/dashboard?id=kartfront`

## 📝 Notas Importantes

1. **Validaciones**: El proyecto incluye validaciones robustas para RUT, fechas, y números
2. **Manejo de errores**: Implementado manejo de errores consistente
3. **Experiencia de usuario**: Mensajes claros y feedback visual
4. **Consistencia**: Encabezados y estilos unificados en todos los componentes
