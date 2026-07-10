# ---------- BUILD STAGE ----------
FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app

# Kopieer de volledige multi-module structuur
COPY . .

# Build alleen deze module
RUN mvn -q -DskipTests -pl product-catalog-service -am clean package


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/product-catalog-service/target/product-catalog-service-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]