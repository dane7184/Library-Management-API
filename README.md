# Library Management System

Multi-module Spring Boot 3 / Java 21 project — PostgreSQL 18, Flyway, Keycloak, Docker.

## Modules

| Module              | Package                    | Responsibility                                   |
|---------------------|----------------------------|--------------------------------------------------|
| `library-common`    | `com.library.common`       | BaseEntity, enums, ApiResponse, exceptions       |
| `library-employee`  | `com.library.employee`     | 1. Master Data Employee                          |
| `library-customer`  | `com.library.customer`     | 2. Master Data Customer                          |
| `library-book`      | `com.library.book`         | 3. Master Data Book                              |
| `library-borrow`    | `com.library.borrow`       | 4. Borrow Book Feature                           |
| `library-return`    | `com.library.bookreturn`   | 5. Return Book Feature (`return` is a keyword)   |
| `library-scheduler` | `com.library.scheduler`    | 6. Scheduler — mark overdue borrows as OVER      |
| `library-report`    | `com.library.report`       | 7. Reports — Excel export (Apache POI)           |
| `library-app`       | `com.library`              | Boot app, security (Keycloak), config, Flyway    |

## Module dependencies

```
library-app ──► employee, customer, book, borrow, return, scheduler, report
report     ──► book, borrow, return
scheduler  ──► borrow
return     ──► borrow, book, employee
borrow     ──► book, customer, employee
employee / customer / book ──► common
```

Each feature module follows the same layout:

```
controller/  dto/  entity/  mapper/  repository/  service/  service/impl/
```

## Run

```bash
# Everything in Docker
docker compose up -d --build

# Or: infra in Docker, app from the IDE
docker compose up -d postgres keycloak
./gradlew :library-app:bootRun --args="--spring.profiles.active=dev"
```

- API / Swagger: http://localhost:8080/swagger-ui.html
- Keycloak admin: http://localhost:8180 (admin / admin)
- PostgreSQL (Docker): `localhost:5433` / db `library_db` / `library` / `library`
  (host port 5433, so it doesn't clash with a locally installed PostgreSQL on 5432)

### Get a token (local dev users from `docker/keycloak/library-realm.json`)

Log in through the API (it forwards to Keycloak for you):

```bash
curl -X POST http://localhost:8080/api/v1/auth/login   -H "Content-Type: application/json"   -d '{"username":"admin","password":"admin123"}'
# -> data.accessToken (5 min), data.refreshToken (30 min)

curl -X POST http://localhost:8080/api/v1/auth/refresh   -H "Content-Type: application/json"   -d '{"refreshToken":"<refreshToken>"}'
```

Then call the API with `Authorization: Bearer <accessToken>`.
Users: `admin` / `admin123` (ADMIN, LIBRARIAN), `librarian` / `librarian123` (LIBRARIAN).

## Endpoints

| Feature   | Base path            |
|-----------|----------------------|
| Auth      | `/api/v1/auth/login`, `/api/v1/auth/refresh` (public) |
| Employee  | `/api/v1/employees`  |
| Customer  | `/api/v1/customers`  |
| Book      | `/api/v1/books`      |
| Borrow    | `/api/v1/borrows`    |
| Return    | `/api/v1/returns`    |
| Reports   | `/api/v1/reports/books`, `/borrows`, `/returns` (xlsx) |

CRUD per feature: `POST /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` (soft delete), `GET /`.

Book search (paged, all filters optional, max page size 100):
`GET /api/v1/books/search?title=java&author=martin&year=2008&status=AVAILABLE&page=0&size=20&sort=title,asc`
