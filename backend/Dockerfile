# =========================================================
# Stage 1: Build Spring Boot application
# =========================================================

FROM maven:3.9.10-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests


# =========================================================
# Stage 2: Run Spring Boot application
# =========================================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy generated Spring Boot jar
COPY --from=build /app/target/*.jar app.jar

# Spring Boot port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]