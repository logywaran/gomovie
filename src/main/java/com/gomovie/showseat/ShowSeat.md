# GoMovie - ShowSeat Module

## 1. Purpose

The ShowSeat module manages **seat inventory for a particular movie show**.

A `ShowSeat` represents one physical `Seat` for one specific `MovieShow`.

It is responsible for:

* Generating show-specific seats
* Displaying available seats for a show
* Maintaining seat booking state
* Maintaining the price for the seat
* Preparing row-level locking for future booking concurrency

The module does **not** manage physical seat configuration.

That responsibility belongs to the `Seat` module.

---

## 2. Module Structure

```text
showseat/
├── ShowSeat.java
├── ShowSeatController.java
├── ShowSeatMapper.java
├── ShowSeatRepository.java
├── ShowSeatResponse.java
├── ShowSeatService.java
├── ShowSeatServiceImpl.java
└── ShowSeatStatus.java
```

| Class                 | Responsibility                          |
| --------------------- | --------------------------------------- |
| `ShowSeat`            | Represents a seat for a particular show |
| `ShowSeatStatus`      | Defines the booking state               |
| `ShowSeatController`  | Exposes customer seat-browsing API      |
| `ShowSeatService`     | Defines ShowSeat operations             |
| `ShowSeatServiceImpl` | Implements ShowSeat business logic      |
| `ShowSeatRepository`  | Database access and row-locking query   |
| `ShowSeatMapper`      | Converts entity to response DTO         |
| `ShowSeatResponse`    | API response DTO                        |

---

## 3. Domain Model

The relationship is:

```text
Screen
  ↓
Seat
  ↓
MovieShow
  ↓
ShowSeat
```

A physical seat can exist independently:

```text
Seat A1
```

When a show is created, the system creates a show-specific inventory record:

```text
MovieShow 16
    ↓
ShowSeat A1
ShowSeat A2
ShowSeat B10
```

Therefore:

```text
Seat
↓
What physical seat exists?

ShowSeat
↓
What is the state and price of that seat
for this particular show?
```

---

## 4. Entity Design

`ShowSeat` stores:

```text
id
show_id
screen_id
seat_id
status
price
```

The foreign keys are maintained using scalar IDs:

```java
private Long showId;
private Long screenId;
private Long seatId;
```

while read-only JPA relationships provide access to:

```text
MovieShow
Screen
Seat
```

The relationships use:

```java
insertable = false
updatable = false
```

This avoids having two writable representations of the same foreign key.

---

## 5. Unique Constraint

A ShowSeat must be unique for a particular show and physical seat.

The database enforces:

```text
UNIQUE(show_id, seat_id)
```

Therefore:

```text
Show 16 + Seat 23 → allowed
Show 16 + Seat 23 → duplicate rejected
Show 17 + Seat 23 → allowed
```

This guarantees that one seat cannot have multiple ShowSeat records for the same show.

---

## 6. ShowSeat Status

The current status enum is:

```text
AVAILABLE
HELD
BOOKED
```

Lifecycle:

```text
AVAILABLE
    ↓
  HELD
    ↓
 BOOKED
```

Meaning:

| Status      | Meaning                             |
| ----------- | ----------------------------------- |
| `AVAILABLE` | Customer can select the seat        |
| `HELD`      | Temporarily reserved during booking |
| `BOOKED`    | Successfully purchased              |

`LOCKED` is **not** a business status.

Database row locking using `PESSIMISTIC_WRITE` is a temporary transaction-level lock and is separate from `ShowSeatStatus`.

`BLOCKED` is also not part of the current MVP because physical seat availability is controlled through `Seat.isActive`.

---

## 7. ShowSeat Generation

ShowSeats are generated automatically when a `MovieShow` is created.

Flow:

```text
Create MovieShow
      ↓
Find active seats of the screen
      ↓
Create ShowSeat for each active seat
      ↓
Status = AVAILABLE
      ↓
Assign price
      ↓
Save ShowSeats
```

Inactive physical seats are not included.

For example:

