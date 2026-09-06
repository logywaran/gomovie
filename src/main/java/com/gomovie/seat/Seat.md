# GoMovie - Seat Module

## 1. Purpose

The Seat module manages the physical seats configured inside GoMovie screens.

A seat belongs to a specific screen and represents a physical seating position within that screen.

The module supports:

* Single seat creation
* Bulk seat creation
* Theatre-manager seat browsing
* Seat lifecycle management
* Seat ownership enforcement
* Seat-position uniqueness
* Seat-type classification
* Row-label normalization

Seats are operational theatre resources and are managed by theatre managers.

The module does **not** manage show-specific seat availability. That responsibility belongs to the future `ShowSeat` model.

---

## 2. Module Structure

```text
seat/
├── Seat.java
├── SeatController.java
├── SeatLifecycleController.java
├── SeatMapper.java
├── SeatRepository.java
├── SeatRequest.java
├── BulkSeatRequest.java
├── SeatResponse.java
├── SeatService.java
└── SeatServiceImpl.java
```

| Class                     | Responsibility                               |
| ------------------------- | -------------------------------------------- |
| `Seat`                    | JPA entity representing a physical seat      |
| `SeatController`          | Seat creation and manager seat browsing APIs |
| `SeatLifecycleController` | Seat deactivation and reactivation APIs      |
| `SeatMapper`              | Converts between entity and DTO              |
| `SeatRepository`          | Database access                              |
| `SeatRequest`             | Single-seat creation DTO and validation      |
| `BulkSeatRequest`         | Bulk seat creation DTO and validation        |
| `SeatResponse`            | Seat response DTO                            |
| `SeatService`             | Defines seat operations                      |
| `SeatServiceImpl`         | Implements seat business logic               |

---

## 3. Entity Design

A seat belongs to one screen.

The relationship is:

```text
Theatre
   │
   └── 1 : N
        │
        ▼
      Screen
        │
        └── 1 : N
             │
             ▼
            Seat
```

The `Seat` entity contains:

```text
id
screen_id
row_label
seat_number
seat_type
is_active
deleted_at
```

A seat cannot exist without a screen.

The database foreign key is:

```text
seat.screen_id → screen.id
```

### Physical seat identity

A seat position is unique within a screen using:

```text
(screen_id, row_label, seat_number)
```

The database constraint is:

```text
uk_seat_screen_row_number
```

Therefore:

```text
Screen 1 → A1
Screen 1 → A2
Screen 2 → A1
```

are valid, while:

```text
Screen 1 → A1
Screen 1 → A1
```

is not.

---

## 4. Seat Type

The current `SeatType` values are:

```text
REGULAR
PREMIUM
RECLINER
WHEELCHAIR
```

`SeatType` describes the physical characteristics of the seat/location.

For example:

```text
A1 → REGULAR
A2 → PREMIUM
B1 → RECLINER
C1 → WHEELCHAIR
```

`WHEELCHAIR` represents an accessibility-related physical seating position. It does not represent the customer.

---

## 5. Business Rules

### Seat ownership

A seat does not directly contain a manager reference.

Ownership is derived through:

```text
Seat
 ↓
Screen
 ↓
Theatre
 ↓
Manager
```

The service verifies:

```text
seat.screen.theatre.manager.id
        ==
authenticated manager.id
```

This ensures that a theatre manager can only manage seats belonging to their assigned theatres.

---

### Theatre manager only

Seat management is restricted to:

```text
ROLE_THEATRE_MANAGER
```

There are no dedicated admin seat-management APIs in the current MVP.

The responsibility hierarchy is:

```text
ADMIN
 ├── City
 ├── Movie
 └── Theatre
       │
       └── Theatre Manager
              ├── Screen
              └── Seat
```

---

### Screen must be active

Seats can only be created under an active screen.

The service validates:

```text
Screen exists
    ↓
Manager owns screen
    ↓
Screen is active
    ↓
Create seat
```

An inactive screen results in:

```text
404 Not Found
```

---

### Row-label normalization

Row labels are normalized before storage:

```java
request.rowLabel().trim().toUpperCase()
```

Therefore:

```text
"a"
"A"
"  a  "
```

are stored as:

```text
"A"
```

This prevents logically identical seat positions from being represented differently.

---

### No physical seat update

There is intentionally no general seat update API.

The following physical properties cannot be changed through an update endpoint:

```text
screen
rowLabel
seatNumber
```

A physical seat's identity should remain stable once it may be referenced by historical booking data.

If the physical layout changes:

```text
Old seat
   ↓
Deactivate

New seat
   ↓
Create
```

---

## 6. Bulk Seat Creation

The module supports creating multiple seats in one request.

Endpoint:

```text
POST /api/screens/{screenId}/seats/bulk
```

Example:

