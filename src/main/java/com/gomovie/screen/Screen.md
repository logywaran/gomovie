# Screen Module

## 1. Purpose

The Screen module manages the screens available inside GoMovie theatres.

A screen belongs to a specific theatre and represents an individual auditorium within that theatre.

The module supports screen creation, theatre-manager screen browsing, screen retrieval, screen updates, screen lifecycle management, and theatre-manager ownership enforcement.

The module also enforces screen ownership so that a theatre manager can access and modify only the screens belonging to their assigned theatres.

Screens are operational resources and are managed by theatre managers.

Administrators do not have separate Screen management APIs in the current MVP design.

---

## 2. Module Structure

```text
screen/
├── Screen.java
├── ScreenController.java
├── ScreenMapper.java
├── ScreenRepository.java
├── ScreenRequest.java
├── ScreenUpdateRequest.java
├── ScreenResponse.java
├── ScreenService.java
└── ScreenServiceImpl.java
```

| Class                 | Responsibility                             |
| --------------------- | ------------------------------------------ |
| `Screen`              | JPA entity representing a theatre screen   |
| `ScreenController`    | Screen management APIs                     |
| `ScreenMapper`        | Converts between `Screen` entity and DTOs  |
| `ScreenRepository`    | Database access                            |
| `ScreenRequest`       | Screen creation request DTO and validation |
| `ScreenUpdateRequest` | Screen update request DTO and validation   |
| `ScreenResponse`      | Screen response DTO                        |
| `ScreenService`       | Defines screen operations                  |
| `ScreenServiceImpl`   | Implements screen business logic           |

---

## 3. Design Decisions

### Screen belongs to a Theatre

Every screen must belong to a valid theatre.

The relationship is:

```text
Theatre
   │
   └── 1 : N
          │
          ▼
       Screen
```

The `Screen` entity contains:

```text
theatre_id
```

as a foreign key referencing:

```text
theatre.id
```

A screen cannot exist without a theatre.

The service resolves the theatre before creating the screen.

If the supplied theatre does not exist:

```text
404 Not Found
```

---

### Theatre owns Screens

A screen is considered part of the theatre's operational configuration.

For example:

```text
Theatre 5
   │
   ├── Screen 1
   ├── Screen 2
   └── Screen 3
```

A screen therefore does not independently belong to a theatre manager.

Its ownership is derived through the theatre:

```text
Screen
   ↓
Theatre
   ↓
manager_id
   ↓
User
```

The service checks:

```text
screen.theatre.manager.id == authenticated manager.id
```

This ensures that a theatre manager can manage only screens belonging to their own theatres.

---

### Theatre manager ownership

Role authorization and resource ownership are treated as separate concerns.

Spring Security first verifies:

```text
ROLE_THEATRE_MANAGER
```

The service then verifies:

```text
screen.theatre.manager.id == authenticated manager.id
```

Therefore:

```text
JWT
 ↓
THEATRE_MANAGER role?
 ↓
Controller identifies authenticated user
 ↓
Service checks screen ownership through theatre
 ↓
Own theatre → allowed
Other manager's theatre → 403 Forbidden
```

For example:

```text
Manager 1 → Theatre 1 → Screen 5
Manager 2 → Theatre 5 → Screen 4
```

Manager 2:

```text
GET /api/screens/4
→ 200 OK
```

Manager 2:

```text
GET /api/screens/5
→ 403 Forbidden
```

The same ownership rule applies to screen creation, updates, deactivation, and reactivation.

---

### Screen management is Theatre Manager-only

Screens represent operational theatre configuration.

Therefore the current MVP assigns Screen management to the theatre manager.

The authorization boundary is:

```text
THEATRE_MANAGER
      ↓
Screen management
```

There are no dedicated:

```text
/api/admin/screens/**
```

endpoints in the current MVP.

This keeps responsibility clear:

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

An administrator can manage the theatre itself, while the assigned theatre manager manages the operational resources inside that theatre.

---

### Public customers do not directly browse Screens

Customers do not need an independent Screen browsing API in the normal booking flow.

The customer flow is:

```text
City
 ↓
Movie
 ↓
Theatre
 ↓
Show
 ↓
Seat selection
```

Therefore there is no customer-facing:

```text
GET /api/screens/**
```

API.

Screen information is considered internal theatre configuration.

A screen may later be exposed indirectly through show information if required by the booking flow.

---

### Manager screen collection includes inactive screens

The manager's screen collection is a management API.

Therefore:

