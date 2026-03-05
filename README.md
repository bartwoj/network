# Network Device Manager

A Spring Boot REST API for managing network devices and their uplink relationships.

## Requirements

- **JDK 21 or higher**
- Maven (or use the included Maven Wrapper — no local Maven installation required)

## Build & Run

Build the project:

```bash
./mvnw clean install
```

Start the application:

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## API Documentation

Once the application is running, the interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

The raw OpenAPI JSON spec is available at:

```
http://localhost:8080/v3/api-docs
```

## Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/device/register` | Register a new device |
| `GET` | `/device/retrieve` | Retrieve a single device by MAC address |
| `GET` | `/device/retrieveAll` | Retrieve all devices sorted |
| `GET` | `/device/retrieveOneAsTree` | Retrieve a device with its full uplink chain |
| `GET` | `/device/retrieveAllAsTree` | Retrieve all devices as uplink trees |

