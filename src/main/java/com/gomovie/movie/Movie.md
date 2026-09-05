# Movie Module

## 1. Purpose

The Movie module manages the movies available in GoMovie.

Movies contain the core movie information displayed to customers, such as title, description, duration, release date, certificate, poster, and trailer.

The module supports movie creation, customer-facing movie browsing and retrieval, optional movie filtering and sorting, movie detail updates, administrative movie listing, and admin lifecycle management.

---

## 2. Module Structure

```text
movie/
├── Movie.java
├── MovieController.java
├── AdminMovieController.java
├── MovieMapper.java
├── MovieRepository.java
├── MovieRequest.java
├── MovieUpdateRequest.java
├── MovieResponse.java
├── MovieService.java
└── MovieServiceImpl.java
```

| Class                  | Responsibility                           |
| ---------------------- | ---------------------------------------- |
| `Movie`                | JPA entity representing a movie          |
| `MovieController`      | Public/customer movie APIs               |
| `AdminMovieController` | Admin movie management APIs              |
| `MovieMapper`          | Converts between `Movie` entity and DTOs |
| `MovieRepository`      | Database access                          |
| `MovieRequest`         | Create request DTO and input validation  |
| `MovieUpdateRequest`   | Update request DTO and input validation  |
| `MovieResponse`        | Response DTO                             |
| `MovieService`         | Defines movie operations                 |
| `MovieServiceImpl`     | Implements movie business logic          |

---

## 3. Design Decisions

### Public and admin APIs are separated

Customer-facing movie operations use:

```text
/api/movies
```

Administrative operations use:

```text
/api/admin/movies
```

This makes endpoint ownership explicit and allows the security configuration to clearly distinguish public movie browsing from administrative movie management.

Public movie retrieval is available without authentication, while movie creation, updates, listing, and lifecycle operations require the `ADMIN` role.

### Soft deactivation

Movies are not physically deleted.

Instead:

```text
Active:
isActive = true
deletedAt = null

Inactive:
isActive = false
deletedAt = timestamp
```

This preserves the movie record instead of physically removing it from the database.

A previously deactivated movie can therefore be reactivated without creating a new record.

### Active movies for customers

The public movie API exposes only active movies.

For retrieving a single movie:

```text
findByIdAndIsActiveTrue()
```

For browsing movies:

```text
findByIsActiveTrue()
```

Therefore inactive movies are not exposed through the customer-facing movie APIs.

An inactive movie is treated as not found when requested directly, and is excluded from the public movie collection.

### Public movie filtering and sorting

The public movie collection supports optional filtering and sorting:

```text
GET /api/movies
GET /api/movies?certificate=U/A
GET /api/movies?sort=title
GET /api/movies?sort=releaseDate
```

Certificate filtering is optional.

Supported sorting options are explicitly controlled by the service:

```text
title       → ascending
releaseDate → descending
```

If no sort option is provided, movies are sorted by title in ascending order.

The API does not allow arbitrary entity fields to be supplied as sorting fields.

This keeps the public API limited to sorting options that are intentionally supported by the Movie module.

Movie-level filtering and sorting is kept separate from show-specific filters such as language, show time, price, and wheelchair availability, which belong to the screening and seat context.

### Separate update operation

Movie creation and movie updates use separate request DTOs:

```text
MovieRequest
    ↓ POST /api/admin/movies

MovieUpdateRequest
    ↓ PUT /api/admin/movies/{id}
```

This keeps the API contracts explicit even though the two requests currently contain similar fields.

### Lifecycle fields are not updated through PUT

`isActive` and `deletedAt` are intentionally excluded from `MovieUpdateRequest`.

They are controlled through dedicated lifecycle operations:

```text
PATCH /api/admin/movies/{id}/deactivate
PATCH /api/admin/movies/{id}/reactivate
```

This prevents general movie updates from bypassing the lifecycle rules.

### Movie title uniqueness

The movie title is currently unique in the database.

The service performs a case-insensitive duplicate check before creating or updating a movie.

For creation:

```text
existsByTitleIgnoreCase()
```

For updates:

```text
existsByTitleIgnoreCaseAndIdNot()
```

This provides a clear `409 Conflict` response when a duplicate title is detected.

The database also enforces uniqueness on the `title` column. This provides final data-integrity protection against duplicate records, including concurrent requests.

Therefore:

```text
Create duplicate title
        ↓
409 Conflict

Update to another movie's title
        ↓
409 Conflict
```

This is a deliberate decision for the current GoMovie MVP.