```text
GET /api/theatres/{theatreId}/screens
```

returns both:

```text
ACTIVE + INACTIVE
```

screens belonging to the manager's theatre.

For example:

```json
[
  {
    "id": 4,
    "theatreId": 5,
    "name": "Screen 1 Updated",
    "isActive": true
  },
  {
    "id": 6,
    "theatreId": 5,
    "name": "Screen 2",
    "isActive": false
  }
]
```

This is intentional.

A manager needs to be able to see inactive screens so that they can identify them and reactivate them when necessary.

Therefore the manager collection uses:

```text
findAllByTheatreId()
```

rather than an active-only query.

---

### Individual inactive screens are not operationally available

Although inactive screens appear in the manager's collection, normal individual screen operations treat them as unavailable.

For example:

```text
GET /api/screens/{screenId}
```

requires the screen to be active.

If the screen is inactive:

```text
404 Not Found
```

Similarly, an inactive screen cannot be updated.

```text
PATCH /api/screens/{screenId}
```

returns:

```text
404 Not Found
```

The only normal operation available to an inactive screen is:

```text
PATCH /api/screens/{screenId}/reactivate
```

This gives us a clear distinction:

```text
Manager collection
        ↓
Shows ACTIVE + INACTIVE

Individual operations
        ↓
ACTIVE screen required
        ↓
except REACTIVATE
```

---

### Screen name uniqueness

A screen name must be unique within a theatre.

The database enforces this using:

```text
(theatre_id, name)
```

with the unique constraint:

```text
uk_screen_theatre_name
```

Therefore:

```text
Screen 1 + Theatre 1
Screen 1 + Theatre 2
```

can both exist.

However:

```text
Screen 1 + Theatre 1
Screen 1 + Theatre 1
```

cannot both exist.

The service performs a case-insensitive duplicate check before saving:

```text
existsByTheatreIdAndNameIgnoreCase()
```

For updates:

```text
existsByTheatreIdAndNameIgnoreCaseAndIdNot()
```

The `IdNot` condition is important because a screen must not be considered a duplicate of itself when its name is being checked.

For example:

```text
Screen 4 = "Screen 1"
```

Updating Screen 4 while keeping:

```text
"Screen 1"
```

should not produce a duplicate error.

But changing Screen 4 to:

```text
"Screen 2"
```

when Screen 2 already exists in the same theatre should return:

```text
409 Conflict
```

The database unique constraint remains the final data-integrity protection.

---

### Separate creation and update requests

Screen creation and updates use separate DTOs.

Creation:

```text
ScreenRequest
     ↓
POST /api/theatres/{theatreId}/screens
```

Update:

```text
ScreenUpdateRequest
     ↓
PATCH /api/screens/{screenId}
```

Creation requires a screen name:

```text
@NotBlank
```

The update request allows the name to be omitted because the operation is a partial update.

Therefore:

```text
ScreenRequest
→ name required

ScreenUpdateRequest
→ name optional
```

---

### Screen cannot be moved between theatres

A screen belongs to the theatre specified during creation.

The update request contains only:

```text
name
```

It does not contain:

```text
theatreId
```

Therefore a theatre manager cannot move a screen from one theatre to another through the update API.

For example:

```text
Screen 4
   ↓
Theatre 5
```

cannot be changed to:

```text
Theatre 1
```

through:

```text
PATCH /api/screens/4
```

This keeps screen ownership stable.

If a different theatre needs the screen, a separate screen would be created under that theatre.

---

### Theatre must be active for screen creation

A screen cannot be created inside an inactive theatre.

The service verifies:

```text
Theatre exists
       ↓
Theatre is active?
       ↓
Create screen
```

If the theatre is inactive, the operation returns:

```text
404 Not Found
```

This prevents new operational resources from being created inside a deactivated theatre.

---

### Soft deactivation

Screens are not physically deleted.

Instead, screen lifecycle is managed using:

```text
isActive
deletedAt
```

Active screen:

```text
isActive = true
deletedAt = null
```

Deactivated screen:

```text
isActive = false
deletedAt = timestamp
```

This preserves the screen record in the database.

A previously deactivated screen can therefore be reactivated without creating a new record.

---

### Lifecycle operations are separate from general updates

`isActive` and `deletedAt` are not part of:

```text
ScreenUpdateRequest
```

They are controlled through dedicated lifecycle operations:

```text
PATCH /api/screens/{id}/deactivate

PATCH /api/screens/{id}/reactivate
```

This prevents a normal name update from bypassing lifecycle rules.

