# Spring Boot Enterprise REST & Security Platform

A production-ready Spring Boot platform demonstrating REST API engineering, role-based stateless security, database query optimization, in-memory caching, symmetric data encryption, and distributed request tracing. Includes an integrated light-themed Single Page Application dashboard.

---

## 1. System Overview

This application serves as a reference architecture for scalable backend systems built with Spring Boot 3 and Java 21. It implements standard software engineering practices across six core functional domains:

1. **REST API Design & Validation**: Structured Controller-Service-Repository layers with Java Bean Validation and predictable response envelopes.
2. **Global Exception Handling & Logging**: Unified error handling via `@RestControllerAdvice` and structured SLF4J request duration tracking.
3. **Scalable Data Retrieval**: Server-side pagination and dynamic sorting over Spring Data JPA repositories.
4. **Query Optimization & In-Memory Caching**: Elimination of the N+1 query problem using `JOIN FETCH`, high-speed caching with Caffeine, and native SQL analytical aggregations.
5. **Stateless Authentication & RBAC**: JWT access token validation, password hashing with BCrypt, and method-level access control with `@PreAuthorize`.
6. **Data Encryption & Token Lifecycle**: AES-256-GCM encryption for credentials at rest, alongside database-persisted refresh token rotation.

---

## 2. Technology Stack

| Layer / Concern | Technology / Library | Description |
| :--- | :--- | :--- |
| **Language** | Java 21 (LTS) | Modern Java runtime |
| **Framework** | Spring Boot 3.3.4 | Core enterprise application framework |
| **Web & REST** | Spring MVC, Tomcat Embed | HTTP request dispatching and RESTful endpoints |
| **Persistence** | Spring Data JPA, Hibernate 6 | Object-relational mapping and database access |
| **Database** | H2 Database (In-Memory) | Zero-configuration relational database with web console |
| **Security** | Spring Security 6 | Security filter chains, BCrypt hashing, and method security |
| **Tokens** | JJWT (io.jsonwebtoken 0.12.5) | HMAC-SHA256 stateless access tokens |
| **Cryptography** | Java Cryptography Architecture (JCA) | AES-256-GCM authenticated symmetric encryption |
| **Caching** | Spring Cache + Caffeine 3.1.8 | High-performance in-memory cache with telemetry |
| **Validation** | Jakarta Bean Validation / Hibernate Validator | Declarative request payload constraints |
| **Observability** | SLF4J + Logback + MDC | Structured request logging and correlation ID tracking |
| **Frontend** | Vanilla HTML5 / CSS3 / JavaScript | Modern light theme Single Page Application |

---

## 3. Project Directory Structure

```
spring-boot-integrated-platform/
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/example/platform/
│   │   │   ├── PlatformApplication.java
│   │   │   ├── common/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── PagedResponse.java
│   │   │   ├── config/
│   │   │   │   ├── CacheConfig.java
│   │   │   │   ├── CorrelationIdFilter.java
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── RequestLoggingFilter.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CredentialController.java
│   │   │   │   ├── PostController.java
│   │   │   │   ├── ScheduleController.java
│   │   │   │   └── SystemController.java
│   │   │   ├── dto/
│   │   │   │   ├── AuthRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── BenchmarkResultDto.java
│   │   │   │   ├── CommentDto.java
│   │   │   │   ├── OAuthCredentialRequest.java
│   │   │   │   ├── OAuthCredentialResponse.java
│   │   │   │   ├── PostAnalyticsDto.java
│   │   │   │   ├── PostRequest.java
│   │   │   │   ├── PostResponse.java
│   │   │   │   ├── RefreshTokenRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── ScheduleRequest.java
│   │   │   │   └── ScheduleResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── CryptoException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UnauthorizedException.java
│   │   │   ├── model/
│   │   │   │   ├── Comment.java
│   │   │   │   ├── OAuthCredential.java
│   │   │   │   ├── Post.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Schedule.java
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── CommentRepository.java
│   │   │   │   ├── OAuthCredentialRepository.java
│   │   │   │   ├── PostRepository.java
│   │   │   │   ├── RefreshTokenRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── ScheduleRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/
│   │   │   │   ├── AesEncryptionService.java
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtService.java
│   │   │   │   └── UserPrincipal.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── OAuthCredentialService.java
│   │   │       ├── PostService.java
│   │   │       ├── ScheduleService.java
│   │   │       └── SystemBenchmarkService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── logback-spring.xml
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/style.css
│   │           └── js/app.js
│   └── test/
│       └── java/com/example/platform/
│           └── PlatformApplicationTests.java
```

