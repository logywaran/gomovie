# Theatre Module

## 1. Purpose

The Theatre module manages the theatres available in GoMovie.

A theatre belongs to a city and is assigned to a specific theatre manager. The module supports theatre creation, customer-facing theatre browsing and retrieval, administrative theatre management, theatre lifecycle management, and theatre-manager-specific theatre operations.

The module also enforces theatre ownership so that a theatre manager can access and modify only the theatres assigned to them.

---

## 2. Module Structure

```text
theatre/
├── Theatre.java
├── TheatreAdminController.java
├── TheatreManagerController.java
├── TheatrePublicController.java
├── TheatreMapper.java
├── TheatreRepository.java
├── TheatreRequest.java
├── TheatreUpdateRequest.java
├── TheatreResponse.java
├── TheatreService.java
└── TheatreServiceImpl.java
```

| Class                      | Responsibility                              |
| -------------------------- | ------------------------------------------- |
| `Theatre`                  | JPA entity representing a theatre           |
| `TheatrePublicController`  | Public/customer theatre APIs                |
| `TheatreAdminController`   | Admin theatre management APIs               |
| `TheatreManagerController` | Theatre manager APIs                        |
| `TheatreMapper`            | Converts between `Theatre` entity and DTOs  |
| `TheatreRepository`        | Database access                             |
| `TheatreRequest`           | Theatre creation request DTO and validation |
| `TheatreUpdateRequest`     | Theatre update request DTO and validation   |
| `TheatreResponse`          | Theatre response DTO                        |
| `TheatreService`           | Defines theatre operations                  |
| `TheatreServiceImpl`       | Implements theatre business logic           |

---

## 3. Design Decisions

### Theatre belongs to a City

Every theatre must belong to a valid city.

The request contains:

```text
cityId
```

The service resolves the corresponding `City` entity before creating the theatre.

The relationship is:

```text
City
  │
  └── 1 : N
        │
        ▼
     Theatre
```

A theatre cannot be created with a non-existent city.

If the supplied city does not exist:

```text
404 Not Found
```

---

### Theatre is assigned to a Theatre Manager

Every theatre must have an assigned theatre manager.

The request contains:

```text
managerId
```

The service resolves the corresponding `User` entity and verifies that the user's role is:

```text
THEATRE_MANAGER
```

A `CUSTOMER` or `ADMIN` cannot be assigned as a theatre manager.

Therefore:

```text
Theatre
   │
   └── manager_id
          │
          ▼
        User
```

The database relationship is represented using:

```text
@ManyToOne
```

with a foreign key from:

```text
theatre.manager_id → users.id
```

This keeps theatre ownership tied to an actual user account instead of storing the manager's name as plain text.

---

### Theatre manager ownership

Role authorization and resource ownership are treated as separate concerns.

Spring Security first verifies:

```text
ROLE_THEATRE_MANAGER
```

The service then verifies:

```text
theatre.manager.id == authenticated manager.id
```

Therefore:

```text
JWT
 ↓
THEATRE_MANAGER role?
 ↓
Controller identifies authenticated user
 ↓
Service checks theatre ownership
 ↓
Own theatre → allowed
Other manager's theatre → 403 Forbidden
```

This prevents a theatre manager from accessing or modifying another manager's theatre simply by knowing its ID.

For example:

```text
Manager 1 → Theatre 1
Manager 2 → Theatre 5
```

Manager 2:

```text
GET /api/manager/theatres/5
→ 200 OK
```

Manager 2:

```text
GET /api/manager/theatres/1
→ 403 Forbidden
```

The same ownership rule applies to manager updates.

---

### Public and administrative APIs are separated

Customer-facing theatre operations use:

```text
/api/theatres
```

Administrative theatre operations use:

```text
/api/admin/theatres
```

Theatre manager operations use:

```text
/api/manager/theatres
```

This makes endpoint ownership explicit and allows Spring Security to apply different authorization rules to each API.

Public APIs are limited to customer-facing read operations.

Administrative APIs require:

```text
ADMIN
```

Theatre manager APIs require:

```text
THEATRE_MANAGER
```

---

### Active theatres for customers

The public theatre APIs expose only active theatres.

For theatre browsing by city:

```text
findByCityIdAndIsActiveTrue()
```

For retrieving a single theatre, the service first retrieves the theatre and then checks:

```text
isActive == true
```

Inactive theatres are therefore not exposed through customer-facing APIs.

An inactive theatre requested directly through the public API is treated as:

```text
404 Not Found
```

---

### Admin can view inactive theatres

Administrators need to manage inactive theatres, so the admin APIs do not apply the active-only restriction.

Therefore:

```text
Public
  → active theatres only

Admin
  → active + inactive theatres
```

This allows administrators to inspect and manage theatres that have previously been deactivated.

---

### Soft deactivation

Theatres are not physically deleted.

Instead:

```text
Active:

isActive = true
deletedAt = null
```

When deactivated:

```text
Inactive:

isActive = false
deletedAt = timestamp
```

This preserves the theatre record in the database.

A previously deactivated theatre can therefore be reactivated without creating a new record.

