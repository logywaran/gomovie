# City Module

## 1. Purpose

The City module manages the cities available in GoMovie.

Cities are master data used to determine which movies, theatres, and shows are available to customers.

The module supports city creation, customer-facing city listing, and admin lifecycle management.

---

## 2. Module Structure

```text
city/
├── City.java
├── CityController.java
├── CityAdminController.java
├── CityMapper.java
├── CityRepository.java
├── CityRequest.java
├── CityResponse.java
├── CityService.java
└── CityServiceImpl.java
```

| Class                 | Responsibility                           |
| --------------------- | ---------------------------------------- |
| `City`                | JPA entity representing a city           |
| `CityController`      | Public/customer city APIs                |
| `CityAdminController` | Admin city management APIs               |
| `CityMapper`          | Converts `City` entity to `CityResponse` |
| `CityRepository`      | Database access                          |
| `CityRequest`         | Request DTO and input validation         |
| `CityResponse`        | Response DTO                             |
| `CityService`         | Defines city operations                  |
| `CityServiceImpl`     | Implements city business logic           |

---

## 3. Design Decisions

### Public and admin APIs are separated

Customer-facing operations use:

```text
/api/cities
```

Administrative operations use:

```text
/api/admin/cities
```

This makes endpoint ownership explicit and allows the security configuration to clearly distinguish public browsing from administrative operations.

### Soft deactivation

Cities are not physically deleted.

Instead:

```text
Active:
isActive = true
deletedAt = null

Inactive:
isActive = false
deletedAt = timestamp
```

This preserves the city record because it may be referenced by theatres and historical booking data.

### Active cities for customers

Customer-facing city retrieval uses:

```text
findByIsActiveTrue()
```

Therefore inactive cities are automatically excluded from the public city list.

Administrators can retrieve all cities, including inactive ones.

### Duplicate prevention

City names are checked using:

```text
existsByNameIgnoreCase()
```

before creation.

The database also enforces a unique constraint on the city name, providing a final data-integrity safeguard.

### Reusable exceptions

The module uses shared exceptions:

* `ResourceNotFoundException` → city does not exist
* `ResourceAlreadyExistsException` → duplicate city
* `InvalidStateException` → city state does not allow the requested operation

No city-specific exception classes are required because these cases follow the project's common exception conventions.

---

## 4. API

| Method  | Endpoint                            | Access  | Success          |
| ------- | ----------------------------------- | ------- | ---------------- |
| `GET`   | `/api/cities`                       | Public  | `200 OK`         |
| `POST`  | `/api/admin/cities`                 | `ADMIN` | `201 Created`    |
| `GET`   | `/api/admin/cities`                 | `ADMIN` | `200 OK`         |
| `PATCH` | `/api/admin/cities/{id}/deactivate` | `ADMIN` | `204 No Content` |
| `PATCH` | `/api/admin/cities/{id}/reactivate` | `ADMIN` | `204 No Content` |

### Error responses

| Situation          |            Status |
| ------------------ | ----------------: |
| Invalid request    | `400 Bad Request` |
| City not found     |   `404 Not Found` |
| Duplicate city     |    `409 Conflict` |
| Invalid city state |    `409 Conflict` |

---

## 5. City Lifecycle

```text
        deactivate
ACTIVE ──────────────> INACTIVE
  ▲                       │
  │                       │ reactivate
  └───────────────────────┘
```

Deactivation:

```text
isActive = false
deletedAt = current timestamp
```

Reactivation:

```text
isActive = true
deletedAt = null
```

---

## 6. Project Conventions

The City module follows the project's standard conventions for:

* DTO validation
* Entity-to-DTO mapping
* Transaction management
* Exception handling
* Logging
* Security
* HTTP response handling

See `docs/conventions.md` for the project-wide standards.

### Module-specific logging

Important City business events are logged, such as:

```text
Creating city
City created successfully
City creation rejected
Deactivating city
City deactivated successfully
Reactivating city
City reactivated successfully
```
## 7. Testing Status

The City module was tested through Swagger/OpenAPI.

Verified scenarios include:

* City creation
* Duplicate city name rejection
* Case-insensitive duplicate city name detection
* Invalid city name validation
* Public city listing
* Admin city listing
* Deactivation of an active city
* Deactivated city hidden from the public API
* Deactivated city still visible to administrators
* Reactivation of an inactive city
* Reactivated city becoming publicly visible
* Repeated deactivation rejected
* Repeated reactivation rejected

The tested lifecycle is:

```text
Create
  ↓
ACTIVE
  ↓
Deactivate
  ↓
INACTIVE
  ↓
Reactivate
  ↓
ACTIVE
```

Invalid lifecycle transitions were also verified:

```text
ACTIVE + deactivate   → success
INACTIVE + deactivate → 409 Conflict

INACTIVE + reactivate → success
ACTIVE + reactivate   → 409 Conflict
```

The City module is considered complete and frozen after successful API testing.

