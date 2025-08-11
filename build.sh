#!/bin/bash

# Script de conveniencia para ejecutar comandos de build con Gradle o Maven
# Uso: ./build.sh [gradle|maven] [comando]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar ayuda
show_help() {
    echo -e "${BLUE}Script de Build Dual - Gradle y Maven${NC}"
    echo ""
    echo "Uso: $0 [gradle|maven] [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo "  clean       - Limpiar build"
    echo "  compile     - Compilar código fuente"
    echo "  test        - Ejecutar tests"
    echo "  build       - Construir proyecto completo"
    echo "  run         - Ejecutar aplicación"
    echo "  package     - Crear JAR/WAR"
    echo ""
    echo "Ejemplos:"
    echo "  $0 gradle clean"
    echo "  $0 maven compile"
    echo "  $0 gradle test"
    echo "  $0 maven spring-boot:run"
    echo ""
    echo "Si no se especifica sistema de build, se usa Gradle por defecto"
}

# Función para ejecutar comando Gradle
run_gradle() {
    local command="$1"
    echo -e "${GREEN}🚀 Ejecutando con Gradle: $command${NC}"
    
    case "$command" in
        "clean")
            ./gradlew clean
            ;;
        "compile")
            ./gradlew clean compileJava
            ;;
        "test")
            ./gradlew test
            ;;
        "build")
            ./gradlew clean build
            ;;
        "run")
            ./gradlew bootRun
            ;;
        "package")
            ./gradlew clean jar
            ;;
        *)
            echo -e "${YELLOW}Comando no reconocido para Gradle: $command${NC}"
            echo -e "${YELLOW}Usando comando directo: ./gradlew $command${NC}"
            ./gradlew "$command"
            ;;
    esac
}

# Función para ejecutar comando Maven
run_maven() {
    local command="$1"
    echo -e "${BLUE}📦 Ejecutando con Maven: $command${NC}"
    
    case "$command" in
        "clean")
            ./mvnw clean
            ;;
        "compile")
            ./mvnw clean compile
            ;;
        "test")
            ./mvnw test
            ;;
        "build")
            ./mvnw clean package
            ;;
        "run")
            ./mvnw spring-boot:run
            ;;
        "package")
            ./mvnw clean package
            ;;
        *)
            echo -e "${YELLOW}Comando no reconocido para Maven: $command${NC}"
            echo -e "${YELLOW}Usando comando directo: ./mvnw $command${NC}"
            ./mvnw "$command"
            ;;
    esac
}

# Función para verificar que los wrappers existen
check_wrappers() {
    if [ ! -f "./gradlew" ]; then
        echo -e "${RED}❌ Error: Gradle wrapper no encontrado (./gradlew)${NC}"
        exit 1
    fi
    
    if [ ! -f "./mvnw" ]; then
        echo -e "${RED}❌ Error: Maven wrapper no encontrado (./mvnw)${NC}"
        exit 1
    fi
    
    # Hacer ejecutables si no lo son
    if [ ! -x "./gradlew" ]; then
        chmod +x ./gradlew
        echo -e "${YELLOW}⚠️  Gradle wrapper hecho ejecutable${NC}"
    fi
    
    if [ ! -x "./mvnw" ]; then
        chmod +x ./mvnw
        echo -e "${YELLOW}⚠️  Maven wrapper hecho ejecutable${NC}"
    fi
}

# Función para mostrar estado de ambos sistemas
show_status() {
    echo -e "${BLUE}📊 Estado de los sistemas de build:${NC}"
    echo ""
    
    # Verificar Gradle
    if [ -f "./gradlew" ] && [ -x "./gradlew" ]; then
        echo -e "${GREEN}✅ Gradle wrapper: Disponible y ejecutable${NC}"
    else
        echo -e "${RED}❌ Gradle wrapper: No disponible o no ejecutable${NC}"
    fi
    
    # Verificar Maven
    if [ -f "./mvnw" ] && [ -x "./mvnw" ]; then
        echo -e "${GREEN}✅ Maven wrapper: Disponible y ejecutable${NC}"
    else
        echo -e "${RED}❌ Maven wrapper: No disponible o no ejecutable${NC}"
    fi
    
    echo ""
    echo -e "${BLUE}📁 Archivos de configuración:${NC}"
    
    if [ -f "./build.gradle" ]; then
        echo -e "${GREEN}✅ build.gradle: Presente${NC}"
    else
        echo -e "${RED}❌ build.gradle: No encontrado${NC}"
    fi
    
    if [ -f "./pom.xml" ]; then
        echo -e "${GREEN}✅ pom.xml: Presente${NC}"
    else
        echo -e "${RED}❌ pom.xml: No encontrado${NC}"
    fi
}

# Función para comparar dependencias
compare_dependencies() {
    echo -e "${BLUE}🔍 Comparando dependencias entre Gradle y Maven...${NC}"
    echo ""
    
    # Extraer versiones de Spring Boot
    gradle_version=$(grep -o "id 'org.springframework.boot' version '[^']*'" build.gradle | grep -o "'[^']*'" | tail -1 | tr -d "'" 2>/dev/null || echo "No encontrado")
    maven_version=$(grep -o "<version>[^<]*</version>" pom.xml | head -1 | sed 's/<version>\(.*\)<\/version>/\1/' 2>/dev/null || echo "No encontrado")
    
    echo -e "Spring Boot:"
    echo -e "  Gradle: ${gradle_version}"
    echo -e "  Maven:  ${maven_version}"
    
    if [ "$gradle_version" = "$maven_version" ]; then
        echo -e "${GREEN}✅ Versiones coinciden${NC}"
    else
        echo -e "${RED}❌ Versiones NO coinciden${NC}"
    fi
    
    echo ""
    echo -e "${YELLOW}💡 Para una comparación completa, ejecuta:${NC}"
    echo -e "  ./gradlew dependencies"
    echo -e "  ./mvnw dependency:tree"
}

# Función principal
main() {
    # Verificar argumentos
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    # Comandos especiales
    case "$1" in
        "help"|"-h"|"--help")
            show_help
            exit 0
            ;;
        "status")
            show_status
            exit 0
            ;;
        "compare")
            compare_dependencies
            exit 0
            ;;
    esac
    
    # Verificar wrappers
    check_wrappers
    
    # Determinar sistema de build
    local build_system="gradle"  # Por defecto
    local command=""
    
    if [ $# -eq 1 ]; then
        # Solo comando, usar Gradle por defecto
        command="$1"
    elif [ $# -eq 2 ]; then
        # Sistema de build + comando
        case "$1" in
            "gradle"|"g")
                build_system="gradle"
                command="$2"
                ;;
            "maven"|"m")
                build_system="maven"
                command="$2"
                ;;
            *)
                echo -e "${RED}❌ Sistema de build no reconocido: $1${NC}"
                echo -e "${YELLOW}Usa 'gradle' o 'maven'${NC}"
                show_help
                exit 1
                ;;
        esac
    else
        echo -e "${RED}❌ Número incorrecto de argumentos${NC}"
        show_help
        exit 1
    fi
    
    # Ejecutar comando
    if [ "$build_system" = "gradle" ]; then
        run_gradle "$command"
    else
        run_maven "$command"
    fi
}

# Ejecutar función principal
main "$@" 