# 🐦 Chirp Backend

A modular chat backend built with **Spring Boot** and **Kotlin**, backed by **PostgreSQL**, **Redis** and **RabbitMQ**.  
Supports user registration with email verification, JWT authentication with refresh tokens, password reset, rate limiting, and group chats with paginated message history.

---

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [API Reference](#-api-reference)
- [License](#-license)

---

## 🛠️ Tech Stack

| Technology               | Description                                   |
|--------------------------|-----------------------------------------------|
| Kotlin                   | Primary language                              |
| Spring Boot 4            | Application framework                         |
| Spring Security + JWT    | Stateless authentication & authorization      |
| Spring Data JPA          | Persistence layer                             |
| PostgreSQL               | Relational database                           |
| Redis                    | IP and email rate limiting (Lua scripts)      |
| RabbitMQ                 | Event-driven communication between modules    |
| Spring Mail + Thymeleaf  | Transactional emails (verification, reset)    |
| Gradle (Kotlin DSL)      | Build system with convention plugins          |

---

## 🚀 Getting Started

### Prerequisites

- JDK 21+
- PostgreSQL instance
- Redis instance
- RabbitMQ instance
- SMTP account (e.g. [Mailgun](https://www.mailgun.com/))
- Gradle (or use the included `./gradlew` wrapper)

### Clone the Repository

```bash
git clone https://github.com/M0bileDev/ChirpBackend.git
cd ChirpBackend
```

### Configure the Application

Connection settings live in `app/src/main/resources/application.yml`. Update the hosts and usernames for PostgreSQL, Redis, RabbitMQ and SMTP to match your environment.

Secrets are read from environment variables:

| Variable             | Description                           |
|----------------------|---------------------------------------|
| `POSTGRES_PASSWORD`  | PostgreSQL password                   |
| `REDIS_PASSWORD`     | Redis password                        |
| `RABBITMQ_PASSWORD`  | RabbitMQ password                     |
| `MAILGUN_PASSWORD`   | SMTP password                         |
| `JWT_SECRET_BASE64`  | Base64-encoded secret for signing JWTs |

### Profiles

| Profile | Schema (`ddl-auto`) | JWT expiry | IP rate limit | Behind nginx |
|---------|---------------------|------------|---------------|--------------|
| `dev`   | `update`            | 1000 min   | off           | no           |
| `prod`  | `validate`          | 15 min     | on            | yes          |

### Run the Application

```bash
./gradlew :app:bootRun --args='--spring.profiles.active=dev'
```

The app will start on `http://localhost:8080` by default.

### Build

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

---

## 📁 Project Structure

The project is a multi-module Gradle build. Each feature module follows the same `api` / `domain` / `infra` / `service` layering.

```
ChirpBackend/
├── app/                  # Spring Boot entry point, security & Redis config, application*.yml
├── user/                 # Auth: register, login, JWT, refresh tokens, email verification, password reset, rate limiting
├── chat/                 # Chats, participants and messages
├── notification/         # Email sending + Thymeleaf email templates
├── common/               # Shared types (UserId, ChatId...), events, RabbitMQ config, common exceptions
├── build-logic/          # Gradle convention plugins (chirp.kotlin-common, chirp.spring-boot-service, chirp.spring-boot-app)
└── gradle/
    └── libs.versions.toml  # Version catalog
```

Inside each feature module:

```
src/main/kotlin/com/mobidev/studio/chirp/
├── api/        # REST controllers, DTOs, requests, exception handlers, mappers
├── domain/     # Models, domain events, exceptions
├── infra/      # JPA entities & repositories, messaging, rate limiting, security
└── service/    # Business logic
```

### Event Flow

Modules talk to each other through **RabbitMQ** events (`UserEvent`, `ChatEvent`):

| Queue                       | Consumer       | Purpose                                         |
|-----------------------------|----------------|-------------------------------------------------|
| `notification.user.events`  | `notification` | Send verification and password reset emails     |
| `chat.user.events`          | `chat`         | Create chat participants for new users          |

---

## 📡 API Reference

All endpoints (except `/api/v1/auth/**`) require a valid **Bearer token** in the `Authorization` header:

```
Authorization: Bearer <access_token>
```

All IDs are **UUIDs**. Timestamps are **ISO-8601** (`Instant`).

---

### 🔐 Authentication

#### Register

```http
POST /api/v1/auth/register
```

| Body Field | Type     | Description                                                 |
|------------|----------|-------------------------------------------------------------|
| `email`    | `String` | Valid email address                                         |
| `username` | `String` | 3–20 characters                                             |
| `password` | `String` | Min. 8 characters, at least one digit or special character |

**Response** `200 OK`

```json
{
  "id": "3f2b8c1e-7a4d-4e2b-9c1a-2d5e6f7a8b9c",
  "email": "john@example.com",
  "username": "john",
  "hasVerifiedEmail": false
}
```

> A verification email is sent after registration. Rate limit: 10 requests / hour per IP.

---

#### Login

```http
POST /api/v1/auth/login
```

| Body Field | Type     | Description    |
|------------|----------|----------------|
| `email`    | `String` | Your email     |
| `password` | `String` | Your password  |

**Response** `200 OK`

```json
{
  "user": {
    "id": "3f2b8c1e-7a4d-4e2b-9c1a-2d5e6f7a8b9c",
    "email": "john@example.com",
    "username": "john",
    "hasVerifiedEmail": true
  },
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

> Email must be verified before login. Rate limit: 10 requests / hour per IP.

---

#### Refresh Token

```http
POST /api/v1/auth/refresh
```

| Body Field     | Type     | Description           |
|----------------|----------|-----------------------|
| `refreshToken` | `String` | Current refresh token |

**Response** `200 OK` — returns a new user + token pair (same shape as login).

---

#### Logout

```http
POST /api/v1/auth/logout
```

| Body Field     | Type     | Description                |
|----------------|----------|----------------------------|
| `refreshToken` | `String` | Refresh token to invalidate |

**Response** `200 OK`

---

#### Verify Email

```http
GET /api/v1/auth/verify?token={token}
```

| Query Param | Type     | Description                     |
|-------------|----------|---------------------------------|
| `token`     | `String` | Token from the verification email |

**Response** `200 OK`

---

#### Resend Verification Email

```http
POST /api/v1/auth/resend-verification
```

| Body Field | Type     | Description    |
|------------|----------|----------------|
| `email`    | `String` | Account email  |

**Response** `200 OK`

> Rate limited per IP and per email.

---

#### Forgot Password

```http
POST /api/v1/auth/forgot-password
```

| Body Field | Type     | Description    |
|------------|----------|----------------|
| `email`    | `String` | Account email  |

**Response** `200 OK` — sends a password reset email.

---

#### Reset Password

```http
POST /api/v1/auth/reset-password
```

| Body Field    | Type     | Description                      |
|---------------|----------|----------------------------------|
| `token`       | `String` | Token from the reset email       |
| `newPassword` | `String` | New password (same rules as register) |

**Response** `200 OK`

---

#### Change Password

```http
POST /api/v1/auth/change-password
```

🔒 Requires Bearer token.

| Body Field    | Type     | Description       |
|---------------|----------|-------------------|
| `oldPassword` | `String` | Current password  |
| `newPassword` | `String` | New password      |

**Response** `200 OK`

---

### 💬 Chats

#### Get Chats for Current User

```http
GET /api/v1/chat
```

**Response** `200 OK`

```json
[
  {
    "id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
    "participants": [
      {
        "userId": "3f2b8c1e-7a4d-4e2b-9c1a-2d5e6f7a8b9c",
        "username": "john",
        "email": "john@example.com",
        "profilePictureUrl": null
      }
    ],
    "lastActivityAt": "2026-10-26T10:30:00Z",
    "lastMessage": {
      "id": "9f8e7d6c-5b4a-3f2e-1d0c-9b8a7f6e5d4c",
      "chatId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "content": "Hello!",
      "createdAt": "2026-10-26T10:30:00Z",
      "senderId": "3f2b8c1e-7a4d-4e2b-9c1a-2d5e6f7a8b9c"
    },
    "creator": {
      "userId": "3f2b8c1e-7a4d-4e2b-9c1a-2d5e6f7a8b9c",
      "username": "john",
      "email": "john@example.com",
      "profilePictureUrl": null
    }
  }
]
```

---

#### Get Chat by ID

```http
GET /api/v1/chat/{chatId}
```

| Path Param | Type   | Description |
|------------|--------|-------------|
| `chatId`   | `UUID` | Chat ID     |

**Response** `200 OK` — returns the chat object. `404 Not Found` if the chat does not exist.

---

#### Create Chat

```http
POST /api/v1/chat
```

| Body Field     | Type         | Description                                   |
|----------------|--------------|-----------------------------------------------|
| `otherUserIds` | `List<UUID>` | Other participants (at least 1, creator is added automatically) |

**Response** `200 OK` — returns the created chat object.

---

#### Add Participants

```http
POST /api/v1/chat/{chatId}/add
```

| Path Param | Type   | Description |
|------------|--------|-------------|
| `chatId`   | `UUID` | Chat ID     |

| Body Field | Type        | Description                |
|------------|-------------|----------------------------|
| `userIds`  | `Set<UUID>` | Users to add (at least 1)  |

**Response** `200 OK` — returns the updated chat object.

---

#### Leave Chat

```http
DELETE /api/v1/chat/{chatId}/leave
```

| Path Param | Type   | Description |
|------------|--------|-------------|
| `chatId`   | `UUID` | Chat ID     |

**Response** `200 OK`

---

#### Get Chat Messages

```http
GET /api/v1/chat/{chatId}/messages?before={instant}&pageSize={size}
```

| Path Param | Type   | Description |
|------------|--------|-------------|
| `chatId`   | `UUID` | Chat ID     |

| Query Param | Type      | Description                                        |
|-------------|-----------|----------------------------------------------------|
| `before`    | `Instant` | Optional. Return messages older than this time (cursor) |
| `pageSize`  | `Int`     | Optional. Default `20`                             |

**Response** `200 OK` — returns a list of message objects.

---

### 👥 Participants

#### Find Participant

```http
GET /api/v1/chat/participants?query={usernameOrEmail}
```

| Query Param | Type     | Description                                                       |
|-------------|----------|-------------------------------------------------------------------|
| `query`     | `String` | Optional. Username or email. If omitted, returns the current user |

**Response** `200 OK`

```json
{
  "userId": "3f2b8c1e-7a4d-4e2b-9c1a-2d5e6f7a8b9c",
  "username": "john",
  "email": "john@example.com",
  "profilePictureUrl": null
}
```

`404 Not Found` if no user matches.

---

### ✉️ Messages

#### Delete Message

```http
DELETE /api/v1/messages/{messageId}
```

| Path Param  | Type   | Description |
|-------------|--------|-------------|
| `messageId` | `UUID` | Message ID  |

**Response** `200 OK`

---

### ⚠️ Errors

| Status                  | When                                                        |
|-------------------------|-------------------------------------------------------------|
| `400 Bad Request`       | Validation failed (`VALIDATION_ERROR`) or invalid chat size |
| `401 Unauthorized`      | Missing/invalid token, wrong credentials, email not verified |
| `403 Forbidden`         | User is not allowed to access the resource                  |
| `404 Not Found`         | User, chat, participant or message not found                |
| `409 Conflict`          | User already exists, or new password equals the old one    |
| `429 Too Many Requests` | Rate limit exceeded                                         |

Validation error example:

```json
{
  "code": "VALIDATION_ERROR",
  "errors": [
    "Username must be between 3 and 20 characters"
  ]
}
```

---

## 📄 License

This project is licensed under the [Apache 2.0 License](LICENSE).