---

## 4. API

| Method  | Endpoint                             | Access            | Success          |
| ------- | ------------------------------------ | ----------------- | ---------------- |
| `POST`  | `/api/theatres/{theatreId}/screens`  | `THEATRE_MANAGER` | `201 Created`    |
| `GET`   | `/api/theatres/{theatreId}/screens`  | `THEATRE_MANAGER` | `200 OK`         |
| `GET`   | `/api/screens/{screenId}`            | `THEATRE_MANAGER` | `200 OK`         |
| `PATCH` | `/api/screens/{screenId}`            | `THEATRE_MANAGER` | `200 OK`         |
| `PATCH` | `/api/screens/{screenId}/deactivate` | `THEATRE_MANAGER` | `204 No Content` |
| `PATCH` | `/api/screens/{screenId}/reactivate` | `THEATRE_MANAGER` | `204 No Content` |

There are currently no:

```text
/api/admin/screens/**
```

or customer-facing:

```text
/api/screens/**
```

management endpoints.

---

### Screen creation

A theatre manager creates a screen using:

```text
POST /api/theatres/{theatreId}/screens
```

Request:

```json
{
  "name": "Screen 1"
}
```

The service validates:

```text
Theatre exists
       ↓
Theatre is active
       ↓
Current manager owns theatre
       ↓
Screen name is unique within theatre
       ↓
Create screen
```

Successful creation returns:

```text
201 Created
```

---

### Manager screen collection

A manager can retrieve all screens belonging to one of their theatres:

```text
GET /api/theatres/{theatreId}/screens
```

The service validates:

```text
Theatre exists
       ↓
Manager owns theatre
       ↓
Theatre is active
       ↓
Retrieve all screens belonging to theatre
```

Both active and inactive screens are returned.

Example:

```json
[
  {
    "id": 4,
    "theatreId": 5,
    "name": "Screen 1 Updated",
    "isActive": true
  },
  {
    "id": 6,
    "theatreId": 5,
    "name": "Screen 2",
    "isActive": false
  }
]
```

This allows the manager to identify inactive screens that may need reactivation.

---

### Screen retrieval

A manager can retrieve an individual screen using:

```text
GET /api/screens/{screenId}
```

The service validates:

```text
Screen exists
       ↓
Manager owns screen through theatre
       ↓
Screen is active
       ↓
Return screen
```

If the screen is inactive:

```text
404 Not Found
```

If the screen belongs to another manager:

```text
403 Forbidden
```

---

### Screen update

A manager can update the name of their own active screen:

```text
PATCH /api/screens/{screenId}
```

Request:

```json
{
  "name": "Updated Screen Name"
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
If name changed → duplicate check
       ↓
Update name
       ↓
Return updated screen
```

An inactive screen cannot be updated.

An attempt to update an inactive screen returns:

```text
404 Not Found
```

---

### Screen deactivation

A manager can deactivate an active screen using:

```text
PATCH /api/screens/{screenId}/deactivate
```

The service validates:

```text
Screen exists
       ↓
Manager owns screen
       ↓
Screen is currently active
       ↓
Set isActive = false
       ↓
Set deletedAt = current timestamp
```

Successful deactivation returns:

```text
204 No Content
```

---

### Screen reactivation

A manager can reactivate an inactive screen using:

```text
PATCH /api/screens/{screenId}/reactivate
```

The service validates:

```text
Screen exists
       ↓
Manager owns screen
       ↓
Screen is currently inactive
       ↓
Set isActive = true
       ↓
Set deletedAt = null
```

Successful reactivation returns:

```text
204 No Content
```

---

## 5. Screen Lifecycle

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
    → 204 No Content

INACTIVE + deactivate
    → 409 Conflict

INACTIVE + reactivate
    → 204 No Content

ACTIVE + reactivate
    → 409 Conflict
```

For individual screen access:

```text
ACTIVE screen
    → GET 200
    → UPDATE 200

INACTIVE screen
    → GET 404
    → UPDATE 404
    → REACTIVATE 204
```

The manager collection remains different:

```text
GET /api/theatres/{theatreId}/screens
    → ACTIVE + INACTIVE
```

This allows managers to discover inactive screens while preventing normal operations on them.

---

## 6. Exception Strategy

The Screen module uses the project's shared exception classes:

* `ResourceNotFoundException` → screen or theatre does not exist, or inactive resource is unavailable for the requested operation
* `ResourceAlreadyExistsException` → duplicate screen name within the same theatre
* `InvalidStateException` → screen lifecycle state does not allow the requested operation

Spring Security's:

```text
AccessDeniedException
```

is used when an authenticated theatre manager attempts to access or modify a screen belonging to another manager's theatre.

Therefore:

```text
Invalid request
    → 400 Bad Request

