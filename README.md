# Redis API - Spring Boot 3.4.5 con Rate Limiting, WebFlux y Redisson

Proyecto de Spring Boot 3.4.5 con Java 17 que implementa un sistema de rate limiting usando Redis como almacenamiento distribuido, con **arquitectura hexagonal**, **programación reactiva con Spring WebFlux**, **Redisson para funcionalidades avanzadas** y soporte para **Gradle y Maven**.

## 🚀 Características

- **Spring Boot 3.4.5** con Java 17
- **Spring WebFlux** para programación reactiva y no bloqueante
- **Arquitectura Hexagonal** (Ports & Adapters) para mejor separación de responsabilidades
- **Redisson** para funcionalidades avanzadas de Redis (locks distribuidos, cache distribuido)
- **Rate Limiting** configurable por endpoint y por IP con control de concurrencia distribuido
- **Redis** como almacenamiento para contadores de rate limiting
- **Configuración dinámica** en tiempo de ejecución
- **Spring Actuator** para monitoreo y health checks
- **Soporte dual de build**: **Gradle** (principal) y **Maven** (alternativo)
- **Endpoints REST reactivos** para health, tiempo, conversión de moneda y administración
- **Programación reactiva** con Project Reactor (Mono, Flux)
- **Fallback automático** entre implementación distribuida y original

## 🏗️ Estructura del Proyecto

### Arquitectura Hexagonal con Redisson

```
src/
├── main/
│   ├── java/
│   │   └── com/miempresa/redis/
│   │       ├── RedisApplication.java                    # Clase principal de la aplicación
│   │       ├── domain/                                 # Capa de dominio
│   │       │   ├── model/                              # Modelos de dominio
│   │       │   │   ├── RateLimitConfig.java            # Configuración de rate limiting
│   │       │   │   ├── RequestInfo.java                # Información de request
│   │       │   │   └── CurrencyConversion.java         # Modelo de conversión de moneda
│   │       │   └── service/                            # Servicios de dominio
│   │       │       ├── UrlNormalizationService.java    # Interfaz de normalización
│   │       │       └── impl/
│   │       │           └── UrlNormalizationServiceImpl.java
│   │       ├── application/                            # Capa de aplicación
│   │       │   ├── port/
│   │       │   │   ├── in/                             # Puertos de entrada (use cases)
│   │       │   │   │   ├── RateLimitUseCase.java
│   │       │   │   │   └── CurrencyConversionUseCase.java
│   │       │   │   └── out/                            # Puertos de salida
│   │       │   │       ├── RateLimitPersistencePort.java
│   │       │   │       ├── DistributedLockPort.java    # Puerto para locks distribuidos
│   │       │   │       ├── DistributedCachePort.java   # Puerto para cache distribuido
│   │       │   │       ├── HealthCheckPort.java        # Puerto para health checks
│   │       │   │       └── ExchangeRateProviderPort.java
│   │       │   └── service/                            # Servicios de aplicación
│   │       │       ├── RateLimitService.java           # Implementación original
│   │       │       ├── RedissonRateLimitService.java   # Implementación con Redisson
│   │       │       ├── RateLimitServiceFactory.java    # Factory para selección de servicio
│   │       │       ├── CurrencyConversionService.java  # Servicio de conversión de moneda
│   │       │       └── DefaultExchangeRateProvider.java
│   │       └── infrastructure/                         # Capa de infraestructura
│   │           ├── adapter/
│   │           │   ├── in/                              # Adaptadores de entrada
│   │           │   │   └── web/
│   │           │   │       ├── controller/             # Controladores REST reactivos
│   │           │   │       │   ├── HealthController.java
│   │           │   │       │   ├── TimeController.java
│   │           │   │       │   ├── CurrencyController.java
│   │           │   │       │   └── RateLimitConfigController.java
│   │           │   │       ├── interceptor/            # Filtros WebFlux
│   │           │   │       │   └── RateLimitWebFilter.java
│   │           │   │       └── util/                   # Utilidades web
│   │           │           ├── ClientIpExtractor.java
│   │           │           └── UrlUtils.java
│   │           │   └── out/                             # Adaptadores de salida
│   │           │       ├── persistence/                # Persistencia
│   │           │       │   ├── redis/                  # Adaptador Redis
│   │           │       │   │   └── RedisRateLimitPersistenceAdapter.java
│   │           │       │   └── InitialDataService.java # Inicialización de datos
│   │           │       ├── redisson/                   # Adaptadores Redisson
│   │           │       │   ├── RedissonDistributedLockAdapter.java
│   │           │       │   ├── RedissonDistributedCacheAdapter.java
│   │           │       │   └── RedissonHealthCheckAdapter.java
│   │           │       └── exchange/                   # Proveedores de tasas de cambio
│   │           │           └── DefaultExchangeRateProvider.java
│   │           └── config/                              # Configuraciones
│   │               ├── RedisConfig.java                 # Configuración de Redis
│   │               ├── RedissonConfig.java              # Configuración de Redisson
│   │               └── WebConfig.java                   # Configuración web WebFlux
│   └── resources/
│       └── application.yml                              # Configuración de la aplicación
```