```json
{
  "seats": [
    {
      "rowLabel": "A",
      "seatNumber": 1,
      "seatType": "REGULAR"
    },
    {
      "rowLabel": "A",
      "seatNumber": 2,
      "seatType": "REGULAR"
    },
    {
      "rowLabel": "A",
      "seatNumber": 3,
      "seatType": "PREMIUM"
    }
  ]
}
```

The request supports a maximum of:

```text
500 seats
```

The service checks for:

1. Duplicate seats inside the same request
2. Seats that already exist in the database

For example:

```text
Request:
A1
A1
A2
```

is rejected before saving.

The operation is transactional, so the bulk request succeeds or fails as one unit.

```text
Validate all
     ↓
Create all
     ↓
COMMIT

Failure
     ↓
ROLLBACK
```

---

## 7. Validation

`SeatRequest` validates:

```text
rowLabel
    ↓
@NotBlank
@Size(max = 10)

seatNumber
    ↓
@NotNull
@Min(1)
@Max(999)

seatType
    ↓
@NotNull
```

`BulkSeatRequest` validates:

```text
seats
    ↓
@NotEmpty
@Size(max = 500)
@Valid
```

`@Valid` ensures that each nested `SeatRequest` is validated.

Invalid input results in:

```text
400 Bad Request
```

---

## 8. API

| Method  | Endpoint                             | Access            | Success          |
| ------- | ------------------------------------ | ----------------- | ---------------- |
| `POST`  | `/api/screens/{screenId}/seats`      | `THEATRE_MANAGER` | `201 Created`    |
| `POST`  | `/api/screens/{screenId}/seats/bulk` | `THEATRE_MANAGER` | `201 Created`    |
| `GET`   | `/api/screens/{screenId}/seats`      | `THEATRE_MANAGER` | `200 OK`         |
| `PATCH` | `/api/seats/{seatId}/deactivate`     | `THEATRE_MANAGER` | `204 No Content` |
| `PATCH` | `/api/seats/{seatId}/reactivate`     | `THEATRE_MANAGER` | `204 No Content` |

There is no general:

```text
PATCH /api/seats/{seatId}
```

because physical seat identity is intentionally stable.

---

## 9. Seat Creation

Single seat creation:

```text
POST /api/screens/{screenId}/seats
```

Request:

```json
{
  "rowLabel": "A",
  "seatNumber": 1,
  "seatType": "REGULAR"
}
```

The service performs:

```text
Screen exists
      ↓
Manager owns screen
      ↓
Screen is active
      ↓
Normalize row label
      ↓
Check duplicate
      ↓
Create seat
      ↓
Return response
```

Successful creation:

```text
201 Created
```

---

## 10. Manager Seat Collection

A theatre manager can retrieve all seats for a screen:

```text
GET /api/screens/{screenId}/seats
```

This is a management API, so it returns:

```text
ACTIVE + INACTIVE
```

For example:

```json
[
  {
    "id": 23,
    "screenId": 4,
    "rowLabel": "A",
    "seatNumber": 1,
    "seatType": "REGULAR",
    "isActive": true
  },
  {
    "id": 24,
    "screenId": 4,
    "rowLabel": "A",
    "seatNumber": 2,
    "seatType": "PREMIUM",
    "isActive": false
  }
]
```

This allows the manager to see the complete physical layout and identify inactive seats.

Therefore the repository uses:

```text
findByScreenId()
```

rather than an active-only query.

---

## 11. Seat Lifecycle

Seats use soft deactivation.

Active:

```text
isActive = true
deletedAt = null
```

Inactive:

```text
isActive = false
deletedAt = timestamp
```

Lifecycle:

```text
        deactivate
ACTIVE ──────────────> INACTIVE
  ▲                       │
  │       reactivate      │
  └───────────────────────┘
```

### Deactivation

```text
Seat exists
    ↓
Manager owns seat
    ↓
Seat is active
    ↓
isActive = false
deletedAt = current timestamp
```

Response:

```text
204 No Content
```

### Reactivation

```text
Seat exists
    ↓
Manager owns seat
    ↓
Seat is inactive
    ↓
isActive = true
deletedAt = null
```

Response:

```text
204 No Content
```

Repeated lifecycle operations are rejected:

```text
ACTIVE + deactivate
    → 204

INACTIVE + deactivate
    → 409

INACTIVE + reactivate
    → 204

ACTIVE + reactivate
    → 409
```

---

## 12. Security and Ownership

Security has two separate layers.

### Role authorization

`SecurityConfig` verifies:

```text
ROLE_THEATRE_MANAGER
```

### Resource ownership

`SeatService` verifies:

```text
Seat
 ↓
Screen
 ↓
Theatre
 ↓
Manager
```

The complete flow is:

```text
JWT
 ↓
JWT Authentication Filter
 ↓
THEATRE_MANAGER role
 ↓
Controller
 ↓
Authenticated manager
 ↓
SeatService
 ↓
Ownership check
 ↓
Allow / Reject
```