### Language is not stored on Movie

Language is intentionally not part of the Movie entity.

The same movie can have screenings in different languages.

For example:

```text
Movie: Example Movie

Show 1 → Tamil
Show 2 → English
Show 3 → Hindi
```

Therefore language belongs to the show/screening context rather than the core movie record.

### Genre is outside the current MVP

Genre is not included in the Movie entity because it is not required for the current GoMovie MVP.

It can be introduced later if movie categorization or filtering becomes a requirement.

---

## 4. API

| Method  | Endpoint                            | Access  | Success       |
| ------- | ----------------------------------- | ------- | ------------- |
| `GET`   | `/api/movies`                       | Public  | `200 OK`      |
| `GET`   | `/api/movies/{id}`                  | Public  | `200 OK`      |
| `GET`   | `/api/admin/movies`                 | `ADMIN` | `200 OK`      |
| `POST`  | `/api/admin/movies`                 | `ADMIN` | `201 Created` |
| `PUT`   | `/api/admin/movies/{id}`            | `ADMIN` | `200 OK`      |
| `PATCH` | `/api/admin/movies/{id}/deactivate` | `ADMIN` | `200 OK`      |
| `PATCH` | `/api/admin/movies/{id}/reactivate` | `ADMIN` | `200 OK`      |

### Public movie collection

The public movie collection supports optional query parameters:

| Parameter     | Required | Description                      |
| ------------- | -------- | -------------------------------- |
| `certificate` | No       | Filters movies by certificate    |
| `sort`        | No       | Sorts by a supported movie field |

Supported `sort` values:

```text
title
releaseDate
```

If no sort option is provided, movies are sorted by title in ascending order.

If no movies match the requested filter, the API returns an empty list with `200 OK`.

Invalid sort values return:

```text
400 Bad Request
```

### Admin movie collection

The admin movie collection is available through:

```text
GET /api/admin/movies
```

Unlike the public movie collection, this endpoint returns both active and inactive movies.

This allows administrators to view and manage movies that have been deactivated.

### Error responses

| Situation             | Status            |
| --------------------- | ----------------- |
| Invalid request       | `400 Bad Request` |
| Invalid sort option   | `400 Bad Request` |
| Movie not found       | `404 Not Found`   |
| Duplicate movie title | `409 Conflict`    |
| Invalid movie state   | `409 Conflict`    |

For the public movie endpoint, an inactive movie is treated as not found.

---

## 5. Movie Lifecycle

```text
        deactivate
ACTIVE ──────────────> INACTIVE
  ▲                       │
  │                       │
  │       reactivate      │
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

The service validates the current state before performing either operation.

Therefore:

```text
ACTIVE + deactivate   → success
INACTIVE + deactivate → 409 Conflict

INACTIVE + reactivate → success
ACTIVE + reactivate   → 409 Conflict
```

---

## 6. Exception Strategy

The Movie module uses the project's shared exception classes:

* `ResourceNotFoundException` → movie does not exist
* `ResourceAlreadyExistsException` → movie title already exists
* `InvalidStateException` → movie state does not allow the requested operation

Invalid sort values use `IllegalArgumentException`, which is handled by the project's global exception handler and returned as `400 Bad Request`.

No movie-specific exception classes are required because these cases follow the project's common exception conventions.

---

## 7. Project Conventions

The Movie module follows the project's standard conventions for:

* DTO validation
* Entity-to-DTO mapping
* Transaction management
* Exception handling
* Logging
* Security
* HTTP response handling

See `docs/conventions.md` for the project-wide standards.

### Module-specific logging

Important Movie business events are logged, such as:

```text
Creating movie
Movie created successfully
Fetching movies
Fetching active movie
Updating movie
Movie updated successfully
Deactivating movie
Movie deactivated successfully
Reactivating movie
Movie reactivated successfully
```

---

## 8. Testing Status

The Movie module was tested through Swagger/OpenAPI.

Verified scenarios include:

* Movie creation
* Duplicate title rejection
* Case-insensitive duplicate detection
* Movie update
* Duplicate title rejection during update
* Public movie listing
* Certificate filtering
* Title sorting
* Release-date sorting
* Invalid sort rejection
* Movie retrieval by ID
* Nonexistent movie handling
* Movie deactivation
* Inactive movie hidden from public API
* Inactive movie retrieval rejected publicly
* Repeated deactivation rejected
* Movie reactivation
* Reactivated movie becoming publicly visible
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

The Movie module is considered complete and ready to be frozen after the verified API tests.
