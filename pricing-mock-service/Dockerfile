# ---------- BUILD STAGE ----------
FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app

COPY . .

RUN mvn -q -DskipTests -pl pricing-mock-service -am clean package


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/pricing-mock-service/target/pricing-mock-service-1.0.0.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]