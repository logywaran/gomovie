# GoMovie - Show Module

## 1. Purpose

The Show module manages the scheduling of movies on screens inside GoMovie theatres.

A `MovieShow` represents a specific scheduled showing of a movie on a particular screen at a particular date and time.

The module supports:

* Movie show creation
* Screen-based scheduling
* Theatre-manager ownership enforcement
* Movie and screen validation
* Show date and time validation
* 15-minute scheduling buffer
* Show conflict detection
* Show lifecycle management
* Customer show browsing
* ShowSeat generation

The module does **not** manage seat availability directly.

That responsibility belongs to the `ShowSeat` module.

---

## 2. Module Structure

```text
show/
├── MovieLanguage.java
├── MovieShow.java
├── MovieShowController.java
├── MovieShowMapper.java
├── MovieShowRepository.java
├── MovieShowRequest.java
├── MovieShowResponse.java
├── MovieShowService.java
└── MovieShowServiceImpl.java
```

| Class                  | Responsibility                                 |
| ---------------------- | ---------------------------------------------- |
| `MovieLanguage`        | Defines supported movie-show languages         |
| `MovieShow`            | JPA entity representing a scheduled movie show |
| `MovieShowController`  | Exposes show APIs                              |
| `MovieShowMapper`      | Converts between entity and DTO                |
| `MovieShowRepository`  | Database access and schedule conflict queries  |
| `MovieShowRequest`     | Show creation request and validation           |
| `MovieShowResponse`    | API response DTO                               |
| `MovieShowService`     | Defines show operations                        |
| `MovieShowServiceImpl` | Implements show business logic                 |

---

## 3. Domain Model

A movie show connects:

```text
Movie
  │
  └── 1 : N
          │
          ▼
      MovieShow
          ▲
          │
        N : 1
          │
        Screen
          │
          ▼
       Theatre
```

A `MovieShow` is identified by:

```text
Movie
+
Screen
+
Show Date
+
Start Time
+
End Time
+
Language
```

For example:

```text
Movie:       Movie 1
Screen:      Screen 3
Date:        2026-09-10
Time:        14:00 - 16:30
Language:    ENGLISH
```

represents one scheduled movie show.

---

## 4. Entity Design

The `MovieShow` entity contains:

```text
id
movie_id
screen_id
show_date
start_time
end_time
language
is_active
```

Relationships:

```text
movie_show.movie_id
        ↓
      movie.id

movie_show.screen_id
        ↓
     screen.id
```

Both relationships are mandatory.

A show cannot exist without:

```text
Movie
Screen
```

The entity uses:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
```

because many movie shows can belong to the same movie or screen.

For example:

```text
Movie 1
 ├── Show 1
 ├── Show 2
 └── Show 3

Screen 2
 ├── Show 4
 ├── Show 5
 └── Show 6
```

---

## 5. Movie Language

Language belongs to `MovieShow`, not `Movie`.

The current supported languages are:

```text
TAMIL
ENGLISH
HINDI
TELUGU
MALAYALAM
KANNADA
```

This allows the same movie to have different language showings.

For example:

```text
Movie 1
 ├── 10:00 → TAMIL
 ├── 13:15 → ENGLISH
 └── 18:30 → TAMIL
```

Therefore language is a property of the scheduled show rather than the movie itself.

---

## 6. Show Lifecycle

A show uses an active/inactive lifecycle.

Active:

```text
isActive = true
```

Inactive:

```text
isActive = false
```

Lifecycle:

```text
        deactivate
ACTIVE ──────────────> INACTIVE
  ▲                       │
  │       reactivate      │
  └───────────────────────┘
```

There is intentionally no physical deletion through the API.

This is important because a show may already have related:

```text
ShowSeat
Booking
BookingSeat
Payment
Ticket
```

records.

Therefore an existing show should be deactivated rather than physically deleted.

---

## 7. Business Rules

### Active movie required

A new show can only be created for an active movie.

Flow:

```text
Movie exists
    ↓
Movie is active
    ↓
Continue
```

If the movie does not exist or is inactive:

```text
404 Not Found
```

An inactive movie should not receive new show schedules.

---

### Active screen required

A show must be scheduled on an active screen.

Flow:

```text
Screen exists
    ↓
Manager owns screen
    ↓
Screen is active
    ↓
Continue
```

An inactive screen cannot receive a new show.

The response is:

```text
404 Not Found
```

---

### Manager ownership

Show management is restricted to the theatre manager who owns the theatre containing the selected screen.

Ownership is derived through:

```text
MovieShow
    ↓