---

### Theatre name uniqueness

The theatre name must be unique within a city.

The database enforces this using:

```text
(city_id, name)
```

with the unique constraint:

```text
uk_theatre_city_name
```

This means:

```text
PVR + Coimbatore
PVR + Chennai
```

can both exist because they belong to different cities.

However:

```text
PVR + Coimbatore
PVR + Coimbatore
```

cannot both exist.

The service performs a case-insensitive duplicate check before saving:

```text
existsByCityIdAndNameIgnoreCase()
```

For updates:

```text
existsByCityIdAndNameIgnoreCaseAndIdNot()
```

This provides a meaningful:

```text
409 Conflict
```

response before attempting the database operation.

The database constraint remains the final data-integrity protection.

---

### Separate update request

Theatre creation and updates use separate DTOs:

```text
TheatreRequest
     ↓
POST /api/admin/theatres
```

and:

```text
TheatreUpdateRequest
     ↓
PATCH /api/admin/theatres/{id}
PATCH /api/manager/theatres/{id}
```

`TheatreUpdateRequest` contains only:

```text
name
address
```

This is intentional.

A manager cannot change:

```text
cityId
managerId
isActive
deletedAt
```

through the manager update API.

The theatre's city and ownership therefore remain controlled by the appropriate business operations.

---

### Manager cannot change theatre ownership

The manager update request deliberately does not contain:

```text
managerId
```

Therefore a theatre manager cannot transfer a theatre to another manager through their update endpoint.

Manager update is limited to theatre details:

```text
name
address
```

Ownership remains an administrative responsibility.

---

### Lifecycle operations are separate from general updates

`isActive` and `deletedAt` are not part of `TheatreUpdateRequest`.

They are controlled through dedicated lifecycle operations:

```text
PATCH /api/admin/theatres/{id}/deactivate

PATCH /api/admin/theatres/{id}/reactivate
```

This prevents a normal theatre update from bypassing lifecycle rules.

Only administrators can deactivate or reactivate theatres.

---

## 4. API

| Method  | Endpoint                              | Access            | Success          |
| ------- | ------------------------------------- | ----------------- | ---------------- |
| `GET`   | `/api/theatres?cityId={cityId}`       | Public            | `200 OK`         |
| `GET`   | `/api/theatres/{id}`                  | Public            | `200 OK`         |
| `GET`   | `/api/admin/theatres`                 | `ADMIN`           | `200 OK`         |
| `GET`   | `/api/admin/theatres/{id}`            | `ADMIN`           | `200 OK`         |
| `POST`  | `/api/admin/theatres`                 | `ADMIN`           | `201 Created`    |
| `PATCH` | `/api/admin/theatres/{id}`            | `ADMIN`           | `200 OK`         |
| `PATCH` | `/api/admin/theatres/{id}/deactivate` | `ADMIN`           | `204 No Content` |
| `PATCH` | `/api/admin/theatres/{id}/reactivate` | `ADMIN`           | `204 No Content` |
| `GET`   | `/api/manager/theatres`               | `THEATRE_MANAGER` | `200 OK`         |
| `GET`   | `/api/manager/theatres/{id}`          | `THEATRE_MANAGER` | `200 OK`         |
| `PATCH` | `/api/manager/theatres/{id}`          | `THEATRE_MANAGER` | `200 OK`         |

---

### Public theatre collection

The public theatre collection is filtered by city:

```text
GET /api/theatres?cityId=1
```

Only active theatres belonging to the requested city are returned.

If the city does not exist:

```text
404 Not Found
```

If the city exists but has no active theatres:

```text
200 OK
[]
```

---

### Public theatre retrieval

A customer can retrieve a theatre using:

```text
GET /api/theatres/{id}
```

The service verifies that the theatre exists and is active.

An inactive or non-existent theatre is returned as:

```text
404 Not Found
```

---

### Admin theatre creation

An administrator creates a theatre using:

```text
POST /api/admin/theatres
```

The request contains:

```json
{
  "cityId": 1,
  "managerId": 3,
  "name": "Example Theatre",
  "address": "Example Address"
}
```

The service validates:

```text
City exists
       ↓
Manager exists
       ↓
Manager has THEATRE_MANAGER role
       ↓
Theatre name is unique within city
       ↓
Create theatre
```

Successful creation returns:

```text
201 Created
```

---

### Admin theatre update

Administrators can update:

```text
name
address
```

using:

```text
PATCH /api/admin/theatres/{id}
```

An inactive theatre cannot be updated through the normal admin update operation.

---

### Theatre manager collection

A theatre manager can retrieve their assigned theatres using:

```text
GET /api/manager/theatres
```

The authenticated user's identity is obtained from the JWT authentication context.

The service then retrieves theatres using:

```text
findByManagerId(managerId)
```

Therefore the manager does not supply a `managerId` query parameter and cannot request another manager's theatre collection.

---

### Theatre manager retrieval

A manager can retrieve a specific theatre using:

```text
GET /api/manager/theatres/{id}
```

The service verifies:

```text
Theatre exists
       ↓
Current user owns theatre
       ↓
Theatre is active
       ↓
Return theatre
```

