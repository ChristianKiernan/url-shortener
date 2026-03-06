# URL Shortener API

A RESTful API for creating and managing shortened URLs, built with Spring Boot and PostgreSQL.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [API Reference](#api-reference)
- [Error Handling](#error-handling)
- [License](#license)

---

## Features

- Create shortened URLs with auto-generated 6-character base-62 codes
- Retrieve the original URL by short code
- Update or delete existing short URLs
- Track access counts per short URL

---

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.3**
- **PostgreSQL**
- **Flyway** — database schema migrations
- **Docker Compose** — local database setup

---

## Prerequisites

- Java 21+
- Docker and Docker Compose (for the local database)
- Maven (or use the included `./mvnw` wrapper — no installation required)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ChristianKiernan/url-shortener.git
cd url-shortener
```

### 2. Start the database

```bash
docker compose up -d
```

This starts a PostgreSQL instance on port `5433`. Alternatively, point the app at your own PostgreSQL database by
setting the environment variables described in [Configuration](#configuration).

### 3. Run the application

```bash
./mvnw spring-boot:run
```

Flyway automatically runs database migrations on startup — no manual schema setup required.

The API is available at `http://localhost:8080`.

---

## Configuration

The application reads the following environment variables when running with the `prod` profile. In development, the
defaults in `application.properties` are used.

| Variable                     | Description         | Example                                         |
|------------------------------|---------------------|-------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | JDBC connection URL | `jdbc:postgresql://localhost:5432/urlshortener` |
| `SPRING_DATASOURCE_USERNAME` | Database username   | `myuser`                                        |
| `SPRING_DATASOURCE_PASSWORD` | Database password   | `mypassword`                                    |

To run with the production profile:

```bash
SPRING_PROFILES_ACTIVE=prod \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/urlshortener \
SPRING_DATASOURCE_USERNAME=myuser \
SPRING_DATASOURCE_PASSWORD=mypassword \
./mvnw spring-boot:run
```

---

## Running Tests

```bash
./mvnw test
```

The test suite includes unit tests (service layer) and integration tests (controller layer). Integration tests use
Testcontainers and require Docker to be running.

To run a single test class:

```bash
./mvnw test -Dtest=UrlShortenerServiceTest
```

---

## API Reference

Base URL: `http://localhost:8080`

---

### Create a shortened URL

```
POST /shorten
```

**Request body**

```json
{
  "url": "https://example.com/some/very/long/path"
}
```

**Response** `201 Created`

```json
{
  "id": 1,
  "url": "https://example.com/some/very/long/path",
  "shortCode": "aB3xYz",
  "createdAt": "2026-03-06T19:00:00Z",
  "updatedAt": "2026-03-06T19:00:00Z",
  "accessCount": 0
}
```

**Example**

```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com"}'
```

---

### Get a shortened URL

Retrieves the entry for a short code and increments its access count.

```
GET /shorten/{code}
```

**Response** `200 OK`

```json
{
  "id": 1,
  "url": "https://example.com",
  "shortCode": "aB3xYz",
  "createdAt": "2026-03-06T19:00:00Z",
  "updatedAt": "2026-03-06T19:00:00Z",
  "accessCount": 1
}
```

**Example**

```bash
curl http://localhost:8080/shorten/aB3xYz
```

---

### Update a shortened URL

Replaces the original URL associated with a short code.

```
PUT /shorten/{code}
```

**Request body**

```json
{
  "url": "https://example.com/updated/path"
}
```

**Response** `200 OK`

```json
{
  "id": 1,
  "url": "https://example.com/updated/path",
  "shortCode": "aB3xYz",
  "createdAt": "2026-03-06T19:00:00Z",
  "updatedAt": "2026-03-06T19:01:00Z",
  "accessCount": 1
}
```

**Example**

```bash
curl -X PUT http://localhost:8080/shorten/aB3xYz \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/updated/path"}'
```

---

### Delete a shortened URL

```
DELETE /shorten/{code}
```

**Response** `204 No Content`

**Example**

```bash
curl -X DELETE http://localhost:8080/shorten/aB3xYz
```

---

### Get access statistics

Retrieves the entry for a short code without incrementing its access count.

```
GET /shorten/{code}/stats
```

**Response** `200 OK`

```json
{
  "id": 1,
  "url": "https://example.com",
  "shortCode": "aB3xYz",
  "createdAt": "2026-03-06T19:00:00Z",
  "updatedAt": "2026-03-06T19:00:00Z",
  "accessCount": 42
}
```

**Example**

```bash
curl http://localhost:8080/shorten/aB3xYz/stats
```

---

## Error Handling

All error responses follow a consistent JSON format.

### 404 Not Found

Returned when a short code does not exist.

```json
{
  "error": "Short code not found: aB3xYz"
}
```

### 400 Bad Request

Returned when the request body fails validation (e.g. missing or malformed URL).

```json
{
  "url": "must not be blank"
}
```

---

## License

This project is licensed under the MIT License.