Screen
    ↓
Theatre
    ↓
Manager
```

The service verifies:

```text
movieShow.screen.theatre.manager.id
        ==
authenticated manager.id
```

This prevents a manager from creating or modifying shows on another manager's screen.

---

## 8. Show Time Validation

A show must have a valid time range.

The rule is:

```text
startTime < endTime
```

Valid:

```text
10:00 → 13:00
```

Invalid:

```text
13:00 → 10:00
```

Invalid:

```text
10:00 → 10:00
```

Invalid requests result in:

```text
400 Bad Request
```

---

## 9. Show Date Validation

The request uses:

```java
@FutureOrPresent
```

for the show date.

Therefore:

```text
Yesterday       → rejected
Today            → allowed
Future date      → allowed
```

There is an additional service-level check for shows scheduled today.

If:

```text
showDate == today
```

then:

```text
startTime >= current time
```

must be true.

For example:

```text
Current time: 15:00

Today 14:00 → rejected
Today 18:00 → allowed
Tomorrow 09:00 → allowed
```

A show cannot be scheduled to start in the past.

---

## 10. Scheduling Buffer

A mandatory **15-minute buffer** is required between shows on the same screen.

The rule applies to the entire show interval.

For example:

```text
Existing:
10:00 ───────── 13:00
```

A new show:

```text
13:15 ───────── 16:00
```

is allowed.

A new show:

```text
13:14 ───────── 16:00
```

is rejected.

The buffer gives the theatre time for activities such as:

```text
Audience exit
Cleaning
Screen preparation
Next audience entry
```

---

## 11. Conflict Detection

Show conflicts are checked using the screen, date, and time interval.

The repository checks:

```text
same screen
+
same date
+
active show
+
overlapping time
```

The overlap condition is:

```text
existing.startTime < requested.endTime
AND
existing.endTime > requested.startTime
```

For the 15-minute buffer, the requested interval is expanded:

```text
requested start - 15 minutes
requested end   + 15 minutes
```

Example:

```text
Existing show:

10:00 ───────────── 13:00


Requested show:

13:14 ───────────── 16:00
```

The requested interval is treated as:

```text
12:59 ───────────── 16:15
```

This overlaps the existing show, so it is rejected.

Exactly 15 minutes is allowed:

```text
Existing:
10:00 ───────────── 13:00

New:
13:15 ───────────── 16:00
```

---

## 12. Inactive Shows and Scheduling

Conflict detection only considers active shows.

Therefore:

```text
ACTIVE show
    ↓
occupies schedule
```

while:

```text
INACTIVE show
    ↓
does not occupy schedule
```

For example:

```text
Show 1 → 10:00 - 13:00 → ACTIVE
Show 2 → 10:00 - 13:00 → INACTIVE
```

The inactive show does not prevent another active show from being scheduled in that time slot.

This is consistent with the lifecycle model.

---

## 13. Reactivation

Reactivation is not simply:

```text
isActive = true
```

The service performs validation again.

Flow:

```text
Show exists
    ↓
Manager owns show
    ↓
Show is inactive
    ↓
Screen is active
    ↓
Check schedule conflict
    ↓
Reactivate
```

### Why check conflicts again?

Consider:

```text
Show A
10:00 → 13:00
ACTIVE
```

Show B originally existed in the same slot but was inactive:

```text
Show B
10:00 → 13:00
INACTIVE
```

While Show B was inactive, another show could be created.

Therefore, when Show B is reactivated, the service checks the schedule again.

The current show itself is excluded from the conflict query.

If a conflict exists:

```text
409 Conflict
```

Otherwise:

```text
Show becomes ACTIVE
```

---

## 14. Show Creation Flow

Endpoint:

```text
POST /api/shows
```

Request example:

```json
{
  "movieId": 1,
  "screenId": 3,
  "showDate": "2026-09-10",
  "startTime": "14:00:00",
  "endTime": "16:30:00",
  "language": "ENGLISH"
}
```

Processing flow:

```text
Authenticated Theatre Manager
          ↓
Resolve Movie
          ↓
Movie exists?
          ↓
Movie active?
          ↓
Resolve Screen
          ↓
Manager owns screen?
          ↓
Screen active?
          ↓
Validate start < end
          ↓
Validate date/time
          ↓
Check 15-minute schedule buffer
          ↓
Create MovieShow
          ↓
Save MovieShow
          ↓
Generate ShowSeats
          ↓
