# Car Rental Reservation Demo

A Micronaut-based demo service for reserving cars by type.

---

# Features

* Reserve a car by type (`SEDAN`, `SUV`, `VAN`)
* Check availability for a given start time and number of days
* List reservations
* Get reservation by id
* Prevent overbooking based on limited fleet size
* Unit and integration tests
* Structured logging with Logback

---

# Tech Stack

* Java 21
* Micronaut
* Maven
* JUnit 5
* Logback

---

# Design Notes

## Core model

The system reserves cars by **car type**, not by assigning a specific physical car.

Supported car types:

* `SEDAN`
* `SUV`
* `VAN`

Fleet capacity is fixed in this demo:

* `SEDAN = 3`
* `SUV = 2`
* `VAN = 1`

---

## Reservation time model

The API uses **Instant**, so timestamps are globally unambiguous and timezone-safe.

Reservation intervals are treated as:

```
[start, end)
```

This means **back-to-back reservations are allowed**.

Example:

Reservation A

```
2027-03-10T10:00:00Z → 2027-03-12T10:00:00Z
```

Reservation B

```
2027-03-12T10:00:00Z → 2027-03-14T10:00:00Z
```

These **do not overlap**.

---

## Overlap logic

Two reservations overlap if:

```
existing.start < requested.end
AND
requested.start < existing.end
```

---

## Concurrency

For this in-memory demo, reservation creation is protected with a **lock per `CarType`**.

This makes the following sequence atomic:

```
check availability → create reservation
```

This prevents overbooking when multiple reservations occur concurrently for the same car type.

The `isAvailable(...)` endpoint is treated as an **informational read** and is not guaranteed to remain valid after it is returned.

---

## Production considerations

In a real system I would:

* use a database-backed persistence layer
* rely on **database transactions** for concurrency control
* likely model **individual cars** instead of only car types
* allocate a concrete car when a reservation is created

The locking mechanism in this demo is only used to keep the in-memory implementation thread-safe.

## Authentication and Authorization

Authentication and authorization are intentionally out of scope for this demo.

In a production system, the service would validate the caller’s identity (e.g., OAuth2 / JWT) and enforce authorization rules such as:

- who can create reservations
- who can view or modify reservations
- tenant or user-level isolation

---

# API

## Create reservation

```
POST /reservations
```

### Request body

```json
{
  "carType": "SUV",
  "start": "2027-03-10T10:00:00Z",
  "numOfDays": 2
}
```

### Success response

```
201 Created
Location: /reservations/{id}
```

### Response body

```json
{
  "id": "....",
  "carType": "SUV",
  "start": "2027-03-10T10:00:00Z",
  "end": "2027-03-12T10:00:00Z"
}
```

---

## Check availability

```
GET /reservations/availability
```

Example:

```
GET /reservations/availability?carType=SUV&start=2027-03-10T10:00:00Z&numOfDays=2
```

Response:

```json
{
  "carType": "SUV",
  "start": "2027-03-10T10:00:00Z",
  "numOfDays": 2,
  "available": true
}
```

---

## List reservations

```
GET /reservations
```

---

## Get reservation by id

```
GET /reservations/{id}
```

---

# Status Codes

| Code              | Meaning                                   |
| ----------------- | ----------------------------------------- |
| `201 Created`     | Reservation created          |
| `400 Bad Request` | Invalid request input                     |
| `404 Not Found`   | Reservation not found                     |
| `409 Conflict`    | No car available for requested time range |

---

# Running the Application

Run tests:

```
mvn clean test
```

Run the application:

```
mvn exec:java
```

---

# Example curl Usage

### Create reservation

```
curl -X POST http://localhost:8080/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "carType": "SUV",
    "start": "2027-03-10T10:00:00Z",
    "numOfDays": 2
  }'
```

---

### Check availability

```
curl "http://localhost:8080/reservations/availability?carType=SUV&start=2027-03-10T10:00:00Z&numOfDays=2"
```

---

### List reservations

```
curl http://localhost:8080/reservations
```

---

### Get reservation by id

```
curl http://localhost:8080/reservations/{id}
```

---

# Manual Demo Flow

### 1. Create first VAN reservation

```
curl -i -X POST http://localhost:8080/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "carType": "VAN",
    "start": "2027-03-10T10:00:00Z",
    "numOfDays": 2
  }'
```

Expected:

* `201 Created`
* `Location` header present

---

### 2. Try conflicting VAN reservation

```
curl -i -X POST http://localhost:8080/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "carType": "VAN",
    "start": "2027-03-10T10:00:00Z",
    "numOfDays": 2
  }'
```

Expected:

```
409 Conflict
```

---

### 3. Check availability

```
curl -i "http://localhost:8080/reservations/availability?carType=VAN&start=2027-03-10T10:00:00Z&numOfDays=2"
```

Expected response:

```json
{
  "carType": "VAN",
  "start": "2027-03-10T10:00:00Z",
  "numberOfDays": 2,
  "available": false
}
```

---

### 4. List reservations

```
curl -i http://localhost:8080/reservations
```

---

### 5. Get reservation by id

```
curl -i http://localhost:8080/reservations/{id}
```

---

### 6. Not found test

```
curl -i http://localhost:8080/reservations/00000000-0000-0000-0000-000000000000
```

Expected:

```
404 Not Found
```

---

# Tests

### Unit tests

* `CarRentalServiceTest`

### Integration tests

* `ReservationControllerTest`

---

# Assumptions

* Reservation duration is expressed in whole days
* Fleet capacity is fixed for the demo
* No cancellation/complete flow is implemented
* No database persistence is implemented
* No authentication/authorization is implemented

---

# Possible Future Improvements

* Persist reservations in a database
* Model individual cars instead of only car types
* Support reservation cancellation/completion
* Add user management / authentication / authorization 
* Add OpenAPI / Swagger documentation
