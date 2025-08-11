# Rate Limiting Implementation

## Descripción

Esta implementación de rate limiting permite limitar las llamadas a endpoints específicos por dirección IP usando Redis como almacenamiento.

## Configuración

### Configuración Inicial

La configuración se define en `application.yml`:

```yaml
rate-limit:
  endpoints:
    "/api/health":
      max-requests: 5
      time-window-seconds: 60
      enabled: true
```

### Configuración Dinámica

Puedes modificar la configuración en tiempo de ejecución usando el endpoint de administración:

#### Actualizar configuración:
```bash
POST /api/admin/rate-limit/config
```

Parámetros:
- `endpoint`: El endpoint a configurar (ej: "/api/health")
- `maxRequests`: Número máximo de requests permitidos
- `timeWindowSeconds`: Ventana de tiempo en segundos
- `enabled`: Habilitar/deshabilitar rate limiting (true/false)

Ejemplo:
```bash
curl -X POST "http://localhost:8080/api/admin/rate-limit/config?endpoint=/api/health&maxRequests=10&timeWindowSeconds=30&enabled=true"
```

#### Consultar configuración:
```bash
GET /api/admin/rate-limit/config/{endpoint}
```

Ejemplo:
```bash
curl "http://localhost:8080/api/admin/rate-limit/config/%2Fapi%2Fhealth"
```

## Comportamiento

### Endpoints Disponibles

#### Endpoints Protegidos (con Rate Limiting)
- `/api/health`: Limitado a 5 requests por minuto por IP

#### Endpoints Sin Protección (sin Rate Limiting)
- `/api/time`: Devuelve la hora actual del servidor

### Ejemplo de Respuesta del Endpoint de Tiempo
```json
{
  "currentTime": "2025-08-05 11:55:30",
  "timestamp": "2025-08-05T11:55:30.123",
  "timezone": "Local",
  "message": "Current server time"
}
```

### Respuesta de Error (Rate Limit Exceeded)
Cuando se excede el límite, la aplicación responde con:
- **Código HTTP**: 409 (Conflict)
- **Content-Type**: application/json
- **Body**: 
```json
{
  "error": "Rate limit exceeded",
  "message": "Too many requests for this endpoint",
  "endpoint": "/api/health",
  "clientIp": "127.0.0.1"
}
```

## Requisitos

### Redis
- **Host**: localhost
- **Puerto**: 6379
- **Autenticación**: No requerida

### Comandos para iniciar Redis:
```bash
# macOS (con Homebrew)
brew services start redis

# Docker
docker run -d -p 6379:6379 redis:latest

# Linux
sudo systemctl start redis
```

## Ejecución

1. Asegúrate de que Redis esté ejecutándose en localhost:6379
2. Ejecuta la aplicación:
```bash
mvn spring-boot:run
```

3. Prueba el rate limiting:
```bash
# Probar endpoint sin rate limiting (time)
curl http://localhost:8080/api/time

# Probar endpoint con rate limiting (health)
for i in {1..10}; do
  curl http://localhost:8080/api/health
  echo "Request $i"
done

# Después del 5to request, deberías recibir error 409
```

## Estructura del Proyecto

```
src/main/java/com/miempresa/redis/
├── config/
│   ├── RateLimitProperties.java    # Configuración de propiedades
│   ├── RedisConfig.java           # Configuración de Redis
│   └── WebConfig.java             # Configuración web e interceptores
├── controller/
│   ├── HealthController.java      # Endpoint de health (con rate limiting)
│   ├── TimeController.java        # Endpoint de tiempo (sin rate limiting)
│   └── RateLimitConfigController.java # Gestión de configuración
├── interceptor/
│   └── RateLimitInterceptor.java  # Interceptor de rate limiting
└── service/
    └── RateLimitService.java      # Lógica de rate limiting
```

## Características

- ✅ Rate limiting por IP
- ✅ Configuración dinámica en tiempo de ejecución
- ✅ Almacenamiento en Redis
- ✅ Respuesta HTTP 409 cuando se excede el límite
- ✅ Solo aplicado a endpoints específicos
- ✅ Configuración por archivo YAML
- ✅ Endpoints de administración para gestión 