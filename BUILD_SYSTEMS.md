# Sistemas de Build Duales: Gradle y Maven

Este proyecto soporta tanto **Gradle** como **Maven** como sistemas de build, permitiendo a los desarrolladores elegir su herramienta preferida. **Ambos sistemas están configurados para Spring WebFlux y programación reactiva**.

## 🚀 Gradle (Sistema Principal)

### Comandos Básicos

```bash
# Compilar el proyecto
./gradlew clean compileJava

# Ejecutar tests
./gradlew test

# Construir el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun

# Limpiar build
./gradlew clean
```

### Ventajas
- Configuración más concisa y legible
- Mejor rendimiento en builds incrementales
- Soporte nativo para Spring Boot
- Wrapper incluido (`gradlew`)

## 📦 Maven (Sistema Alternativo)

### Comandos Básicos

```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Construir el proyecto
./mvnw clean package

# Ejecutar la aplicación
./mvnw spring-boot:run

# Limpiar build
./mvnw clean
```

### Ventajas
- Estándar de la industria Java
- Mejor integración con IDEs
- Sintaxis XML familiar para muchos desarrolladores
- Wrapper incluido (`mvnw`)

## 🔧 Configuración

### Dependencias
Ambos sistemas de build están configurados con las mismas dependencias **reactivas**:

- **Spring Boot 3.4.5**
- **Java 17**
- **Spring WebFlux** (programación reactiva)
- **Spring Data Redis Reactive**
- **Project Reactor** (Mono, Flux)
- **Spring Actuator**
- **Lombok**
- **TestContainers** (para tests)

### Versiones
- **Gradle**: 8.5
- **Maven**: 3.9.6

### Dependencias Reactivas Específicas

#### Spring WebFlux
```gradle
// Gradle
implementation 'org.springframework.boot:spring-boot-starter-webflux'
implementation 'io.projectreactor:reactor-core'
testImplementation 'io.projectreactor:reactor-test'
```

```xml
<!-- Maven -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
```

#### Spring Data Redis Reactive
```gradle
// Gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
```

```xml
<!-- Maven -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

## 📁 Estructura de Archivos

```
├── build.gradle              # Configuración Gradle (WebFlux)
├── pom.xml                   # Configuración Maven (WebFlux)
├── gradlew                   # Gradle Wrapper (Unix/macOS)
├── gradlew.bat              # Gradle Wrapper (Windows)
├── mvnw                     # Maven Wrapper (Unix/macOS)
├── mvnw.cmd                 # Maven Wrapper (Windows)
├── .mvn/                    # Configuración Maven
│   └── wrapper/
│       └── maven-wrapper.properties
└── gradle/                  # Configuración Gradle
    └── wrapper/
        └── gradle-wrapper.properties
```

## 🎯 Casos de Uso

### Para Desarrolladores
- **Usa Gradle** si prefieres una sintaxis más moderna y legible
- **Usa Maven** si prefieres el estándar de la industria o tu equipo ya lo usa
- **Ambos soportan WebFlux** y programación reactiva

### Para CI/CD
- **Gradle**: Mejor para builds complejos y personalizados
- **Maven**: Mejor para integración con herramientas empresariales
- **Ambos compilan** la aplicación WebFlux correctamente

### Para IDEs
- **IntelliJ IDEA**: Soporta ambos nativamente con WebFlux
- **Eclipse**: Mejor soporte para Maven con WebFlux
- **VS Code**: Extensiones disponibles para ambos con soporte WebFlux

## ⚠️ Consideraciones Importantes

### Sincronización
- Ambos archivos de configuración deben mantenerse sincronizados
- Al agregar nuevas dependencias, actualizar tanto `build.gradle` como `pom.xml`
- Las versiones de las dependencias deben ser consistentes
- **WebFlux requiere** dependencias reactivas en ambos sistemas

### Wrappers
- **Nunca elimines** los archivos wrapper (`gradlew`, `mvnw`)
- Los wrappers aseguran que todos los desarrolladores usen la misma versión
- Incluye los wrappers en el control de versiones

### Tests
- Los tests se ejecutan correctamente en ambos sistemas
- Los resultados deben ser idénticos independientemente del sistema usado
- **Tests reactivos** funcionan con `reactor-test` en ambos sistemas

### WebFlux y Reactividad
- **Ambos sistemas** compilan correctamente código WebFlux
- **Project Reactor** está disponible en ambos builds
- **Dependencias reactivas** están sincronizadas entre Gradle y Maven

## 🔄 Migración

### De Gradle a Maven
```bash
# Si quieres usar solo Maven
rm build.gradle gradlew gradlew.bat gradle/
# Usar solo: ./mvnw clean compile
```

### De Maven a Gradle
```bash
# Si quieres usar solo Gradle
rm pom.xml mvnw mvnw.cmd .mvn/
# Usar solo: ./gradlew clean compileJava
```

## 🚀 Ventajas de WebFlux en Ambos Sistemas

### Gradle
- **Configuración declarativa** para dependencias reactivas
- **Mejor rendimiento** en builds incrementales
- **Soporte nativo** para Spring Boot WebFlux

### Maven
- **Estándar de la industria** para proyectos Java
- **Mejor integración** con herramientas empresariales
- **Soporte completo** para WebFlux y Project Reactor

## 📚 Recursos Adicionales

- [Documentación oficial de Gradle](https://gradle.org/docs/)
- [Documentación oficial de Maven](https://maven.apache.org/guides/)
- [Spring Boot WebFlux con Gradle](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/)
- [Spring Boot WebFlux con Maven](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/html/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Documentation](https://projectreactor.io/docs/core/release/reference/)

## 🤝 Contribución

Al contribuir al proyecto:
1. Mantén ambos sistemas de build sincronizados
2. Prueba que funcione tanto con Gradle como con Maven
3. Documenta cualquier cambio en la configuración de build
4. Usa el sistema de build que prefieras para desarrollo local
5. **Asegúrate de que WebFlux compile** en ambos sistemas
6. **Mantén las dependencias reactivas** sincronizadas

## 🎉 Estado Actual

✅ **Gradle**: Configurado y funcionando con WebFlux  
✅ **Maven**: Configurado y funcionando con WebFlux  
✅ **WebFlux**: Compilando correctamente en ambos sistemas  
✅ **Dependencias**: Sincronizadas entre Gradle y Maven  
✅ **Tests**: Funcionando en ambos sistemas de build  
✅ **Arquitectura**: Hexagonal reactiva preservada