Return response
```

Successful creation:

```text
201 Created
```

---

## 15. ShowSeat Generation

When a new `MovieShow` is created, the system generates `ShowSeat` records for the physical seats belonging to the screen.

The relationship is:

```text
Screen
  ↓
Physical Seats
  ↓
MovieShow
  ↓
ShowSeats
```

For example:

```text
Screen 3
 ├── Seat A1
 ├── Seat A2
 └── Seat A3
```

When MovieShow 20 is created:

```text
MovieShow 20
 ├── ShowSeat A1
 ├── ShowSeat A2
 └── ShowSeat A3
```

This creates show-specific seat inventory.

The distinction is:

```text
Seat
↓
What physical seat exists?

ShowSeat
↓
What is the state of that seat for this particular show?
```

The `MovieShow` creation and ShowSeat generation occur inside the same transaction.

Therefore, if ShowSeat generation fails, the MovieShow creation can be rolled back.

---

## 16. No General Show Update

There is intentionally no general:

```text
PATCH /api/shows/{id}
```

for changing:

```text
Movie
Screen
Date
Start time
End time
Language
```

The reason is that a show is connected to show-specific seat inventory.

Changing the schedule after `ShowSeat` records have been generated can create inconsistencies.

For example:

```text
Show 10
Screen 1
18:00
```

already has:

```text
ShowSeat records
```

Changing it to:

```text
Screen 2
20:00
```

would require reconsidering the entire show-seat inventory.

Therefore the simpler MVP rule is:

```text
Wrong schedule
      ↓
Deactivate old show
      ↓
Create new show
```

---

## 17. Customer Show Browsing

Customer browsing is based on:

```text
Movie + Theatre
```

Endpoint:

```text
GET /api/shows/movie/{movieId}/theatre/{theatreId}
```

Example:

```text
GET /api/shows/movie/1/theatre/4
```

The repository returns:

```text
Only active shows
+
Matching movie
+
Matching theatre
+
Ordered by show date
+
Ordered by start time
```

The result can contain multiple dates.

Example:

```json
[
  {
    "id": 9,
    "movieId": 1,
    "screenId": 3,
    "showDate": "2026-09-10",
    "startTime": "14:00:00",
    "endTime": "16:30:00",
    "language": "ENGLISH",
    "isActive": true
  },
  {
    "id": 12,
    "movieId": 1,
    "screenId": 3,
    "showDate": "2026-09-11",
    "startTime": "18:00:00",
    "endTime": "20:30:00",
    "language": "TAMIL",
    "isActive": true
  }
]
```

The frontend can group the results by date and display available show times.

---

## 18. Customer vs Manager APIs

The module has two different responsibilities.

### Manager

Managers create and control schedules:

```text
POST /api/shows
PATCH /api/shows/{id}/deactivate
PATCH /api/shows/{id}/reactivate
```

### Customer

Customers browse available active shows:

```text
GET /api/shows/movie/{movieId}/theatre/{theatreId}
```

The distinction is:

```text
Manager
   ↓
Manage schedule

Customer
   ↓
Browse active schedule
```

---

## 19. API

| Method  | Endpoint                                         | Access            | Success       |
| ------- | ------------------------------------------------ | ----------------- | ------------- |
| `POST`  | `/api/shows`                                     | `THEATRE_MANAGER` | `201 Created` |
| `GET`   | `/api/shows/{id}`                                | Public            | `200 OK`      |
| `GET`   | `/api/shows`                                     | Public            | `200 OK`      |
| `GET`   | `/api/shows/movie/{movieId}/theatre/{theatreId}` | Public            | `200 OK`      |
| `PATCH` | `/api/shows/{id}/deactivate`                     | `THEATRE_MANAGER` | `200 OK`      |
| `PATCH` | `/api/shows/{id}/reactivate`                     | `THEATRE_MANAGER` | `200 OK`      |

The primary customer browsing endpoint is:

```text
GET /api/shows/movie/{movieId}/theatre/{theatreId}
```

---

## 20. Security and Ownership

Security uses two layers.

### Role authorization

`SecurityConfig` verifies:

```text
THEATRE_MANAGER
```

for show management operations.

### Resource ownership

`MovieShowService` verifies:

```text
Show
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
MovieShowService
 ↓
Ownership check
 ↓