```text
Screen seats:

A1 → ACTIVE
A2 → ACTIVE
A3 → INACTIVE
B10 → ACTIVE
```

Generated ShowSeats:

```text
A1 → AVAILABLE
A2 → AVAILABLE
B10 → AVAILABLE
```

A3 is not generated.

Show creation and ShowSeat generation occur within the same transaction.

---

## 8. Pricing

The current MVP uses a fixed price:

```text
₹200.00
```

The price is stored directly on `ShowSeat`.

This is important because the booking should use the price associated with the show seat rather than recalculating the physical seat price later.

The field uses:

```java
BigDecimal
```

for monetary precision.

---

## 9. Customer API

Customers can view the seats for an active show.

```text
GET /api/show-seats/show/{showId}
```

Example:

```text
GET /api/show-seats/show/16
```

Response contains:

```text
ShowSeat ID
Show ID
Screen ID
Seat ID
Row
Seat Number
Seat Type
Status
Price
```

Inactive shows return:

```text
404 Not Found
```

Only active show seats are exposed through the customer browsing API.

---

## 10. Concurrency Preparation

The repository contains a pessimistic locking query:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This is intended for the future booking/seat-hold flow.

The planned flow is:

```text
Customer selects seats
        ↓
SELECT ShowSeat FOR UPDATE
        ↓
Database temporarily locks rows
        ↓
Check status
        ↓
AVAILABLE?
        ↓
Change to HELD
        ↓
Commit transaction
        ↓
Database lock released
```

The database lock prevents two concurrent transactions from modifying the same ShowSeat at the same time.

The business status remains:

```text
AVAILABLE → HELD → BOOKED
```

---

## 11. Security

The ShowSeat customer API is protected using:

```text
CUSTOMER
```

The endpoint is:

```text
GET /api/show-seats/**
```

ShowSeat generation is not exposed as a public API.

It is triggered internally by the Show module when a show is created.

This prevents customers or managers from manually creating or modifying show-seat inventory.

---

## 12. Repository

The repository provides:

```text
findByShowId()
```

Used to retrieve seats belonging to a show.

```text
existsByShowId()
```

Used to prevent duplicate ShowSeat generation.

```text
findAllByIdForUpdate()
```

Uses pessimistic locking and prepares the module for concurrent seat holding during booking.

The query orders seat IDs consistently to reduce the possibility of deadlocks when multiple transactions lock several seats.

---

## 13. Design Decisions

Important decisions:

* ShowSeats are generated automatically when a show is created.
* Only active physical seats are converted into ShowSeats.
* `UNIQUE(show_id, seat_id)` protects database integrity.
* ShowSeat status is `AVAILABLE`, `HELD`, or `BOOKED`.
* `BLOCKED` is not required for the current MVP.
* Database locking is separate from business status.
* Price is stored on ShowSeat using `BigDecimal`.
* Customers can only view seats for active shows.
* ShowSeats are not manually created through an API.
* Physical seat configuration remains the responsibility of the Seat module.
* Pessimistic locking is prepared for the future seat-hold flow.

---

## 14. Testing Status

Verified through Swagger/OpenAPI:

```text
1. Inactive physical seats are not generated
2. Active physical seats are generated
3. Generated seats start as AVAILABLE
4. ShowSeat price is assigned correctly
5. Inactive show returns 404
6. Active show returns its ShowSeats
7. ShowSeat generation prevents duplicates
```

Example verified:

```text
Screen 4

A1  → ACTIVE
A2  → ACTIVE
A3  → INACTIVE
B10 → ACTIVE
```

For Show 16:

```text
A1  → AVAILABLE → ₹200
A2  → AVAILABLE → ₹200
B10 → AVAILABLE → ₹200
```

A3 was correctly excluded.

---

## 15. Module Status

The ShowSeat module is **complete and ready to be frozen**.

Core flow:

```text
MovieShow created
      ↓
Get active physical seats
      ↓
Generate ShowSeats
      ↓
AVAILABLE
      ↓
Customer views seats
      ↓
Future booking flow
      ↓
HELD
      ↓
BOOKED
```

**Module Status: FROZEN 🔒**