### Archivos de Build

```
├── build.gradle              # Configuración Gradle
├── pom.xml                   # Configuración Maven
├── gradlew                   # Gradle Wrapper (Unix/macOS)
├── gradlew.bat              # Gradle Wrapper (Windows)
├── mvnw                     # Maven Wrapper (Unix/macOS)
├── mvnw.cmd                 # Maven Wrapper (Windows)
├── build.sh                 # Script de conveniencia para ambos sistemas
├── .mvn/                    # Configuración Maven
│   └── wrapper/
│       └── maven-wrapper.properties
└── gradle/                  # Configuración Gradle
    └── wrapper/
        └── gradle-wrapper.properties
```

## 🆕 Nuevas Funcionalidades con Redisson

### 🔒 **Locks Distribuidos**
- **Control de concurrencia** en múltiples instancias de la aplicación
- **Prevención de condiciones de carrera** durante rate limiting
- **Locks atómicos** para actualización de configuraciones
- **Timeout configurable** para evitar deadlocks

### 💾 **Cache Distribuido**
- **Cache compartido** entre múltiples instancias
- **Configuraciones de rate limiting** en memoria distribuida
- **Reducción de latencia** en consultas frecuentes
- **Consistencia de datos** en entornos distribuidos

### 🏥 **Health Checks Avanzados**
- **Monitoreo de estado** de servicios distribuidos
- **Fallback automático** a implementación original
- **Métricas de Redis** (total de claves, estado de conexión)
- **Detección automática** de problemas de conectividad

### 🔄 **Factory Pattern Inteligente**
- **Selección automática** del servicio apropiado
- **Fallback transparente** en caso de fallos
- **Configuración dinámica** de servicios
- **Monitoreo continuo** del estado de salud

## 🛠️ Sistemas de Build

### 🚀 Gradle (Sistema Principal)

```bash
# Compilar el proyecto
./gradlew clean compileJava

# Ejecutar tests
./gradlew test

# Construir el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun
```

### 📦 Maven (Sistema Alternativo)

```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Construir el proyecto
./mvnw clean package

# Ejecutar la aplicación
./mvnw spring-boot:run
```

### 🔧 Script de Conveniencia

```bash
# Ver estado de ambos sistemas
./build.sh status

# Comparar dependencias
./build.sh compare

# Usar Gradle (por defecto)
./build.sh clean
./build.sh compile

# Usar Maven
./build.sh maven clean
./build.sh maven compile
```

**💡 Ventaja**: Puedes elegir tu sistema de build preferido o usar ambos en el mismo proyecto.

## 📋 Endpoints Disponibles

### Endpoints Públicos

#### GET /api/health

Endpoint de health check con rate limiting (5 requests/minuto por IP)

```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:45.123",
  "message": "Redis API is running successfully",
  "version": "1.0.0",
  "redissonStatus": "UP"
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

#### GET /api/currency/convert/pen-to-usd

Conversión específica de PEN a USD

#### GET /api/currency/convert/usd-to-pen

Conversión específica de USD a PEN

#### GET /api/currency/exchange-rate

Consulta de tasas de cambio

- **Parámetros:**
  - `from`: Moneda origen
  - `to`: Moneda destino

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

#### POST /api/admin/rate-limit/fallback

Forzar el uso del servicio de fallback (implementación original)

#### POST /api/admin/rate-limit/redisson

Forzar el uso del servicio de Redisson (implementación distribuida)

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

redisson:
  connection-pool-size: 64
  connection-minimum-idle: 24
  lock-watchdog-timeout: 30000
  rate-limit:
    lock-timeout: 5000
    cache-ttl: 600

rate-limit:
  endpoints:
    "/api/health":
      max-requests: 5
      time-window-seconds: 60
      enabled: true
      lock-timeout: 5000
```

### Variables de Entorno

- `SERVER_PORT`: Puerto del servidor (default: 8080)
- `SPRING_PROFILES_ACTIVE`: Perfil activo (default: default)
- `SPRING_REDIS_HOST`: Host de Redis (default: localhost)
- `SPRING_REDIS_PORT`: Puerto de Redis (default: 6379)
- `REDISSON_CONNECTION_POOL_SIZE`: Tamaño del pool de conexiones (default: 64)
- `REDISSON_LOCK_WATCHDOG_TIMEOUT`: Timeout del watchdog de locks (default: 30000)

## 🚀 Cómo Ejecutar

### Prerrequisitos

- Java 17 o superior
- Redis ejecutándose en localhost:6379
- **Gradle 7.x** o superior (para build con Gradle)
- **Maven 3.6.x** o superior (para build con Maven)

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

##### Con Gradle (Recomendado)

```bash
# Compilar y ejecutar
./gradlew bootRun

# O compilar y ejecutar el JAR
./gradlew clean build
java -jar build/libs/redis-1.0.0.jar
```

##### Con Maven

```bash
# Compilar y ejecutar
./mvnw spring-boot:run

# O compilar y ejecutar el JAR
./mvnw clean package
java -jar target/redis-rate-limit-1.0.0.jar
```