Allow / Reject
```

A manager knowing another show ID is not enough to modify that show.

If the show belongs to another manager's theatre:

```text
403 Forbidden
```

---

## 21. Repository

The repository provides schedule-specific queries.

### Conflict during creation

```text
existsConflictingShow()
```

Checks whether an active show conflicts with the requested schedule.

### Conflict during reactivation

```text
existsConflictingShowExcludingId()
```

Checks for conflicts while excluding the show being reactivated.

### Customer browsing

```text
findActiveShowsForCustomer()
```

Finds active shows using:

```text
movieId
+
theatreId
```

and orders them by:

```text
showDate
+
startTime
```

---

## 22. Exception Strategy

The Show module uses shared project exceptions.

| Situation                        | Exception                               | Response          |
| -------------------------------- | --------------------------------------- | ----------------- |
| Invalid request                  | Validation / `IllegalArgumentException` | `400 Bad Request` |
| Movie not found/inactive         | `ResourceNotFoundException`             | `404 Not Found`   |
| Screen not found/inactive        | `ResourceNotFoundException`             | `404 Not Found`   |
| Manager does not own screen/show | `AccessDeniedException`                 | `403 Forbidden`   |
| Schedule conflict                | `ResourceAlreadyExistsException`        | `409 Conflict`    |
| Invalid lifecycle state          | `InvalidStateException`                 | `409 Conflict`    |

---

## 23. Testing Status

The MovieShow module was tested through Swagger/OpenAPI.

### Creation

Verified:

* Manager creates a show on an owned screen
* Manager cannot create a show on another manager's screen
* Inactive movie rejected
* Inactive screen rejected
* Invalid start/end time rejected
* Today's past show time rejected
* Today's future show time accepted
* Future date accepted

### Scheduling

Verified:

* Overlapping shows rejected
* 15-minute buffer enforced
* Exactly 15-minute gap allowed
* Less than 15-minute gap rejected
* Inactive shows do not block scheduling
* Reactivation checks for schedule conflicts
* Reactivation excludes the current show from conflict detection

### Lifecycle

Verified:

```text
ACTIVE
   ↓
deactivate
   ↓
INACTIVE
   ↓
reactivate
   ↓
ACTIVE
```

Repeated lifecycle operations are rejected.

### Customer browsing

Verified:

```text
GET /api/shows/movie/1/theatre/4
```

returns:

```text
200 OK
```

and returns active shows ordered by date and time.

---

## 24. Design Decisions

The important Show module decisions are:

* A `MovieShow` connects a movie to a physical screen and schedule.
* Language belongs to `MovieShow`.
* Only active movies can receive new shows.
* Only active screens can receive new shows.
* Show ownership is derived through `Screen → Theatre → Manager`.
* Theatre managers can only manage their own shows.
* Shows cannot have overlapping schedules on the same screen.
* A mandatory 15-minute buffer exists between shows.
* Inactive shows do not occupy the schedule.
* Reactivation performs schedule validation again.
* Shows are deactivated instead of physically deleted.
* There is no general show update API.
* ShowSeat records are generated when a MovieShow is created.
* Customer browsing returns active shows for a movie and theatre.
* Customer results are ordered by show date and start time.

---

## 25. Domain Separation

The Show module is responsible for:

```text
MovieShow
    ↓
When and where is the movie playing?
```

The Seat module is responsible for:

```text
Seat
    ↓
What physical seats exist?
```

The ShowSeat module is responsible for:

```text
ShowSeat
    ↓
What is the state of each seat for this particular show?
```

Therefore:

```text
Movie
   ↓
MovieShow
   ↓
ShowSeat
   ↓
Booking
```

This separation prevents the physical seat definition from being mixed with show-specific availability.

---

## 26. Future Considerations

The Show module intentionally does not handle:

```text
Seat availability
Seat holding
Booking
Payment
Ticket generation
```

Those responsibilities belong to later modules.

The current MVP also does not implement:

```text
Recurring show schedules
Cross-midnight shows
Advanced schedule management
General show editing
```

These are intentionally outside the current scope.

---

## 27. Module Status

The MovieShow module is **complete and ready to be frozen**.

Implemented flow:

```text
THEATRE MANAGER
      ↓
Assigned Theatre
      ↓
Screen
      ↓
Create MovieShow
      ↓
Validate movie
      ↓
Validate screen
      ↓
Validate schedule
      ↓
Check 15-minute buffer
      ↓
Create MovieShow
      ↓
Generate ShowSeats
      ↓
ACTIVE
      ↓
Customer browses active shows
      ↓
Select Show
```

The module has been verified through Swagger/OpenAPI.

Core show creation, validation, ownership, scheduling conflict detection, 15-minute buffering, lifecycle management, reactivation validation, customer browsing, and ShowSeat generation have been implemented and tested.

**Module Status: FROZEN 🔒**
