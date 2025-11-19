# Etapa 1: Build de la aplicación
FROM eclipse-temurin:21-jdk-alpine AS build

# Directorio de la app
WORKDIR /app

# Copiar archivos de dependencias primero para aprovechar cache
COPY pom.xml .
RUN mkdir -p src && echo "" > src/temp.java

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B || mvn dependency:go-offline -B

# Copiar el resto del proyecto
COPY . .

# Construir el jar ejecutable (skip tests)
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Etapa 2: Crear runtime mínimo con jlink (opcional, muy ligero)
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Copiar el jar desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto, usando variable de entorno de Render
ENV PORT=8080
EXPOSE ${PORT}

# Ejecutar la app con el puerto de Render
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
