# Etapa 1: Build de la aplicación con Maven
FROM maven:3.9.3-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar pom.xml primero para cachear dependencias
COPY pom.xml .

# Descargar dependencias offline
RUN mvn dependency:go-offline -B

# Copiar el resto del proyecto
COPY . .

# Construir jar ejecutable (sin tests)
RUN mvn clean package -DskipTests

# Etapa 2: Runtime ligero para producción
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Copiar el jar desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Configurar puerto de Render
ENV PORT=8080
EXPOSE ${PORT}

# Ejecutar la app
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