---

## 4. Setup and Execution

### Prerequisites
- JDK 17 or JDK 21 installed.
- PowerShell or standard terminal.

### Running the Application

1. Open PowerShell and navigate to the project directory:
   ```powershell
   cd C:\Users\pande\.gemini\antigravity\scratch\spring-boot-integrated-platform
   ```

2. Set `JAVA_HOME` if not configured globally:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
   ```

3. Launch the application:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Access the web dashboard:
   Open your browser and visit: **`http://localhost:8080`**

5. Access the H2 Database Console (Optional):
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:platformdb`
   - User Name: `sa`
   - Password: `password`

---

## 5. Pre-Seeded Accounts

The database bootstraps automatically with sample users, posts, comments, schedules, and encrypted OAuth keys:

| Account | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | `ROLE_ADMIN`, `ROLE_USER` | Full administrative control, vault secret decryption, global post edits/deletions |
| **`dev_user`** | `user123` | `ROLE_USER` | Create posts, edit own posts, add comments, schedule broadcasts, view own credentials |

---

## 6. Architecture & Implementation Highlights

### A. Unified Response Envelopes
Every API endpoint returns a standardized JSON structure. Success responses use `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Post retrieved successfully",
  "data": { ... },
  "timestamp": "2026-10-05T21:30:00.000",
  "correlationId": "8f3b9c21-12a4-4b52-9df7-28e404b901fc"
}
```

Error responses handled by `GlobalExceptionHandler` return structured details with field validation breakdowns:

```json
{
  "success": false,
  "status": 400,
  "error": "Validation Error",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/posts",
  "validationErrors": {
    "title": "Title must be between 3 and 150 characters",
    "content": "Content must be between 10 and 5000 characters"
  },
  "timestamp": "2026-10-05T21:30:05.120",
  "correlationId": "8f3b9c21-12a4-4b52-9df7-28e404b901fc"
}
```

### B. Scalable Read APIs with Pagination and Sorting
Endpoints accept `page`, `size`, `sortBy`, `sortDir`, `category`, and `search` parameters:

```
GET /api/v1/posts?page=0&size=6&sortBy=createdAt&sortDir=desc&category=ENGINEERING&search=spring
```

Returns `PagedResponse<T>` containing records and pagination metadata (`pageNumber`, `pageSize`, `totalElements`, `totalPages`, `isLast`).

### C. N+1 Query Resolution and In-Memory Caching
- **Unoptimized Path**: Lazy associations trigger separate queries for each author and comment collection.
- **Optimized Path**: `PostRepository.findAllWithJoinFetch()` runs a single query with `JOIN FETCH p.author LEFT JOIN FETCH p.comments`, eliminating database round-trips.
- **Caffeine Cache**: Method-level `@Cacheable(value = "posts", key = "#id")` caches hot post reads. Cache eviction (`@CacheEvict`) runs on write and update mutations.
- **Native SQL**: Direct database aggregations executed using SQL `GROUP BY` via `PostRepository.getCategoryAnalyticsNative()`.

### D. Symmetric AES-256-GCM Encryption
Third-party OAuth secrets and access tokens are encrypted with AES-256 in GCM mode before saving to the database. Each encryption generates a random 12-byte initialization vector (IV) stored alongside the ciphertext, providing confidentiality and authentication tag integrity.

### E. Token Lifecycle & Rotation
- **Access Token**: Short-lived JWT (15 minutes expiration) signed with HMAC-SHA256 containing user roles.
- **Refresh Token**: Long-lived UUID (7 days expiration) stored in the database.
- **Rotation**: Refreshing tokens revokes the old token and issues a new pair to prevent token replay attacks.

---

## 7. Complete API Reference

### Authentication Endpoints (`/api/v1/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register a new user account |
| `POST` | `/api/v1/auth/login` | Public | Authenticate and obtain JWT access and refresh tokens |
| `POST` | `/api/v1/auth/refresh-token` | Public | Exchange refresh token for a rotated new pair |
| `POST` | `/api/v1/auth/logout` | Authenticated | Revoke refresh token and terminate active session |

