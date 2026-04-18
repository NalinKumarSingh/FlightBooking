# Flight Booking System - Complete Technical Documentation

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [High Level Design (HLD)](#2-high-level-design-hld)
3. [Low Level Design (LLD)](#3-low-level-design-lld)
4. [Entity Relationship Diagram](#4-entity-relationship-diagram)
5. [Module Documentation](#5-module-documentation)
6. [API Reference](#6-api-reference)
7. [System Design](#7-system-design)
8. [Security Analysis](#8-security-analysis)
9. [Data Flow Examples](#9-data-flow-examples)
10. [Database Schema](#10-database-schema)
11. [Production Considerations](#11-production-considerations)

---

## 1. Project Overview

### 1.1 Introduction
**Flight Booking System** is a Spring Boot REST API backend for managing flight bookings with features like:
- JWT-based authentication with role-based access control
- Redis-based distributed seat locking (prevents double booking)
- Dynamic seat pricing based on class and type
- Flexible cancellation policies with automatic refund calculation
- Email OTP verification for new users
- Background schedulers for automatic cleanup

### 1.2 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.5.3 |
| Language | Java | 17 |
| Database | MariaDB | - |
| Cache/Locking | Redis (Redisson) | 3.27.2 |
| Authentication | JWT (jjwt) | 0.11.5 |
| Email | Spring Mail (Gmail SMTP) | - |
| Build Tool | Gradle (Kotlin DSL) | - |
| ORM | Spring Data JPA | - |

### 1.3 Project Structure

```
src/main/java/com/example/flight/
├── FlightApplication.java          # Main entry point
└── v1/
    ├── airline/                    # Airline management
    │   ├── controller/
    │   ├── entity/
    │   ├── exceptions/
    │   ├── model/
    │   ├── repository/
    │   └── service/
    ├── booking/                    # Core booking logic
    │   ├── controller/
    │   ├── entity/
    │   ├── enums/
    │   ├── exceptions/
    │   ├── model/
    │   ├── repository/
    │   └── service/
    ├── configure/                  # Configuration classes
    │   └── RedissonConfig.java
    ├── flight/                     # Flight management
    ├── flightschedule/            # Flight schedules
    ├── location/                   # Airports/locations
    ├── passenger/                  # Passenger info
    ├── payment/                    # Payment processing
    ├── policy/                     # Cancellation policies
    ├── scheduler/                  # Background jobs
    ├── seat/                       # Seat inventory
    ├── ticket/                     # Ticket generation
    ├── user/                       # User auth & management
    └── utils/                      # Utilities (JWT, OTP, Mail)
```

---

## 2. High Level Design (HLD)

### 2.1 System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                                 │
│                   (Angular Frontend @ :4200)                         │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY                                  │
│                    (Spring Boot @ :8093)                             │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    REST Controllers                          │    │
│  │  UserController │ BookingController │ FlightController │ ... │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                                  │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                   Business Logic                             │    │
│  │  UserService │ BookingService │ PaymentService │ SeatService │    │
│  └─────────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                     Utilities                                │    │
│  │      JwtUtil    │    OtpUtil    │    MailService            │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌──────────────────────┐  ┌──────────────┐  ┌──────────────┐
│      DATA LAYER      │  │    CACHE     │  │    EMAIL     │
│   ┌──────────────┐   │  │ ┌──────────┐ │  │ ┌──────────┐ │
│   │   MariaDB    │   │  │ │  Redis   │ │  │ │  Gmail   │ │
│   │              │   │  │ │          │ │  │ │   SMTP   │ │
│   │ - Users      │   │  │ │ Seat     │ │  │ │          │ │
│   │ - Bookings   │   │  │ │ Locks    │ │  │ │  OTP     │ │
│   │ - Flights    │   │  │ │          │ │  │ │  Emails  │ │
│   │ - Seats      │   │  │ └──────────┘ │  │ └──────────┘ │
│   │ - Payments   │   │  │  :6379       │  │              │
│   └──────────────┘   │  └──────────────┘  └──────────────┘
└──────────────────────┘
```

### 2.2 Key Components

| Component | Purpose |
|-----------|---------|
| **User Module** | Authentication, registration, OTP verification, role management |
| **Airline Module** | Airline CRUD, cancellation policies |
| **Flight Module** | Flight definition, routes |
| **Schedule Module** | Flight timings, status management |
| **Seat Module** | Seat inventory, pricing, availability |
| **Booking Module** | Core booking with distributed locking |
| **Payment Module** | Payment processing, ticket generation |
| **Ticket Module** | Digital ticket retrieval |
| **Scheduler** | Background jobs for cleanup |

### 2.3 User Roles

| Role | Permissions |
|------|-------------|
| **USER** | Search flights, book seats, make payments, view tickets, cancel bookings |
| **ADMIN** | Create airlines, verify airline staff |
| **AIRLINE_STAFF** | Add flights, locations, schedules (only for their airline) |

---

## 3. Low Level Design (LLD)

### 3.1 Class Diagrams

#### User Module
```
┌─────────────────────────────────────┐
│            User (Entity)            │
├─────────────────────────────────────┤
│ - id: Long                          │
│ - name: String                      │
│ - email: String (unique)            │
│ - password: String                  │
│ - role: Role (enum)                 │
│ - isVerified: Boolean               │
│ - adminVerification: Boolean        │
│ - airlineId: Long                   │
│ - createdAt: LocalDateTime          │
└─────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────┐
│         Role (Enum)                 │
├─────────────────────────────────────┤
│ USER, ADMIN, AIRLINE_STAFF          │
└─────────────────────────────────────┘
```

#### Booking Module
```
┌─────────────────────────────────────┐
│          Booking (Entity)           │
├─────────────────────────────────────┤
│ - id: Long                          │
│ - userId: Long (FK)                 │
│ - scheduleId: Long (FK)             │
│ - seatIds: String (comma-separated) │
│ - totalPrice: BigDecimal            │
│ - priceLocked: BigDecimal           │
│ - status: BookingStatus             │
│ - bookedAt: LocalDateTime           │
│ - expiresAt: LocalDateTime          │
│ - version: Integer (optimistic lock)│
└─────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────┐
│       BookingStatus (Enum)          │
├─────────────────────────────────────┤
│ PENDING, CONFIRMED, CANCELLED       │
└─────────────────────────────────────┘
```

#### Seat Module
```
┌─────────────────────────────────────┐
│           Seat (Entity)             │
├─────────────────────────────────────┤
│ - id: Long                          │
│ - seatNumber: String (e.g., "1A")   │
│ - seatClass: SeatClass              │
│ - seatType: SeatType                │
│ - basePrice: BigDecimal             │
│ - premiumFee: BigDecimal            │
│ - isAvailable: Boolean              │
│ - scheduleId: Long (FK)             │
│ - lockedUntil: LocalDateTime        │
└─────────────────────────────────────┘
            │
    ┌───────┴───────┐
    ▼               ▼
┌───────────┐  ┌───────────────┐
│ SeatClass │  │   SeatType    │
├───────────┤  ├───────────────┤
│ ECONOMY   │  │ REGULAR       │
│ BUSINESS  │  │ EXTRA_LEGROOM │
└───────────┘  └───────────────┘
```

### 3.2 Sequence Diagrams

#### User Registration Flow
```
User          Controller       Service         OtpUtil        MailService      Database
 │                │               │               │               │               │
 │  POST /register│               │               │               │               │
 │───────────────>│               │               │               │               │
 │                │ createUser()  │               │               │               │
 │                │──────────────>│               │               │               │
 │                │               │ generateOtp() │               │               │
 │                │               │──────────────>│               │               │
 │                │               │    otp        │               │               │
 │                │               │<──────────────│               │               │
 │                │               │               │  sendMail()   │               │
 │                │               │───────────────────────────────>│               │
 │                │               │               │               │               │
 │                │               │                    save(user) │               │
 │                │               │───────────────────────────────────────────────>│
 │                │               │                               │               │
 │   UserResponse │               │               │               │               │
 │<───────────────│               │               │               │               │
```

#### Booking Flow with Seat Locking
```
User        Controller      Service        Redis         SeatRepo      BookingRepo
 │              │              │              │              │              │
 │ POST /create │              │              │              │              │
 │─────────────>│              │              │              │              │
 │              │createBooking │              │              │              │
 │              │─────────────>│              │              │              │
 │              │              │  tryLock()   │              │              │
 │              │              │─────────────>│              │              │
 │              │              │   acquired   │              │              │
 │              │              │<─────────────│              │              │
 │              │              │              │              │              │
 │              │              │      findSeats()            │              │
 │              │              │─────────────────────────────>│              │
 │              │              │         seats               │              │
 │              │              │<─────────────────────────────│              │
 │              │              │              │              │              │
 │              │              │     markUnavailable()       │              │
 │              │              │─────────────────────────────>│              │
 │              │              │              │              │              │
 │              │              │              │    save(booking)            │
 │              │              │──────────────────────────────────────────>│
 │              │              │              │              │              │
 │              │              │  unlock()    │              │              │
 │              │              │─────────────>│              │              │
 │              │              │              │              │              │
 │ BookingResp  │              │              │              │              │
 │<─────────────│              │              │              │              │
```

---

## 4. Entity Relationship Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    AIRLINE   │       │    FLIGHT    │       │   LOCATION   │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ PK id        │       │ PK id        │       │ PK id        │
│    name      │◄──────┤ FK airlineId │       │    code      │
│    cancel-   │   1:N │    flightCode│   M:1 │    name      │
│    Policy    │       │ FK departureId├──────►│    city      │
│    baggage-  │       │ FK arrivalId │───────►│    country   │
│    Policy    │       │    aircraft  │       └──────────────┘
└──────────────┘       └──────┬───────┘
                              │ 1:N
                              ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│     USER     │       │FLIGHT_SCHEDULE       │     SEAT     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ PK id        │       │ PK id        │       │ PK id        │
│    name      │       │ FK flightId  │◄──────┤ FK scheduleId│
│    email     │       │    departure-│   1:N │    seatNumber│
│    password  │       │    DateTime  │       │    seatClass │
│    role      │       │    arrival-  │       │    seatType  │
│    isVerified│       │    DateTime  │       │    basePrice │
│ FK airlineId │       │    status    │       │    premiumFee│
└──────┬───────┘       └──────┬───────┘       │    isAvailable
       │ 1:N                  │ 1:N           │    lockedUntil
       ▼                      ▼               └──────────────┘
┌──────────────┐       ┌──────────────┐
│   BOOKING    │       │  PASSENGER   │
├──────────────┤       ├──────────────┤
│ PK id        │       │ PK id        │
│ FK userId    │◄──────┤ FK bookingId │
│ FK scheduleId│   1:N │    name      │
│    seatIds   │       │    age       │
│    totalPrice│       │    gender    │
│    status    │       │    seatNumber│
│    bookedAt  │       └──────┬───────┘
│    expiresAt │              │ 1:1
└──────┬───────┘              ▼
       │ 1:1           ┌──────────────┐
       ▼               │    TICKET    │
┌──────────────┐       ├──────────────┤
│   PAYMENT    │       │ PK id        │
├──────────────┤       │ FK userId    │
│ PK id        │       │ FK passengerId
│ FK bookingId │       │ FK flightId  │
│    amount    │       │ FK scheduleId│
│    status    │       │    createdOn │
│    method    │       └──────────────┘
│    txnId     │
└──────────────┘
```

---

## 5. Module Documentation

### 5.1 User Module

#### Entity: User
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | Unique identifier |
| name | String | Required | Full name |
| email | String | Unique, Required | Login email |
| password | String | Required | Plain text password |
| role | Role | Required | USER/ADMIN/AIRLINE_STAFF |
| isVerified | Boolean | Default: false | Email verified |
| adminVerification | Boolean | Nullable | Staff approval status |
| airlineId | Long | FK, Nullable | Staff's airline |
| createdAt | LocalDateTime | | Account creation time |

#### Service Methods
```java
// Create user with OTP email verification
UserResponse createUser(UserRequest request)

// Verify OTP and activate account
boolean verifyOtp(String email, String otp)

// Login and return JWT token
UserResponse login(LoginRequest request)

// Admin verifies airline staff
void verifyStaffByAdminFromToken(String token, String staffEmail)
```

### 5.2 Booking Module

#### Entity: Booking
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| userId | Long | User who booked |
| scheduleId | Long | Flight schedule |
| seatIds | String | Comma-separated seat IDs |
| totalPrice | BigDecimal | Sum of seat prices |
| priceLocked | BigDecimal | Price at booking time |
| status | BookingStatus | PENDING/CONFIRMED/CANCELLED |
| bookedAt | LocalDateTime | Booking timestamp |
| expiresAt | LocalDateTime | Payment deadline (+10 min) |
| version | Integer | Optimistic locking |

#### Booking Lifecycle
```
1. User selects seats
         │
         ▼
2. createBooking() called
         │
         ▼
3. Redis locks acquired for each seat (5s wait, 10s hold)
         │
         ▼
4. Seats validated (available + not locked)
         │
         ▼
5. Seats marked unavailable, lockedUntil = now + 10min
         │
         ▼
6. PENDING booking created, expiresAt = now + 10min
         │
         ▼
7. Redis locks released
         │
    ┌────┴────┐
    ▼         ▼
Payment    No Payment
 Made       (Timeout)
    │         │
    ▼         ▼
CONFIRMED  Scheduler runs
    │      every 5 min
    │         │
    ▼         ▼
 Tickets   CANCELLED
Generated  Seats Released
```

### 5.3 Seat Module

#### Seat Layout (Aircraft Configuration)
```
BUSINESS CLASS (Rows 1-5)
├── Row 1-3: EXTRA_LEGROOM
│   └── Seats A-F, Price: 300-700 + Premium: 50-150
├── Row 4-5: REGULAR
│   └── Seats A-F, Price: 300-700
└── Total: 30 seats

ECONOMY CLASS (Rows 6-27)
├── All rows: REGULAR
│   └── Seats A-F, Price: 100-300
└── Total: 132 seats

TOTAL SEATS PER FLIGHT: 162
```

#### Pricing Strategy
- Prices randomized per schedule creation
- Economy: 100-300 (random base)
- Business: 300-700 (random base)
- Extra legroom premium: 50-150 (random)

### 5.4 Payment Module

#### Payment Flow
```java
// 1. User calls payment endpoint
POST /api/v1/payments/make
{
  "bookingId": 123,
  "amount": 15000.00,
  "transactionId": "TXN_ABC123",
  "paymentMethod": "CARD"
}

// 2. System processes payment
- Validate booking exists
- Create Payment record (status = SUCCESS)
- Update booking.status = CONFIRMED
- Generate Ticket for each passenger

// 3. Return confirmation
{
  "paymentId": 456,
  "status": "SUCCESS",
  "amount": 15000.00
}
```

### 5.5 Cancellation Policy

#### Policy Format
```
REFUND_100_BEFORE=72h   → 100% refund if 72+ hours before
REFUND_75_BEFORE=48h    → 75% refund if 48-72 hours before
REFUND_50_BEFORE=24h    → 50% refund if 24-48 hours before
NO_REFUND_AFTER=24h     → 0% refund if <24 hours before
```

#### Refund Calculation
```java
hoursBeforeFlight = Duration.between(now, departureDateTime).toHours()

if (hoursBeforeFlight >= 72) refund = 100%
else if (hoursBeforeFlight >= 48) refund = 75%
else if (hoursBeforeFlight >= 24) refund = 50%
else refund = 0%

refundAmount = totalPrice * refundPercent / 100
```

---

## 6. API Reference

### 6.1 Authentication APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/users/register` | No | Register + send OTP |
| GET | `/api/users/verify-otp` | No | Verify email OTP |
| POST | `/api/users/login` | No | Login, get JWT |
| POST | `/api/users/verify-staff` | ADMIN | Approve airline staff |

### 6.2 Airline APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/airlines/create` | ADMIN | Create airline |
| GET | `/api/v1/airlines` | No | List all airlines |
| GET | `/api/v1/airlines/{id}` | No | Get airline details |

### 6.3 Flight APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/flights/add` | AIRLINE_STAFF | Create flight |
| GET | `/api/v1/flights` | AIRLINE_STAFF | List staff's flights |
| GET | `/api/v1/flights/{id}` | No | Get flight details |

### 6.4 Location APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/locations/add` | AIRLINE_STAFF | Add airport |
| GET | `/api/v1/locations` | No | List all airports |
| GET | `/api/v1/locations/{id}` | No | Get airport details |

### 6.5 Schedule APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/schedules/create` | AIRLINE_STAFF | Create schedule |
| GET | `/api/v1/schedules/{id}` | No | Get schedule details |
| GET | `/api/v1/schedules/search` | No | Search flights |

**Search Parameters:**
- `from` - Departure location ID
- `to` - Arrival location ID
- `date` - Travel date (YYYY-MM-DD)
- `sortBy` - `price` or `duration`
- `airlineId` - Filter by airline (optional)

### 6.6 Seat APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/seats/all` | No | All seats for schedule |
| GET | `/api/v1/seats/available` | No | Available seats only |
| GET | `/api/v1/seats/{id}` | No | Single seat details |

### 6.7 Booking APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/bookings/create` | USER | Create booking |
| GET | `/api/v1/bookings/my-bookings` | USER | User's bookings |
| POST | `/api/v1/bookings/cancel/{id}` | USER | Cancel booking |

**Create Booking Request:**
```json
{
  "scheduleId": 5,
  "seatIds": [101, 102],
  "passengers": [
    {"name": "John Doe", "age": 30, "gender": "M"},
    {"name": "Jane Doe", "age": 28, "gender": "F"}
  ]
}
```

### 6.8 Payment APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/payments/make` | No | Process payment |
| GET | `/api/v1/payments/{id}` | No | Get payment details |

### 6.9 Ticket APIs

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/tickets` | USER | All user tickets |
| GET | `/api/users/tickets/future` | USER | Upcoming flights |
| GET | `/api/users/tickets/past` | USER | Past flights |

---

## 7. System Design

### 7.1 Distributed Seat Locking

**Problem:** Multiple users trying to book the same seat simultaneously.

**Solution:** Redis-based distributed locking with Redisson.

```java
// Lock acquisition
RLock lock = redissonClient.getLock("lock:seat:" + seatId);
boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
// 5 seconds wait, 10 seconds hold time

if (acquired) {
    try {
        // Perform seat booking
    } finally {
        lock.unlock();
    }
}
```

**Lock Strategy:**
- **Wait Time:** 5 seconds (how long to wait for lock)
- **Hold Time:** 10 seconds (auto-release if not unlocked)
- **Key Format:** `lock:seat:{seatId}`

### 7.2 Booking Expiry Management

**Problem:** Users may abandon bookings without paying.

**Solution:** 10-minute payment window with automatic cleanup.

```
Scheduler: SeatLockExpiryScheduler
Frequency: Every 5 minutes
Actions:
  1. Find PENDING bookings where expiresAt < now
  2. Release all locked seats
  3. Delete passenger records
  4. Set booking status = CANCELLED
```

### 7.3 Flight Status Management

**Problem:** Need to track completed flights.

**Solution:** Automatic status updates via scheduler.

```
Scheduler: ScheduleStatusUpdater
Frequency: Every 5 minutes
Actions:
  1. Find SCHEDULED flights where arrivalDateTime < now
  2. Update status to COMPLETED
```

### 7.4 JWT Authentication Flow

```
┌─────────┐      ┌─────────┐      ┌─────────┐
│  Login  │      │ Extract │      │  Access │
│ Request │─────>│  Token  │─────>│   API   │
└─────────┘      └─────────┘      └─────────┘
     │                │                │
     ▼                ▼                ▼
┌─────────┐      ┌─────────┐      ┌─────────┐
│ Validate│      │ Validate│      │ Extract │
│Password │      │Signature│      │ Claims  │
└─────────┘      └─────────┘      └─────────┘
     │                │                │
     ▼                ▼                ▼
┌─────────┐      ┌─────────┐      ┌─────────┐
│ Generate│      │  Check  │      │  Check  │
│   JWT   │      │ Expiry  │      │  Role   │
└─────────┘      └─────────┘      └─────────┘
```

**Token Structure:**
```json
{
  "sub": "user@email.com",
  "userId": 123,
  "role": "USER",
  "iat": 1700000000,
  "exp": 1700036000
}
```
- **Validity:** 10 hours
- **Algorithm:** HS256

### 7.5 OTP Verification Flow

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│ Register │────>│ Generate │────>│  Store   │
│  Request │     │  6-digit │     │ In-Memory│
└──────────┘     │   OTP    │     │  5 min   │
                 └──────────┘     └──────────┘
                      │
                      ▼
                 ┌──────────┐
                 │  Send    │
                 │  Email   │
                 └──────────┘
                      │
                      ▼
┌──────────┐     ┌──────────┐     ┌──────────┐
│  User    │────>│ Verify   │────>│  Mark    │
│ Enters   │     │   OTP    │     │ Verified │
│   OTP    │     │          │     │          │
└──────────┘     └──────────┘     └──────────┘
```

---

## 8. Security Analysis

### 8.1 Current Security Issues

| Issue | Severity | Location | Recommendation |
|-------|----------|----------|----------------|
| Plaintext passwords | 🔴 Critical | User entity | Use BCrypt hashing |
| Hardcoded JWT secret | 🔴 Critical | JwtUtil.java | Use env variable |
| Hardcoded email creds | 🔴 Critical | application.properties | Use env variable |
| No input validation | 🟠 High | All endpoints | Add validation annotations |
| Long token expiry | 🟡 Medium | JwtUtil.java | Reduce to 1-2 hours |
| No rate limiting | 🟡 Medium | All endpoints | Add rate limiter |
| OTP in memory | 🟡 Medium | OtpUtil.java | Use Redis for persistence |

### 8.2 Recommended Security Improvements

```java
// 1. Password Hashing
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// In UserService
user.setPassword(passwordEncoder.encode(request.getPassword()));

// 2. Environment Variables
@Value("${JWT_SECRET}")
private String jwtSecret;

// 3. Input Validation
@NotBlank(message = "Email is required")
@Email(message = "Invalid email format")
private String email;

@Size(min = 8, message = "Password must be at least 8 characters")
private String password;
```

---

## 9. Data Flow Examples

### 9.1 Complete Booking Flow

```
1. USER SEARCHES FLIGHTS
   GET /api/v1/schedules/search?from=1&to=2&date=2024-01-20

   Response: List of available flights with prices

2. USER VIEWS AVAILABLE SEATS
   GET /api/v1/seats/available?scheduleId=5

   Response: List of seats with prices and classes

3. USER CREATES BOOKING
   POST /api/v1/bookings/create
   Authorization: Bearer {jwt_token}
   Body: {scheduleId, seatIds, passengers}

   System:
   - Acquires Redis locks
   - Validates seat availability
   - Calculates total price
   - Creates PENDING booking (10 min expiry)
   - Creates passenger records

   Response: BookingResponse with bookingId

4. USER MAKES PAYMENT
   POST /api/v1/payments/make
   Body: {bookingId, amount, transactionId, paymentMethod}

   System:
   - Creates payment record
   - Updates booking to CONFIRMED
   - Generates tickets

   Response: PaymentResponse with confirmation

5. USER RETRIEVES TICKETS
   GET /api/users/tickets
   Authorization: Bearer {jwt_token}

   Response: List of tickets with flight details
```

### 9.2 Cancellation Flow

```
1. USER REQUESTS CANCELLATION
   POST /api/v1/bookings/cancel/123
   Authorization: Bearer {jwt_token}

2. SYSTEM PROCESSES
   - Validates user owns booking
   - Validates booking is CONFIRMED
   - Fetches airline cancellation policy
   - Calculates hours until departure
   - Applies policy rules for refund %
   - Updates booking to CANCELLED
   - Releases all seats

3. RESPONSE
   {
     "bookingId": 123,
     "status": "CANCELLED",
     "refundAmount": 7500.00,
     "message": "50% refund applied"
   }
```

---

## 10. Database Schema

### 10.1 Table Definitions

```sql
-- Users Table
CREATE TABLE user_flight_test (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN', 'AIRLINE_STAFF') NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    admin_verification BOOLEAN,
    airline_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Airlines Table
CREATE TABLE airline (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    cancellation_policy TEXT,
    baggage_policy TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Locations Table
CREATE TABLE location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL
);

-- Flights Table
CREATE TABLE flight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flight_code VARCHAR(50) NOT NULL,
    airline_id BIGINT NOT NULL,
    departure_id BIGINT NOT NULL,
    arrival_id BIGINT NOT NULL,
    aircraft_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airline_id) REFERENCES airline(id),
    FOREIGN KEY (departure_id) REFERENCES location(id),
    FOREIGN KEY (arrival_id) REFERENCES location(id)
);

-- Flight Schedules Table
CREATE TABLE flight_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flight_id BIGINT NOT NULL,
    departure_date_time TIMESTAMP NOT NULL,
    arrival_date_time TIMESTAMP NOT NULL,
    duration BIGINT,
    status ENUM('SCHEDULED', 'DELAYED', 'CANCELLED', 'COMPLETED') DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flight_id) REFERENCES flight(id)
);

-- Seats Table
CREATE TABLE seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seat_number VARCHAR(10) NOT NULL,
    seat_class ENUM('ECONOMY', 'BUSINESS') NOT NULL,
    seat_type ENUM('REGULAR', 'EXTRA_LEGROOM') NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    premium_fee DECIMAL(10,2),
    is_available BOOLEAN DEFAULT TRUE,
    schedule_id BIGINT NOT NULL,
    locked_until TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES flight_schedule(id)
);

-- Bookings Table
CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    seat_ids VARCHAR(500),
    total_price DECIMAL(10,2),
    price_locked DECIMAL(10,2),
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED') NOT NULL,
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    version INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user_flight_test(id),
    FOREIGN KEY (schedule_id) REFERENCES flight_schedule(id)
);

-- Passengers Table
CREATE TABLE passenger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES booking(id)
);

-- Payments Table
CREATE TABLE payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    transaction_id VARCHAR(255),
    payment_method ENUM('UPI', 'CARD', 'NET_BANKING', 'WALLET'),
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(id)
);

-- Tickets Table
CREATE TABLE ticket_new (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    flight_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_flight_test(id),
    FOREIGN KEY (passenger_id) REFERENCES passenger(id),
    FOREIGN KEY (flight_id) REFERENCES flight(id),
    FOREIGN KEY (schedule_id) REFERENCES flight_schedule(id)
);
```

### 10.2 Indexes (Recommended)

```sql
CREATE INDEX idx_user_email ON user_flight_test(email);
CREATE INDEX idx_booking_user ON booking(user_id);
CREATE INDEX idx_booking_status ON booking(status);
CREATE INDEX idx_seat_schedule ON seat(schedule_id);
CREATE INDEX idx_seat_available ON seat(schedule_id, is_available);
CREATE INDEX idx_schedule_flight ON flight_schedule(flight_id);
CREATE INDEX idx_schedule_status ON flight_schedule(status);
```

---

## 11. Production Considerations

### 11.1 Environment Configuration

```properties
# application-prod.properties

# Database (Use connection pooling)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.hikari.maximum-pool-size=10

# Redis
spring.redis.host=${REDIS_HOST}
spring.redis.port=${REDIS_PORT}

# Email
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

### 11.2 Deployment Checklist

- [ ] Move all credentials to environment variables
- [ ] Implement password hashing (BCrypt)
- [ ] Add input validation on all endpoints
- [ ] Configure HTTPS only
- [ ] Set up proper CORS for production domain
- [ ] Implement rate limiting
- [ ] Add comprehensive logging
- [ ] Set up health checks (/actuator/health)
- [ ] Configure database connection pooling
- [ ] Set up Redis for OTP storage (persistence)
- [ ] Implement token refresh mechanism
- [ ] Add API versioning
- [ ] Set up monitoring and alerting

### 11.3 Scaling Considerations

```
                    ┌─────────────┐
                    │   Load      │
                    │  Balancer   │
                    └──────┬──────┘
                           │
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │   App       │ │   App       │ │   App       │
    │ Instance 1  │ │ Instance 2  │ │ Instance 3  │
    └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
           │               │               │
           └───────────────┼───────────────┘
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
       ┌─────────────┐          ┌─────────────┐
       │   Redis     │          │  MariaDB    │
       │  Cluster    │          │  Primary +  │
       │             │          │  Replicas   │
       └─────────────┘          └─────────────┘
```

---

## Appendix A: Dependencies

```kotlin
// build.gradle.kts
dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("com.h2database:h2")

    // Redis
    implementation("org.redisson:redisson-spring-boot-starter:3.27.2")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Utilities
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

---

## Appendix B: Running the Application

### Prerequisites
- Java 17+
- Redis running on localhost:6379
- MariaDB database access

### Commands
```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# Run with profile
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Docker (Redis)
```bash
docker run -d -p 6379:6379 --name redis redis
```

---

**Document Version:** 1.0
**Last Updated:** January 2026
**Author:** Auto-generated Documentation
