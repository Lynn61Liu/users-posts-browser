# Epic Backlog

This document breaks the solution into a first-pass development backlog. It is written for GitHub Projects or any similar kanban board.

## Board Setup

Recommended columns:

- Backlog
- Ready
- In Progress
- Review
- Done

Recommended labels:

- epic
- backend
- frontend
- database
- testing
- docker

## Development Order

The recommended order is:

1. Project Setup
2. Database Foundation
3. Import Flow
4. Query API
5. UI Pages
6. Sync Feedback
7. Testing
8. Delivery

## Epic 1: Project Setup

**Goal:** Create the base project structure and make the application runnable.

**Why this matters:** This gives the rest of the work a stable starting point.

**Scope:**

- create the backend Spring Boot project
- create the frontend React/Vite project
- create the PostgreSQL service
- create the Docker Compose setup
- add a minimal README

**Candidate cards:**

- Initialize the backend project
- Initialize the frontend project
- Add Docker Compose for backend, frontend, and database
- Add environment variable support
- Add a minimal run guide

## Epic 2: Database Foundation

**Goal:** Create the PostgreSQL schema needed for import, traceability, and display.

**Why this matters:** The sync and query flows depend on a stable schema.

**Scope:**

- create the `users` table
- create the `posts` table
- create the `raw_source` table
- add primary keys, foreign keys, and unique constraints
- add migration scripts

**Candidate cards:**

- Create the `users` table
- Create the `posts` table
- Create the `raw_source` table
- Add database constraints
- Add migration scripts

## Epic 3: Import Flow

**Goal:** Import Users and Posts from JSONPlaceholder and store them locally.

**Why this matters:** This is the core behavior of the system.

**Scope:**

- call the external `users` endpoint
- call the external `posts` endpoint
- map external JSON into internal records
- compute `payload_hash`
- detect changed and unchanged records
- store raw payloads in `raw_source`
- upsert `users` and `posts`

**Candidate cards:**

- Implement external API client for Users
- Implement external API client for Posts
- Implement JSON mapping logic
- Implement hash comparison logic
- Implement raw source persistence
- Implement upsert logic for users and posts

## Epic 4: Query API

**Goal:** Expose backend endpoints for list and detail reads.

**Why this matters:** The frontend should read local data through a stable API.

**Scope:**

- `GET /api/users`
- `GET /api/users/{id}`
- `GET /api/users/{id}/posts`
- response DTOs

**Candidate cards:**

- Implement the user list endpoint
- Implement the user detail endpoint
- Implement the user posts endpoint
- Add response DTOs
- Add API error responses

## Epic 5: UI Pages

**Goal:** Build the list and detail pages that show imported data.

**Why this matters:** This is the user-facing part of the solution.

**Scope:**

- user list page
- user detail page
- navigation between list and detail
- loading, empty, and error states

**Candidate cards:**

- Build the user list page
- Build the user detail page
- Add page navigation
- Add loading and empty states
- Add error state handling

## Epic 6: Sync Feedback

**Goal:** Show clear feedback after each sync action.

**Why this matters:** Users need to know whether data changed or not.

**Scope:**

- sync success message
- no-change message
- update message
- error message

**Candidate cards:**

- Add sync button behavior
- Add success feedback
- Add no-change feedback
- Add update feedback
- Add error feedback

## Epic 7: Testing

**Goal:** Prove the core behaviors with a small TDD-oriented test set.

**Why this matters:** The most important flows should be verified before release.

**Scope:**

- mapping tests
- hash comparison tests
- sync and persistence tests
- API tests
- UI smoke tests

**Candidate cards:**

- Add mapping tests
- Add hash comparison tests
- Add sync persistence tests
- Add API tests
- Add UI smoke tests

## Epic 8: Delivery

**Goal:** Make the project easy to run and review.

**Why this matters:** The assessment should be simple to start and verify.

**Scope:**

- one-command startup
- final README instructions
- environment setup notes
- final verification

**Candidate cards:**

- Finalize Docker Compose
- Finalize README instructions
- Add environment setup notes
- Run final verification

## First Card
<!-- 
**Title:** Initialize the backend project

**Goal:** Create the Spring Boot backend skeleton and verify that it starts successfully.

**Acceptance:**

- the backend project exists
- the backend starts locally
- the project has a clean package structure
- the initial build passes

**Notes:**

- this card should stay small
- it should produce a working backend foundation for later tasks -->
