# Epic 3: Import Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Import JSONPlaceholder Users and Posts into the local PostgreSQL schema, detect unchanged versus changed source data, and expose a sync endpoint that the frontend can call later.

**Architecture:** The backend will use a small import pipeline built around `HttpClient` for external requests, mapper classes for JSON-to-domain conversion, a hash service for change detection, and JDBC-based persistence against the existing `users`, `posts`, and `raw_source` tables. The API surface will start with `POST /api/sync` so the frontend can trigger the workflow, while the rest of the import logic stays behind services and repositories that are easy to unit test.

**Tech Stack:** Spring Boot 4, Java 21, Spring WebMVC, Spring JDBC/JdbcTemplate, Jackson, PostgreSQL, Testcontainers, OkHttp `MockWebServer` for deterministic external API tests.

---

### Task 1: Add Import-Focused Tests First

**Files:**
- Create: `backend/src/test/java/com/example/userspostsbrowser/importflow/JsonPlaceholderClientTests.java`
- Create: `backend/src/test/java/com/example/userspostsbrowser/importflow/UserMapperTests.java`
- Create: `backend/src/test/java/com/example/userspostsbrowser/importflow/PostMapperTests.java`
- Create: `backend/src/test/java/com/example/userspostsbrowser/importflow/PayloadHashServiceTests.java`
- Create: `backend/src/test/java/com/example/userspostsbrowser/importflow/SyncServiceTests.java`
- Create: `backend/src/test/java/com/example/userspostsbrowser/importflow/SyncControllerTests.java`

- [ ] **Step 1: Write the failing tests**

```java
// Example expectations:
// - client returns parsed user/post lists from JSONPlaceholder-shaped responses
// - mapper preserves top-level and nested fields
// - hash service returns the same hash for the same payload and a different hash for changed payloads
// - first sync creates rows in raw_source, users, and posts
// - second sync with unchanged data reports no_change and does not duplicate rows
// - changed source payload updates raw_source and business rows
// - POST /api/sync returns a clear JSON result
```

- [ ] **Step 2: Run the targeted tests to verify they fail**

Run:
```bash
cd backend
./mvnw -Dtest='JsonPlaceholderClientTests,UserMapperTests,PostMapperTests,PayloadHashServiceTests,SyncServiceTests,SyncControllerTests' test
```

Expected: compilation or assertion failures because the import flow code does not exist yet.

- [ ] **Step 3: Keep only the tests that express one behavior each**

Split any broad test into smaller tests so each failure points to one missing behavior.

- [ ] **Step 4: Commit the red tests**

```bash
git add backend/src/test/java/com/example/userspostsbrowser/importflow
git commit -m "test: add Epic 3 import flow coverage"
```

### Task 2: Add Import DTOs, Client, and Mappers

**Files:**
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/JsonPlaceholderClient.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/UserMapper.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/PostMapper.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/dto/JsonPlaceholderUserDto.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/dto/JsonPlaceholderPostDto.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/dto/ImportedUserRecord.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/dto/ImportedPostRecord.java`

- [ ] **Step 1: Write the minimal DTO and client code**

```java
// DTOs should match the JSONPlaceholder shape, including nested address/company structures for users.
// Client should fetch from configurable URLs and deserialize into lists.
```

- [ ] **Step 2: Run the targeted tests and confirm the failures move from “missing types” to behavior gaps**

Run:
```bash
cd backend
./mvnw -Dtest='JsonPlaceholderClientTests,UserMapperTests,PostMapperTests' test
```

- [ ] **Step 3: Implement the mappers and client in the smallest possible way**

Use `HttpClient` plus Jackson, and keep the mapping logic deterministic and side-effect free.

- [ ] **Step 4: Re-run the tests and confirm they pass**

Run:
```bash
cd backend
./mvnw -Dtest='JsonPlaceholderClientTests,UserMapperTests,PostMapperTests' test
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/example/userspostsbrowser/importflow backend/src/test/java/com/example/userspostsbrowser/importflow
git commit -m "feat: add import client and mappers"
```

### Task 3: Add Hashing and Persistence

**Files:**
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/PayloadHashService.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/SyncRepository.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/SyncService.java`
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.properties`

- [ ] **Step 1: Write failing tests for hash comparison and sync persistence**

```java
// Verify unchanged payloads produce the same hash.
// Verify changed payloads produce a different hash.
// Verify first sync inserts raw_source/users/posts.
// Verify second sync with identical input does not duplicate rows.
// Verify changed input updates stored rows.
```

- [ ] **Step 2: Run the tests and observe the failures**

Run:
```bash
cd backend
./mvnw -Dtest='PayloadHashServiceTests,SyncServiceTests' test
```

- [ ] **Step 3: Implement hash calculation and JDBC persistence**

Use `SHA-256` for `payload_hash`, `JdbcTemplate` for inserts/upserts, and preserve the current schema constraints.

- [ ] **Step 4: Re-run the tests until they pass**

Run:
```bash
cd backend
./mvnw -Dtest='PayloadHashServiceTests,SyncServiceTests' test
```

- [ ] **Step 5: Commit**

```bash
git add backend/pom.xml backend/src/main/resources/application.properties backend/src/main/java/com/example/userspostsbrowser/importflow backend/src/test/java/com/example/userspostsbrowser/importflow
git commit -m "feat: add sync hash and persistence"
```

### Task 4: Add the Sync API

**Files:**
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/SyncController.java`
- Create: `backend/src/main/java/com/example/userspostsbrowser/importflow/dto/SyncResponse.java`

- [ ] **Step 1: Write the failing controller test**

```java
// POST /api/sync should return a JSON body with status/message fields.
```

- [ ] **Step 2: Implement the controller with minimal wiring**

Return a stable response shape that the frontend can consume later.

- [ ] **Step 3: Re-run controller and integration tests**

Run:
```bash
cd backend
./mvnw -Dtest='SyncControllerTests,SyncServiceTests' test
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/example/userspostsbrowser/importflow backend/src/test/java/com/example/userspostsbrowser/importflow
git commit -m "feat: expose sync api"
```

### Task 5: Full Backend Verification

**Files:**
- Modify: any backend file touched above

- [ ] **Step 1: Run the whole backend test suite**

Run:
```bash
cd backend
./mvnw test
```

- [ ] **Step 2: Fix any regressions**

Keep the public API surface minimal and preserve the existing setup/database tests.

- [ ] **Step 3: Verify the app still boots**

Run:
```bash
cd backend
./mvnw spring-boot:run
```

- [ ] **Step 4: Commit the completed Epic 3 implementation**

```bash
git add backend
git commit -m "feat: implement epic 3 import flow"
```
