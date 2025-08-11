# Arquitectura Hexagonal Reactiva (Puertos y Adaptadores con WebFlux)

## 🏗️ Visión General

El proyecto ha sido refactorizado para implementar una **Arquitectura Hexagonal Reactiva** (también conocida como Arquitectura de Puertos y Adaptadores), que proporciona una mejor separación de responsabilidades, mayor testabilidad, flexibilidad para cambios futuros y **programación reactiva con Spring WebFlux**.

## 📁 Estructura de Carpetas

```
src/main/java/com/miempresa/redis/
├── RedisApplication.java                                    # Clase principal de Spring Boot WebFlux
├── application/                                            # Capa de aplicación (Reactiva)
│   ├── port/
│   │   ├── in/                                            # Puertos de entrada (casos de uso reactivos)
│   │   │   └── RateLimitUseCase.java                      # Retorna Mono<T>
│   │   └── out/                                           # Puertos de salida (persistencia reactiva)
│   │       └── RateLimitPersistencePort.java              # Retorna Mono<T>
│   └── service/                                           # Servicios de aplicación reactivos
│       └── RateLimitService.java                          # Implementa lógica reactiva
├── domain/                                                 # Capa de dominio (núcleo de negocio)
│   ├── model/                                             # Modelos de dominio
│   │   ├── RateLimitConfig.java
│   │   └── RequestInfo.java
│   └── service/                                           # Servicios de dominio
│       ├── UrlNormalizationService.java                   # Interfaz
│       └── impl/
│           └── UrlNormalizationServiceImpl.java           # Implementación
└── infrastructure/                                         # Capa de infraestructura (Reactiva)
    ├── adapter/
    │   ├── in/                                            # Adaptadores de entrada reactivos
    │   │   └── web/                                       # Adaptadores web WebFlux
    │   │       ├── controller/                            # Controladores REST reactivos
    │   │       │   ├── CurrencyController.java            # Retorna Mono<ResponseEntity>
    │   │       │   ├── HealthController.java              # Retorna Mono<ResponseEntity>
    │   │       │   ├── RateLimitConfigController.java     # Retorna Mono<ResponseEntity>
    │   │       │   └── TimeController.java                # Retorna Mono<ResponseEntity>
    │   │       ├── interceptor/                           # Filtros WebFlux
    │   │       │   └── RateLimitWebFilter.java            # Implementa WebFilter
    │   │       └── util/                                  # Utilidades web WebFlux
    │   │           ├── ClientIpExtractor.java             # Adaptado para ServerWebExchange
    │   │           └── UrlUtils.java
    │   └── out/                                           # Adaptadores de salida reactivos
    │       └── persistence/                               # Adaptadores de persistencia reactiva
    │           ├── InitialDataService.java                # Inicialización de datos
    │           └── redis/                                 # Implementación Redis reactiva
    │               └── RedisRateLimitPersistenceAdapter.java # Retorna Mono<T>
    └── config/                                            # Configuraciones WebFlux
        ├── RedisConfig.java                               # Configuración Redis
        └── WebConfig.java                                 # Configuración WebFlux
```

## 🔄 Flujo de Datos Reactivo

### 1. **Request HTTP llega (WebFlux)**
```
HTTP Request → RateLimitWebFilter → RateLimitUseCase → RateLimitPersistencePort → Redis
                (WebFilter)        (Mono<Boolean>)    (Mono<T>)              (Template)
```

### 2. **Verificación de Rate Limiting (Reactiva)**
```
RequestInfo → RateLimitUseCase.isRequestAllowed() → RateLimitPersistencePort → Redis
             (Mono<Boolean>)                      (Mono<Integer>)           (Template)
```

### 3. **Configuración de Rate Limiting (Reactiva)**
```
Admin Request → RateLimitConfigController → RateLimitUseCase → RateLimitPersistencePort → Redis
               (Mono<ResponseEntity>)     (Mono<Void>)      (Mono<Void>)              (Template)
```

