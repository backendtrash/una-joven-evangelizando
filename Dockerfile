# --- Etapa de build: compila el JAR ejecutable con Maven + JDK 17 ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copiamos todo (respetando .dockerignore) y empaquetamos.
COPY . .
# Usamos el `mvn` de la imagen (JAVA_HOME ya viene bien configurado).
RUN mvn -B -ntp clean package -DskipTests

# --- Etapa de runtime: solo JRE 17 + el JAR ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/una-joven-evangelizando.jar app.jar
# Railway inyecta la variable PORT; la app la lee (server.port=${PORT:8080}).
ENTRYPOINT ["java", "-jar", "app.jar"]
