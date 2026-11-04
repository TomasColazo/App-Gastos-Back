# App Gastos - Backend API

API REST construida con **Java 21** y **Spring Boot 3.3.x**, con base de datos **PostgreSQL** y autenticación por **JWT**.

## Características
- Autenticación de usuarios con JWT (Spring Security 6).
- Gestión de gastos diarios, mensuales, compras de supermercado, salidas, suscripciones y cuotas de tarjetas.
- Soporte multimoneda (ARS, USD, EUR, etc.).
- Categorías precargadas del sistema y soporte para categorías personalizadas.
- Resúmenes y estadísticas mensuales.
- Dockerizado listo para producción o desarrollo mediante Docker Compose.

## Requisitos
- Java 21+
- Maven 3.9+ (o Docker)
- PostgreSQL 16+

## Ejecución local
```bash
# Con Maven local:
mvn spring-boot:run

# O utilizando Docker Compose desde la raíz del proyecto:
docker compose up --build backend
```