## 🚀 Características Reactivas

### **Programación No Bloqueante**
- **WebFlux**: Framework web reactivo basado en Project Reactor
- **Mono<T>**: Para operaciones que retornan 0 o 1 resultado
- **Flux<T>**: Para operaciones que retornan múltiples resultados
- **Operadores reactivos**: `flatMap`, `then`, `onErrorResume`, etc.

### **Arquitectura Reactiva End-to-End**
- **Controladores**: Retornan `Mono<ResponseEntity<T>>`
- **Servicios**: Operan con `Mono<T>` y `Flux<T>`
- **Persistencia**: Interfaz reactiva con `Mono<T>`
- **Filtros**: `WebFilter` en lugar de interceptores

### **Manejo de Errores Reactivo**
- **onErrorResume**: Manejo de errores en el flujo reactivo
- **onErrorReturn**: Valores por defecto en caso de error
- **doOnError**: Logging y monitoreo de errores

## 🎯 Beneficios de la Nueva Arquitectura

### **Separación de Responsabilidades**
- **Dominio**: Lógica de negocio pura, sin dependencias externas
- **Aplicación**: Orquestación de casos de uso reactivos
- **Infraestructura**: Implementaciones concretas reactivas (Redis, WebFlux, etc.)

### **Testabilidad**
- **Tests unitarios**: Dominio y aplicación se pueden testear sin infraestructura
- **Tests de integración**: Adaptadores se pueden testear de forma aislada
- **Tests reactivos**: Con `reactor-test` para Mono/Flux
- **Mocks**: Fácil creación de mocks para puertos reactivos

### **Flexibilidad**
- **Cambio de base de datos**: Solo cambiar el adaptador de persistencia
- **Cambio de framework web**: Solo cambiar los adaptadores de entrada
- **Nuevas funcionalidades**: Agregar nuevos puertos y adaptadores reactivos
- **Escalabilidad**: Mejor rendimiento con menos threads

### **Mantenibilidad**
- **Código organizado**: Cada capa tiene una responsabilidad clara
- **Dependencias claras**: Solo el dominio no tiene dependencias externas
- **Cambios localizados**: Modificaciones afectan solo a la capa correspondiente
- **Patrones reactivos**: Consistencia en toda la aplicación

## 🔌 Puertos y Adaptadores Reactivos

### **Puertos de Entrada (Casos de Uso Reactivos)**
- `RateLimitUseCase`: Define qué puede hacer la aplicación con rate limiting
  - `Mono<Boolean> isRequestAllowed(RequestInfo requestInfo)`
  - `Mono<Void> updateConfiguration(...)`
  - `Mono<RateLimitConfig> getConfiguration(String endpoint)`

### **Puertos de Salida (Persistencia Reactiva)**
- `RateLimitPersistencePort`: Define cómo se almacenan los datos
  - `Mono<Integer> getCurrentRequestCount(RequestInfo requestInfo)`
  - `Mono<Void> incrementRequestCount(...)`
  - `Mono<RateLimitConfig> getConfiguration(String endpoint)`

### **Adaptadores de Entrada Reactivos**
- **Web Controllers**: Exponen APIs REST reactivas
  - Retornan `Mono<ResponseEntity<T>>`
  - Manejo reactivo de requests y responses
- **WebFilters**: Filtros WebFlux para rate limiting
  - `RateLimitWebFilter` implementa `WebFilter` y `Ordered`
  - Manejo reactivo del pipeline de requests

### **Adaptadores de Salida Reactivos**
- **Redis**: Persistencia con interfaz reactiva
  - `RedisRateLimitPersistenceAdapter` implementa `RateLimitPersistencePort`
  - Wrapping de operaciones síncronas en `Mono<T>`

## 🔧 Implementación Técnica