##### Con Script de Conveniencia

```bash
# Usar Gradle (por defecto)
./build.sh run

# Usar Maven
./build.sh maven run
```

### Docker

```bash
# Construir imagen (con Gradle)
docker build -t redis-api .

# O construir imagen (con Maven)
docker build -f Dockerfile.maven -t redis-api .

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

# Conversión de moneda
curl "http://localhost:8080/api/currency/convert?amount=100&rate=3.8"

# Conversión específica PEN a USD
curl "http://localhost:8080/api/currency/convert/pen-to-usd?amount=100"

# Consultar tasa de cambio
curl "http://localhost:8080/api/currency/exchange-rate?from=PEN&to=USD"

# Configurar rate limiting
curl -X POST "http://localhost:8080/api/admin/rate-limit/config?endpoint=/api/time&maxRequests=3&timeWindowSeconds=60&enabled=true"

# Consultar configuración
curl "http://localhost:8080/api/admin/rate-limit/config/%2Fapi%2Ftime"

# Forzar fallback
curl -X POST "http://localhost:8080/api/admin/rate-limit/fallback"

# Forzar Redisson
curl -X POST "http://localhost:8080/api/admin/rate-limit/redisson"
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
- **Spring WebFlux** (programación reactiva)
- **Spring Data Redis Reactive**
- **Spring Actuator**
- **Project Reactor** (Mono, Flux)
- **Redisson 3.24.3** (cliente Redis avanzado)
- **Gradle 8.5** (sistema principal)
- **Maven 3.9.6** (sistema alternativo)
- **Redis**
- **Lombok**

## 🏗️ Arquitectura Hexagonal Implementada

### **Capa de Dominio**
- **Modelos**: `RateLimitConfig`, `RequestInfo`, `CurrencyConversion`
- **Servicios**: `UrlNormalizationService`

### **Capa de Aplicación**
- **Puertos de Entrada**: `RateLimitUseCase`, `CurrencyConversionUseCase`
- **Puertos de Salida**: `DistributedLockPort`, `DistributedCachePort`, `HealthCheckPort`
- **Servicios**: `RedissonRateLimitService`, `CurrencyConversionService`, `RateLimitServiceFactory`

### **Capa de Infraestructura**
- **Adaptadores de Entrada**: Controladores REST, Filtros WebFlux
- **Adaptadores de Salida**: `RedissonDistributedLockAdapter`, `RedissonDistributedCacheAdapter`

## 📚 Documentación Adicional

- [README_RATE_LIMIT.md](README_RATE_LIMIT.md) - Documentación detallada del sistema de rate limiting
- [BUILD_SYSTEMS.md](BUILD_SYSTEMS.md) - Guía completa de sistemas de build duales (Gradle + Maven)
- [ARCHITECTURE.md](ARCHITECTURE.md) - Documentación de la arquitectura hexagonal implementada
- [WEBFLUX_MIGRATION_SUMMARY.md](WEBFLUX_MIGRATION_SUMMARY.md) - Resumen de la migración a Spring WebFlux
- [REDISSON_IMPLEMENTATION.md](REDISSON_IMPLEMENTATION.md) - Documentación de la implementación de Redisson

## 🎯 Funcionalidades del Rate Limiting

- ✅ **Rate limiting por IP** usando Redis
- ✅ **Configuración dinámica** en tiempo de ejecución
- ✅ **Configuración por endpoint** específico
- ✅ **Almacenamiento distribuido** en Redis
- ✅ **Locks distribuidos** para control de concurrencia
- ✅ **Cache distribuido** para configuraciones
- ✅ **Fallback automático** entre implementaciones
- ✅ **Respuesta HTTP 409** cuando se excede el límite
- ✅ **Filtros WebFlux** para aplicar rate limiting automáticamente
- ✅ **Utilidades** para normalización de URLs
- ✅ **Arquitectura reactiva** con Mono/Flux
- ✅ **Health checks avanzados** para servicios distribuidos

## 🚀 Ventajas de Spring WebFlux + Redisson

- **Programación no bloqueante** para mejor rendimiento
- **Escalabilidad mejorada** con menos threads
- **Backpressure handling** con Project Reactor
- **Compatibilidad con Spring 6** y Java 17+
- **Arquitectura reactiva** end-to-end
- **Mejor manejo de concurrencia** para aplicaciones de alto tráfico
- **Locks distribuidos** para entornos multi-instancia
- **Cache distribuido** para mejor rendimiento
- **Fallback automático** para alta disponibilidad
- **Monitoreo avanzado** de servicios distribuidos

## 🔒 Beneficios de Redisson

- **Locks distribuidos** para sincronización entre instancias
- **Cache distribuido** para compartir datos entre nodos
- **Health checks** para monitoreo de servicios
- **Fallback automático** en caso de fallos
- **Configuración flexible** de timeouts y pools
- **Integración nativa** con Spring Boot
- **Soporte para clusters** de Redis
- **Manejo automático** de reconexiones
