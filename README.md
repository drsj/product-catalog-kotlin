# Product Catalog

A Spring Boot microservices application for product catalog management with pricing integration, built with Kotlin and containerized with Docker.

## Project Overview

This project is a multi-module Maven application consisting of:

- **Product Catalog Service**: A Spring Boot WebFlux service that manages product catalogs with Elasticsearch search capabilities and PostgreSQL persistence
- **Pricing Mock Service**: A lightweight Spring Boot WebFlux service that provides mock pricing data for testing and development

## Architecture

The application uses a microservices architecture with the following components:

- **PostgreSQL**: Relational database for product persistence
- **Elasticsearch**: Search engine for product catalog queries
- **Spring Boot 4.1.0**: Framework for building microservices
- **Kotlin 2.3.21**: Primary language for service implementation
- **Java 25**: JVM target version
- **Docker Compose**: Orchestration of all services for local development

## Prerequisites

- Java 25 or higher
- Maven 3.8+
- Docker and Docker Compose
- 8GB RAM minimum (for all services)

## Building the Project

### Build All Modules

```bash
mvn clean install
```

This will:
- Compile Kotlin and Java source code
- Run tests
- Package the services as executable JAR files with Spring Boot Maven plugin

### Build Specific Module

```bash
# Build product-catalog-service
mvn clean install -pl product-catalog-service

# Build pricing-mock-service
mvn clean install -pl pricing-mock-service
```

## Running the Application

### Quick Start with Docker Compose

The easiest way to run the entire application stack is with Docker Compose:

```bash
docker-compose up --build
```

This will:
- Build Docker images for both services
- Start PostgreSQL database (port 5432)
- Start Elasticsearch instance (port 9200)
- Start Pricing Mock Service (port 9090)
- Start Product Catalog Service (port 8080)

Wait for all services to be healthy (check logs for confirmation).

### Running Individual Services Locally

Before running services locally, start the infrastructure:

```bash
docker-compose up postgres elasticsearch
```

Then run each service:

```bash
# Product Catalog Service
cd product-catalog-service
mvn spring-boot:run

# Pricing Mock Service (in another terminal)
cd pricing-mock-service
mvn spring-boot:run
```

## Service Endpoints

### Product Catalog Service
- **Base URL**: `http://localhost:8080`
- **Actuator**: `http://localhost:8080/actuator`

### Pricing Mock Service
- **Base URL**: `http://localhost:9090`
- **Actuator**: `http://localhost:9090/actuator`

## Database Configuration

**PostgreSQL**:
- Host: localhost
- Port: 5432
- Database: productdb
- User: product
- Password: product

**Elasticsearch**:
- Host: localhost
- Port: 9200
- Security: Disabled (development only)

## Stopping the Application

To stop all services:

```bash
docker-compose down
```

To stop and remove all data:

```bash
docker-compose down -v
```

## Development

### Project Structure

```
product-deployment/
├── product-catalog-service/    # Main catalog management service
│   ├── src/
│   │   ├── main/kotlin/       # Kotlin source code
│   │   └── test/              # Tests
│   ├── Dockerfile             # Container definition
│   └── pom.xml
├── pricing-mock-service/       # Mock pricing service
│   ├── src/
│   │   ├── main/kotlin/       # Kotlin source code
│   │   └── test/              # Tests
│   ├── Dockerfile             # Container definition
│   └── pom.xml
├── docker-compose.yml          # Service orchestration
├── pom.xml                     # Parent POM with dependency management
└── README.md
````

## Technologies

- **Spring Boot**: 4.1.0
- **Kotlin**: 2.3.21
- **Spring Data JPA**: ORM layer
- **Spring Data Elasticsearch**: Search capabilities
- **Spring WebFlux**: Reactive web framework
- **PostgreSQL**: 16
- **Elasticsearch**: 9.4.2
- **Maven**: Build automation

## Troubleshooting

### Services fail to start
- Ensure ports 8080, 9090, 5432, and 9200 are available
- Check Docker is running: `docker ps`
- Review logs: `docker-compose logs <service-name>`

### Database connection errors
- Verify PostgreSQL is healthy: `docker-compose logs postgres`
- Wait for database to be ready (5-10 seconds)
- Check database credentials in docker-compose.yml

### Elasticsearch not responding
- Check Elasticsearch health: `curl http://localhost:9200`
- Ensure xpack.security is disabled in docker-compose.yml for development

## License

This is an assignment project for educational purposes.