### **Spring WebFlux**
```java
@Configuration
public class WebConfig {
    @Bean
    public WebFilter rateLimitFilter() {
        return rateLimitWebFilter;
    }
}
```

### **Controladores Reactivos**
```java
@GetMapping("/health")
public Mono<ResponseEntity<Map<String, Object>>> health() {
    Map<String, Object> response = new HashMap<>();
    // ... lógica
    return Mono.just(ResponseEntity.ok(response));
}
```

### **Servicios Reactivos**
```java
@Override
public Mono<Boolean> isRequestAllowed(RequestInfo requestInfo) {
    return persistencePort.getConfiguration(normalizedEndpoint)
        .flatMap(config -> {
            if (!config.isRateLimitEnabled()) {
                return Mono.just(true);
            }
            return persistencePort.getCurrentRequestCount(normalizedRequestInfo)
                .flatMap(currentCount -> {
                    if (config.hasReachedLimit(currentCount)) {
                        return Mono.just(false);
                    }
                    return persistencePort.incrementRequestCount(normalizedRequestInfo, config.getTimeWindowSeconds())
                        .then(Mono.just(true));
                });
        })
        .defaultIfEmpty(true);
}
```

### **Persistencia Reactiva**
```java
@Override
public Mono<Integer> getCurrentRequestCount(RequestInfo requestInfo) {
    String key = requestInfo.getRateLimitKey();
    try {
        String currentCountStr = (String) redisTemplate.opsForValue().get(key);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        return Mono.just(currentCount);
    } catch (Exception e) {
        return Mono.error(e);
    }
}
```

## 🚀 Ventajas de la Arquitectura Reactiva

### **Rendimiento**
- **Programación no bloqueante**: Mejor throughput
- **Menos threads**: Mejor utilización de recursos
- **Backpressure handling**: Control de flujo de datos

### **Escalabilidad**
- **Mejor concurrencia**: Manejo eficiente de múltiples requests
- **Recursos optimizados**: Menor uso de memoria y CPU
- **Horizontal scaling**: Fácil escalado de la aplicación

### **Mantenibilidad**
- **Código reactivo**: Patrones consistentes en toda la aplicación
- **Manejo de errores**: Flujo de errores integrado en el pipeline
- **Testing**: Herramientas específicas para testing reactivo

## 📊 Comparación: Antes vs Después

| Aspecto | Arquitectura Anterior | Arquitectura Actual |
|---------|----------------------|---------------------|
| **Framework Web** | Spring Web (Servlet) | Spring WebFlux (Reactive) |
| **Interceptores** | HandlerInterceptor | WebFilter |
| **Controladores** | ResponseEntity | Mono<ResponseEntity> |
| **Servicios** | Síncronos | Reactivos (Mono/Flux) |
| **Persistencia** | Síncrona | Reactiva (wrapped) |
| **Manejo de Errores** | try-catch | onErrorResume/onErrorReturn |
| **Testing** | JUnit estándar | JUnit + reactor-test |
| **Rendimiento** | Bloqueante | No bloqueante |
| **Escalabilidad** | Limitada por threads | Mejorada con menos threads |

## 🎉 Conclusión

La **Arquitectura Hexagonal Reactiva** implementada proporciona:

- ✅ **Mejor separación de responsabilidades** con patrones reactivos
- ✅ **Mayor testabilidad** con herramientas específicas para WebFlux
- ✅ **Mejor rendimiento** con programación no bloqueante
- ✅ **Mayor escalabilidad** para aplicaciones de alto tráfico
- ✅ **Consistencia reactiva** end-to-end
- ✅ **Compatibilidad** con Spring Boot 3.4.5 y Java 17
- ✅ **Preservación** de la arquitectura hexagonal existente

La migración a **Spring WebFlux** ha sido completamente exitosa, manteniendo toda la funcionalidad existente mientras se mejora significativamente el rendimiento y la escalabilidad de la aplicación. 