A manager knowing another seat's ID is not enough to access it.

If the seat belongs to another manager's theatre:

```text
403 Forbidden
```

---

## 13. Customer vs Seat

Customers do not directly manage physical `Seat` records.

The customer booking flow will use:

```text
City
 ↓
Movie
 ↓
Theatre
 ↓
Show
 ↓
ShowSeat
 ↓
Seat selection
```

The physical `Seat` represents:

```text
What physical seat exists?
```

The future `ShowSeat` represents:

```text
What is the state of that seat for this particular show?
```

For example:

```text
Seat A1
   ↓
MovieShow 101 → ShowSeat A1
MovieShow 102 → ShowSeat A1
MovieShow 103 → ShowSeat A1
```

Each `ShowSeat` can have its own availability state.

Therefore booking states such as:

```text
AVAILABLE
HELD
BOOKED
```

belong to `ShowSeat`, not `Seat`.

---

## 14. Exception Strategy

The Seat module uses shared project exceptions.

| Situation                     | Exception                        | Response          |
| ----------------------------- | -------------------------------- | ----------------- |
| Invalid request               | Validation exception             | `400 Bad Request` |
| Screen/seat unavailable       | `ResourceNotFoundException`      | `404 Not Found`   |
| Manager does not own resource | `AccessDeniedException`          | `403 Forbidden`   |
| Duplicate seat                | `ResourceAlreadyExistsException` | `409 Conflict`    |
| Invalid lifecycle state       | `InvalidStateException`          | `409 Conflict`    |

The service-level duplicate check provides a meaningful business error, while the database unique constraint remains the final data-integrity protection.

---

## 15. Repository

The repository provides:

```text
existsByScreenIdAndRowLabelAndSeatNumber()
```

for duplicate detection.

It also provides:

```text
findByScreenId()
```

for the manager's complete seat layout.

The database enforces:

```text
uk_seat_screen_row_number
```

to guarantee physical seat-position uniqueness.

---

## 16. Testing Status

The Seat module was tested through Swagger/OpenAPI.

Verified scenarios include:

### Creation

* Manager creates a seat in their own screen
* Manager cannot create a seat in another manager's screen
* Duplicate seat detection
* Row-label normalization

### Bulk creation

* Manager creates multiple seats
* Unauthorized manager is rejected
* Duplicate inside bulk request
* Duplicate against existing database seat
* Nested validation
* Null seat number rejection

### Retrieval

* Manager retrieves seats from their own screen
* Unauthorized manager is rejected
* Active seats are returned
* Inactive seats remain visible in manager collection

### Lifecycle

* Active seat deactivation
* Repeated deactivation rejection
* Inactive seat reactivation
* Repeated reactivation rejection
* Ownership enforcement during lifecycle operations

Verified lifecycle:

```text
Seat 23
   ↓
ACTIVE
   ↓
deactivate
   ↓
INACTIVE
   ↓
GET screen seats
   ↓
Seat 23 still visible
isActive = false
   ↓
reactivate
   ↓
ACTIVE
```

---

## 17. Design Decisions

The important Seat module decisions are:

* A seat belongs to exactly one screen.
* Seat ownership is derived through `Screen → Theatre → Manager`.
* `(screen_id, row_label, seat_number)` uniquely identifies a physical seat position.
* Row labels are normalized before storage.
* Seat management is restricted to `THEATRE_MANAGER`.
* There is no general seat update API.
* Seats use soft deactivation rather than physical deletion.
* Manager seat collections include both active and inactive seats.
* Bulk creation is transactional.
* `Seat` manages physical configuration, while `ShowSeat` will manage show-specific availability.

---

## 18. Future Considerations

The Seat module intentionally does not handle:

```text
MovieShow
ShowSeat generation
Show-specific pricing
Seat availability
Seat holding
Booking
Payment
```

Those responsibilities belong to later modules.

The domain separation remains:

```text
Seat
    ↓
Physical theatre configuration

ShowSeat
    ↓
Physical seat for a particular movie show
    ↓
Availability / booking state
```

---

## 19. Module Status

The Seat module is **complete and ready to be frozen** after final SecurityConfig verification.

Implemented flow:

```text
THEATRE MANAGER
      ↓
Assigned Theatre
      ↓
Screen
      ↓
Create Seat
      ↓
ACTIVE
      ↓
View physical seat layout
      ↓
Deactivate
      ↓
INACTIVE
      ↓
Seat remains visible in manager layout
      ↓
Reactivate
      ↓
ACTIVE
```

The module has been verified through Swagger/OpenAPI.

Core seat creation, bulk creation, validation, normalization, duplicate protection, ownership, lifecycle management, and exception handling have been tested.

Once the final `/api/seats/**` role restriction is verified, the Seat module can be **frozen**.