If the theatre belongs to another manager:

```text
403 Forbidden
```

If the manager owns the theatre but it is inactive:

```text
409 Conflict
```

---

### Theatre manager update

A manager can update only their own theatre:

```text
PATCH /api/manager/theatres/{id}
```

The request contains only:

```json
{
  "name": "Updated Theatre Name",
  "address": "Updated Address"
}
```

The service performs:

```text
Theatre exists
       ↓
Manager owns theatre?
       ↓
Is theatre active?
       ↓
Name duplicate check
       ↓
Update name/address
       ↓
Return updated theatre
```

If the theatre belongs to another manager:

```text
403 Forbidden
```

---

## 5. Theatre Lifecycle

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
ACTIVE + deactivate
    → success

INACTIVE + deactivate
    → 409 Conflict

INACTIVE + reactivate
    → success

ACTIVE + reactivate
    → 409 Conflict
```

---

## 6. Exception Strategy

The Theatre module uses the project's shared exception classes:

* `ResourceNotFoundException` → theatre or city does not exist
* `ResourceAlreadyExistsException` → duplicate theatre name within a city
* `InvalidStateException` → theatre state does not allow the requested operation

Spring Security's:

```text
AccessDeniedException
```

is used when an authenticated theatre manager attempts to access or modify a theatre that belongs to another manager.

Therefore:

```text
Invalid request
    → 400 Bad Request

Theatre/City not found
    → 404 Not Found

Manager does not own theatre
    → 403 Forbidden

Duplicate theatre name
    → 409 Conflict

Invalid theatre state
    → 409 Conflict
```

---

## 7. Security and Ownership

The Theatre module uses role-based authorization in `SecurityConfig`.

Public theatre GET operations are permitted:

```text
GET /api/theatres/**
```

Admin theatre operations require:

```text
ROLE_ADMIN
```

Manager theatre operations require:

```text
ROLE_THEATRE_MANAGER
```

The security configuration provides the first authorization boundary.

The service provides the resource ownership boundary.

These are intentionally separate:

```text
Role authorization
       ↓
"Is this user a theatre manager?"
       ↓
Ownership authorization
       ↓
"Does this theatre belong to this manager?"
```

This prevents role-based access from being confused with resource-level ownership.

---

## 8. Project Conventions

The Theatre module follows the project's standard conventions for:

* DTO validation
* Entity-to-DTO mapping
* Transaction management
* Exception handling
* Logging
* Security
* HTTP response handling
* Soft lifecycle management

See `docs/conventions.md` for the project-wide standards.

### Module-specific logging

Important Theatre business events are logged, such as:

```text
Creating theatre
Theatre created successfully
Fetching theatres by city
Fetching theatre
Updating theatre
Theatre updated successfully
Deactivating theatre
Theatre deactivated successfully
Reactivating theatre
Theatre reactivated successfully
Manager accessing theatre
Manager does not own theatre
Manager updating theatre
```

Manager ownership failures are logged as warnings so that unauthorized resource-access attempts are visible during development and troubleshooting.

---

## 9. Testing Status

The Theatre module was tested through Swagger/OpenAPI.

Verified scenarios include:

### Theatre creation

* Theatre creation with valid city and manager
* Theatre creation with non-existent manager
* Theatre creation with a user who is not a theatre manager
* Duplicate theatre name detection
* Case-insensitive duplicate theatre name detection

### Admin operations

* Admin theatre listing
* Admin theatre retrieval by ID
* Admin theatre update
* Duplicate theatre name during update
* Non-existent theatre update
* Theatre deactivation
* Repeated deactivation rejection
* Theatre reactivation
* Repeated reactivation rejection

### Public operations

* Public theatre listing by city
* Public theatre retrieval by ID
* Non-existent city handling
* Inactive theatre hidden from public collection
* Inactive theatre rejected from public retrieval
* Reactivated theatre becoming publicly visible

### Theatre manager operations

* Manager retrieves only assigned theatres
* Manager retrieves their own theatre
* Manager cannot retrieve another manager's theatre
* Manager updates their own theatre
* Manager cannot update another manager's theatre
* Unauthorized manager access returns `403 Forbidden`

The verified ownership scenarios are:

```text
Manager 2
    ↓
Theatre 5
    ↓
GET    → 200 OK
PATCH  → 200 OK
```

and:

```text
Manager 2
    ↓
Theatre 1
    ↓
GET    → 403 Forbidden
PATCH  → 403 Forbidden
```

---

## 10. Theatre Module Status

The Theatre module is considered **complete and ready to be frozen**.

The implemented flow is:

```text
Admin
  ↓
Create Theatre
  ↓
Assign City
  ↓
Assign Theatre Manager
  ↓
ACTIVE
  ↓
Manager can access own theatre
  ↓
Manager can update own theatre
  ↓
Admin can deactivate
  ↓
INACTIVE
  ↓
Admin can reactivate
  ↓
ACTIVE
```

The module has been verified through Swagger/OpenAPI and the core business, lifecycle, security, and ownership rules have been tested.

The Theatre module is now frozen.