Screen/Theatre not found
    → 404 Not Found

Manager does not own screen/theatre
    → 403 Forbidden

Duplicate screen name
    → 409 Conflict

Invalid screen state
    → 409 Conflict
```

---

## 7. Security and Ownership

The Screen module uses role-based authorization in `SecurityConfig`.

Screen management endpoints require:

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
"Does this screen belong to one of this manager's theatres?"
```

For a screen:

```text
Screen
   ↓
Theatre
   ↓
Theatre.manager
   ↓
Authenticated User
```

The manager's ownership is therefore determined through the theatre relationship.

Example:

```text
Manager 2
   ↓
Theatre 5
   ↓
Screen 4
```

Manager 2 can manage Screen 4.

But:

```text
Manager 2
   ↓
Theatre 1
   ↓
Screen 5
```

is rejected:

```text
403 Forbidden
```

This prevents a manager from accessing another manager's screens simply by knowing their IDs.

---

## 8. Project Conventions

The Screen module follows the project's standard conventions for:

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

Important Screen business events are logged, such as:

```text
Creating screen
Screen created successfully
Fetching screens for theatre
Fetching screen
Updating screen
Renaming screen
Screen updated successfully
Deactivating screen
Screen deactivated successfully
Reactivating screen
Screen reactivated successfully
Manager does not own theatre
Manager does not own screen
```

Manager ownership failures are logged as warnings so that unauthorized resource-access attempts are visible during development and troubleshooting.

---

## 9. Testing Status

The Screen module was tested through Swagger/OpenAPI.

Verified scenarios include:

### Screen creation

* Manager creates a screen in their own theatre
* Manager cannot create a screen in another manager's theatre
* Duplicate screen name detection
* Case-insensitive duplicate screen name detection

### Screen retrieval

* Manager retrieves screens from their own theatre
* Manager cannot retrieve screens from another manager's theatre
* Manager retrieves their own screen
* Manager cannot retrieve another manager's screen

### Screen update

* Manager updates their own screen
* Duplicate screen name during update
* Case-insensitive duplicate name protection
* `AndIdNot` duplicate protection during rename
* Manager cannot update another manager's screen
* Inactive screen cannot be updated

### Screen lifecycle

* Active screen deactivation
* Repeated deactivation rejection
* Inactive screen retrieval rejection
* Inactive screen update rejection
* Screen reactivation
* Reactivated screen retrieval

### Manager screen collection

* Active screens are returned
* Inactive screens are returned
* Inactive screen remains visible in the manager's theatre screen collection
* Reactivated screen returns as active

The verified lifecycle scenario is:

```text
Screen 6
    ↓
ACTIVE
    ↓
deactivate
    ↓
INACTIVE
    ↓
GET /api/theatres/5/screens
    ↓
Screen 6 still visible with isActive = false
    ↓
reactivate
    ↓
ACTIVE
```

The verified ownership scenarios include:

```text
Manager 2
    ↓
Theatre 5
    ↓
Screen 4
    ↓
GET    → 200 OK
PATCH  → 200 OK
```

and:

```text
Manager 2
    ↓
Manager 1's Theatre
    ↓
Screen 5
    ↓
GET    → 403 Forbidden
PATCH  → 403 Forbidden
```

---

## 10. Screen Module Status

The Screen module is considered **complete and ready to be frozen**.

The implemented flow is:

```text
THEATRE MANAGER
       ↓
Assigned Theatre
       ↓
Create Screen
       ↓
ACTIVE
       ↓
Manager can view screen
       ↓
Manager can update screen
       ↓
Manager can deactivate screen
       ↓
INACTIVE
       ↓
Screen remains visible in manager's theatre screen list
       ↓
Manager can reactivate screen
       ↓
ACTIVE
```

The ownership flow is:

```text
Authenticated Manager
        ↓
SecurityConfig
        ↓
THEATRE_MANAGER role
        ↓
ScreenService
        ↓
Screen → Theatre → Manager
        ↓
Ownership verified
        ↓
Allow / Reject
```

The module has been verified through Swagger/OpenAPI.

The core business rules, ownership rules, duplicate protection, lifecycle behavior, and security boundaries have been tested.

The Screen module is now **frozen**.
