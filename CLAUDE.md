# CLAUDE.md

Project: KIHAN

Purpose:
- Manage recurring schedules and deadlines.
- Auto-generate daily executions and track done/delayed.

MVP Scope:
- Deadline (ONE_TIME, RECURRING)
- RecurrenceRule (VO)
- Execution (Entity)
- Scheduler creates daily executions
- No external APIs

Out of Scope:
- Weather/context
- Subscriptions/expenses
- Inventory counts
- Sharing/collaboration
- Analytics/dashboard
- Cron expressions

Domain Decisions:
- Deadline = Aggregate Root
- RecurrenceRule = Value Object (immutable)
- Execution = Independent aggregate entity (references Deadline)
- Execution created via scheduler/service workflows

Tech & Style:
- Java 25
- Minimal output
- No explanations unless requested
- Code or spec only
- No setters in entities
- Lombok allowed
- Early return preferred

## Build & Run Commands

```bash
# Build
./gradlew build

# Run application
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.example.kihan.KihanApplicationTests"

# Clean build
./gradlew clean build
```

## Tech Stack

- **Java 25** / **Spring Boot 4.0.2** / **Gradle**
- **Spring Security** with JWT (jjwt 0.12.6) + HttpOnly cookie refresh tokens
- **Spring Data JPA** with H2 in-memory database (MySQL mode)
- **Lombok** for boilerplate reduction
- **Bean Validation** for request validation

## Architecture

Pragmatic layered architecture — selectively applies DDD/Hexagonal concepts only where justified. Core principle: **defer abstraction until actual change pressure appears**.

### Layer Structure (`com.example.kihan`)

```
presentation/  → Controllers, request/response DTOs, exception handling
application/   → Services (command/query split), facades, application DTOs
domain/        → Entities, value objects, domain exceptions (no framework deps)
infrastructure/ → Config, repositories, security (JWT, cookies), scheduling
```

### Key Design Decisions

- **Domain entity = JPA entity** (no separate mapping layer). Only split when JPA constraints distort domain expression.
- **CQRS-lite**: Services split into `command/` (writes) and `query/` (reads) packages.
- **Facades** orchestrate multi-service workflows (e.g., `LoginFacade` chains authentication → token generation → refresh token issuance). Transactional boundaries live here.
- **UserFinder interface** in application layer prevents circular dependencies between domain services. Implemented by `UserQueryService`.
- **Domain exceptions** are framework-agnostic. `ExceptionStatusMapper` chain in presentation layer maps them to HTTP status codes. New domain → add a mapper implementing `ExceptionStatusMapper`.
- **Error responses** use RFC 7807 ProblemDetail format throughout (including security handlers).
- **Soft delete** via `BaseEntity.deletedAt` — all queries filter with `deletedAtIsNull()`.
- **Audit** via JPA `@CreatedBy`/`@LastModifiedBy` backed by `AuditorAwareImpl` reading `SecurityContext`.

### Security Flow

- Access token in `Authorization: Bearer` header, refresh token in HttpOnly cookie.
- `JwtProvider` handles raw JWT parsing/generation. Application layer accesses it through `JwtTokenGenerator` and `RefreshTokenVerifier` (never directly).
- Refresh tokens are stored server-side in DB; `RefreshTokenCleanupScheduler` periodically purges expired ones.

### Public Endpoints (no auth required)

- `POST /api/users/register`
- `POST /api/auth/login`
- `POST /api/auth/reissue`
- `/h2-console/**`

## Configuration

External config in `src/main/resources/application.yml`. Type-safe properties via `@ConfigurationProperties` records: `JwtProperties`, `CookieProperties`, `CorsProperties`.
