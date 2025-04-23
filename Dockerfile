# Etapa de build
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de execução (com Inkscape incluído)
FROM eclipse-temurin:17-jdk

# 🛠 Instala o Inkscape
RUN apt update && apt install -y inkscape

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