### Post Management Endpoints (`/api/v1/posts`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/posts` | Public | Paginated and sorted post feed with filters |
| `GET` | `/api/v1/posts/{id}` | Public | Retrieve single post by ID (Caffeine cached) |
| `POST` | `/api/v1/posts` | Authenticated | Create a new post with Bean Validation |
| `PUT` | `/api/v1/posts/{id}` | Author / Admin | Update existing post details |
| `DELETE` | `/api/v1/posts/{id}` | Author / Admin | Delete post and associated comments |
| `POST` | `/api/v1/posts/{id}/comments` | Authenticated | Add a comment to an existing post |
| `GET` | `/api/v1/posts/analytics/native` | Public | Retrieve post category analytics via native SQL |

### Social Scheduling Endpoints (`/api/v1/schedules`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/schedules` | Public | List all scheduled broadcast events |
| `GET` | `/api/v1/schedules/{id}` | Public | Retrieve schedule details by ID |
| `POST` | `/api/v1/schedules` | Authenticated | Schedule post for platform with `@Future` validation |
| `PATCH` | `/api/v1/schedules/{id}/status` | Authenticated | Update publishing status (PENDING, PUBLISHED, FAILED) |
| `DELETE` | `/api/v1/schedules/{id}` | Authenticated | Remove scheduled broadcast |

### AES Encrypted Credentials Vault (`/api/v1/credentials`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/credentials` | Authenticated | Encrypt and store OAuth client secrets and tokens |
| `GET` | `/api/v1/credentials` | Authenticated | List credentials for current user (masked values) |
| `GET` | `/api/v1/credentials/{id}/decrypt` | Author / Admin | Decrypt and reveal plaintext secrets |
| `DELETE` | `/api/v1/credentials/{id}` | Author / Admin | Remove stored credential record |

### System, Benchmarks & Observability (`/api/v1/system`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/system/trace-info` | Public | Inspect current MDC correlation ID and server status |
| `GET` | `/api/v1/system/cache-stats` | Public | Retrieve Caffeine cache hit count, miss count, and ratio |
| `POST` | `/api/v1/system/cache-clear` | Public | Evict all cached items |
| `GET` | `/api/v1/system/benchmark/n-plus-one` | Public | Execute unoptimized lazy load queries for timing |
| `GET` | `/api/v1/system/benchmark/join-fetch` | Public | Execute optimized single-query JOIN FETCH for timing |
| `GET` | `/api/v1/system/test-400` | Public | Trigger 400 Bad Request exception response |
| `GET` | `/api/v1/system/test-404` | Public | Trigger 404 Not Found exception response |
| `GET` | `/api/v1/system/test-403` | Admin Only | Trigger 403 Forbidden AccessDeniedException response |
| `GET` | `/api/v1/system/test-500` | Public | Trigger 500 Internal Server Error response |

---

## 8. Running Automated Tests

Execute the automated test suite using Maven:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
.\mvnw.cmd test
```

The test suite validates:
- Spring application context initialization
- AES-256-GCM symmetric encryption and decryption correctness
- Authentication and JWT token issuance and signature validation
- Post creation, validation constraints, and Caffeine cache integration

---

## 9. Deployment Guide (GitHub, Vercel & Cloud)

### A. Pushing to GitHub
1. Create a new repository on [GitHub](https://github.com/new).
2. Link your local repository and push:
   ```powershell
   git remote add origin https://github.com/<YOUR_USERNAME>/<REPO_NAME>.git
   git branch -M main
   git push -u origin main
   ```

### B. Deploying Frontend to Vercel
The repository includes a root `vercel.json` pre-configured to deploy the light theme Single Page Application from `src/main/resources/static`:
1. Import your GitHub repository on [Vercel Dashboard](https://vercel.com/new).
2. Framework Preset: **Other**.
3. Root Directory: `./` (Vercel automatically detects `vercel.json` and routes to the static assets).
4. Click **Deploy**.

### C. Deploying Spring Boot Backend (Docker / Render / Railway)
To host the backend API alongside your Vercel frontend:
1. Use the included multi-stage `Dockerfile`.
2. Connect your GitHub repository to [Render](https://render.com) or [Railway](https://railway.app).
3. Select **Docker** as the deployment environment.
4. Set the port to `8080`.

