# Redis API - Spring Boot 3.4.5 con Rate Limiting

Proyecto de Spring Boot 3.4.5 con Java 17 que implementa un sistema de rate limiting usando Redis como almacenamiento distribuido.

## 🚀 Características

- **Spring Boot 3.4.5** con Java 17
- **Rate Limiting** configurable por endpoint y por IP
- **Redis** como almacenamiento para contadores de rate limiting
- **Configuración dinámica** en tiempo de ejecución
- **Spring Actuator** para monitoreo y health checks
- **Gradle** como build tool
- **Endpoints REST** para health, tiempo, conversión de moneda y administración

## 🏗️ Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/miempresa/redis/
│   │       ├── RedisApplication.java          # Clase principal de la aplicación
│   │       ├── config/
│   │       │   ├── RedisConfig.java          # Configuración de Redis
│   │       │   └── WebConfig.java            # Configuración web e interceptores
│   │       ├── controller/
│   │       │   ├── HealthController.java     # Endpoint de health check
│   │       │   ├── TimeController.java       # Endpoint de tiempo actual
│   │       │   ├── CurrencyController.java   # Endpoint de conversión de moneda
│   │       │   └── RateLimitConfigController.java # Gestión de configuración
│   │       ├── service/
│   │       │   └── RateLimitService.java     # Lógica de rate limiting
│   │       ├── interceptor/
│   │       │   └── RateLimitInterceptor.java # Interceptor para rate limiting
│   │       ├── dto/
│   │       │   └── EndpointConfigDTO.java    # DTO para configuración
│   │       └── util/
│   │           └── UrlUtils.java             # Utilidades para manejo de URLs
│   └── resources/
│       └── application.yml                   # Configuración de la aplicación
```

## 📋 Endpoints Disponibles

### Endpoints Públicos

#### GET /api/health

Endpoint de health check con rate limiting (5 requests/minuto por IP)

```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:45.123",
  "message": "Redis API is running successfully",
  "version": "1.0.0"
}
```

#### GET /api/time

Endpoint de tiempo actual (sin rate limiting)

```json
{
  "currentTime": "2025-08-05 11:55:30",
  "timestamp": "2025-08-05T11:55:30.123",
  "timezone": "Local",
  "message": "Current server time"
}
```

#### GET /api/currency/convert

Endpoint de conversión de moneda (configurable)

- **Parámetros:**
  - `amount`: Cantidad a convertir
  - `rate`: Tasa de cambio (opcional, default: 3.8)

### Endpoints de Administración

#### GET /api/admin/rate-limit/config/

Consultar configuración de rate limiting para un endpoint específico

#### POST /api/admin/rate-limit/config

Actualizar configuración de rate limiting

- **Parámetros:**
  - `endpoint`: Endpoint a configurar
  - `maxRequests`: Número máximo de requests permitidos
  - `timeWindowSeconds`: Ventana de tiempo en segundos
  - `enabled`: Habilitar/deshabilitar rate limiting

### Endpoints de Actuator

#### GET /actuator/health

Health check de Spring Actuator

#### GET /actuator/info

Información de la aplicación

## ⚙️ Configuración

### Archivo application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: redis-api
  data:
    redis:
      host: localhost
      port: 6379

rate-limit:
  endpoints:
    "/api/health":
      max-requests: 5
      time-window-seconds: 60
      enabled: true
```

### Variables de Entorno

- `SERVER_PORT`: Puerto del servidor (default: 8080)
- `SPRING_PROFILES_ACTIVE`: Perfil activo (default: default)
- `SPRING_REDIS_HOST`: Host de Redis (default: localhost)
- `SPRING_REDIS_PORT`: Puerto de Redis (default: 6379)

## 🚀 Cómo Ejecutar

### Prerrequisitos

- Java 17 o superior
- Redis ejecutándose en localhost:6379
- Gradle 7.x o superior

### Desarrollo Local

#### 1. Iniciar Redis

```bash
# macOS (con Homebrew)
brew services start redis

# Docker
docker run -d -p 6379:6379 redis:latest

# Linux
sudo systemctl start redis
```

#### 2. Ejecutar la Aplicación

```bash
# Compilar y ejecutar
./gradlew bootRun

# O compilar y ejecutar el JAR
./gradlew clean build
java -jar build/libs/redis-1.0.0.jar
```

### Docker

```bash
# Construir imagen
docker build -t redis-api .

# Ejecutar contenedor
docker run -p 8080:8080 --network host redis-api
```

## 🧪 Pruebas

### Archivo .http

El proyecto incluye un archivo `.http` con ejemplos de requests para probar todos los endpoints:

```bash
# Health check
curl http://localhost:8080/api/health

# Tiempo actual
curl http://localhost:8080/api/time

# Configurar rate limiting
curl -X POST "http://localhost:8080/api/admin/rate-limit/config?endpoint=/api/time&maxRequests=3&timeWindowSeconds=60&enabled=true"

# Consultar configuración
curl "http://localhost:8080/api/admin/rate-limit/config/%2Fapi%2Ftime"
```

### Prueba de Rate Limiting

```bash
# Probar rate limiting en /api/health (5 requests/minuto)
for i in {1..10}; do
  curl http://localhost:8080/api/health
  echo "Request $i"
done
# Después del 5to request, recibirás error 409 (Rate limit exceeded)
```

## 🔧 Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.4.5**
- **Spring Web**
- **Spring Data Redis**
- **Spring Actuator**
- **Gradle**
- **Redis**
- **Lombok**

## 📚 Documentación Adicional

- [README_RATE_LIMIT.md](README_RATE_LIMIT.md) - Documentación detallada del sistema de rate limiting
- [README_GRADLE_MIGRATION.md](README_GRADLE_MIGRATION.md) - Notas sobre migración a Gradle

## 🎯 Funcionalidades del Rate Limiting

- ✅ **Rate limiting por IP** usando Redis
- ✅ **Configuración dinámica** en tiempo de ejecución
- ✅ **Configuración por endpoint** específico
- ✅ **Almacenamiento distribuido** en Redis
- ✅ **Respuesta HTTP 409** cuando se excede el límite
- ✅ **Interceptores** para aplicar rate limiting automáticamente
- ✅ **Utilidades** para normalización de URLs

## 🔮 Próximos Pasos

1. **Tests unitarios** e integración
2. **Métricas** con Prometheus
3. **Logging estructurado** con JSON
4. **Autenticación** con JWT
5. **Documentación API** con OpenAPI/Swagger
6. **Monitoreo** con Grafana
7. **CI/CD** pipeline
