# Epic Backlog

This document breaks the solution into a first-pass development backlog. It is written for Trello, GitHub Projects, or any similar kanban board.

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

---

## Epic 1: Project Setup

**Goal:** Create the base project structure and make the application runnable.

**Why this matters:** This gives the rest of the work a stable starting point.

### Card 1.1: Initialize the backend project

**Goal:** Create the Spring Boot backend skeleton and verify that it starts successfully.

**Acceptance:**

- the backend project exists
- the project builds successfully
- the backend starts locally without errors
- the package structure is clean and ready for later work

### Card 1.2: Initialize the frontend project

**Goal:** Create the React/Vite frontend skeleton and verify that it starts successfully.

**Acceptance:**

- the frontend project exists
- the frontend starts locally without errors
- the project builds successfully
- the component structure is clean and ready for later pages

### Card 1.3: Add Docker Compose for backend, frontend, and database

**Goal:** Create a single local runtime that can start the whole system.

**Acceptance:**

- Docker Compose starts the backend container
- Docker Compose starts the frontend container
- Docker Compose starts the PostgreSQL container
- the services can reach each other through the compose network

### Card 1.4: Add shared environment configuration

**Goal:** Centralize the environment values needed by the application.

**Acceptance:**

- backend configuration can read database connection values
- frontend configuration can read the API base URL
- sensitive values are not hardcoded in source files
- local setup can be changed through environment values

### Card 1.5: Add a minimal run guide

**Goal:** Provide a short guide that explains how to start the system.

**Acceptance:**

- the README explains the start command
- the README explains the required environment setup
- the README is short and easy to follow
- a reviewer can start the project without guessing the order of steps

---

## Epic 2: Database Foundation

**Goal:** Create the PostgreSQL schema needed for import, traceability, and display.

**Why this matters:** The sync and query flows depend on a stable schema.

### Card 2.1: Create the `raw_source` table

**Goal:** Store one raw imported JSON record per external item.

**Acceptance:**

- the table stores `source_type`, `external_id`, `raw_payload`, `payload_hash`, and sync metadata
- the table has a unique constraint on `source_type + external_id`
- the table can store both user and post raw records

### Card 2.2: Create the `users` table

**Goal:** Store normalized user data with expanded address and company fields.

**Acceptance:**

- the table stores user identity fields
- the table stores expanded address fields
- the table stores expanded company fields
- the table can reference the raw source record

### Card 2.3: Create the `posts` table

**Goal:** Store normalized post data and link each post to its user.

**Acceptance:**

- the table stores post identity fields
- the table stores the post title and body
- the table includes a foreign key to the parent user
- the table can reference the raw source record

### Card 2.4: Add database constraints and indexes

**Goal:** Make the database protect data integrity during repeated syncs.

**Acceptance:**

- primary keys are defined for all tables
- foreign keys are defined where relationships exist
- uniqueness rules prevent duplicate business records
- the raw source uniqueness rule prevents duplicate raw records

### Card 2.5: Add migration scripts

**Goal:** Create repeatable schema changes for local and review environments.

**Acceptance:**

- the schema can be created from migrations
- the migrations can be run more than once without manual cleanup
- the schema matches the design document
- the migration order is clear and deterministic

---

## Epic 3: Import Flow

**Goal:** Import Users and Posts from JSONPlaceholder and store them locally.

**Why this matters:** This is the core behavior of the system.

### Card 3.1: Implement the external API client for Users

**Goal:** Fetch user data from JSONPlaceholder in a reliable way.

**Acceptance:**

- the backend can call the external users endpoint
- the response is parsed successfully
- failures from the external endpoint are handled clearly
- the client is isolated from the rest of the import logic

### Card 3.2: Implement the external API client for Posts

**Goal:** Fetch post data from JSONPlaceholder in a reliable way.

**Acceptance:**

- the backend can call the external posts endpoint
- the response is parsed successfully
- failures from the external endpoint are handled clearly
- the client is isolated from the rest of the import logic

### Card 3.3: Implement JSON mapping logic

**Goal:** Map the external JSON shape into internal records used by the system.

**Acceptance:**

- user JSON maps into the internal user model
- post JSON maps into the internal post model
- nested user fields are handled correctly
- mapping failures are visible and testable

### Card 3.4: Implement hash comparison logic

**Goal:** Detect whether an imported record changed since the last sync.

**Acceptance:**

- a hash can be calculated from each raw record
- unchanged data produces the same hash
- changed data produces a different hash
- the sync flow can use the hash result to decide whether to update

### Card 3.5: Implement raw source persistence and upsert logic

**Goal:** Store the raw record and update business tables only when needed.

**Acceptance:**

- one raw record is stored per external item
- unchanged records are not duplicated
- changed records update the stored data
- users and posts are upserted correctly after a changed import

### Card 3.6: Implement sync result reporting

**Goal:** Return a clear sync status after each import run.

**Acceptance:**

- the sync can return success
- the sync can return info for no change
- the sync can return error when import or save fails
- the status can be used by the UI

---

## Epic 4: Query API

**Goal:** Expose backend endpoints for list and detail reads.

**Why this matters:** The frontend should read local data through a stable API.

### Card 4.1: Implement the user list endpoint

**Goal:** Return the imported users for the list page.

**Acceptance:**

- the endpoint returns all imported users
- the response includes the fields needed by the list page
- the endpoint works against local database data

### Card 4.2: Implement the user detail endpoint

**Goal:** Return the selected user with full details.

**Acceptance:**

- the endpoint returns one user by id
- the response includes the selected user's full information
- the endpoint returns a clear error when the user is not found

### Card 4.3: Implement the user posts endpoint

**Goal:** Return the posts related to one user.

**Acceptance:**

- the endpoint returns posts for the selected user
- the response only includes the related records
- the endpoint returns an empty result when the user has no posts

### Card 4.4: Add response DTOs and error responses

**Goal:** Keep the API output stable and clear for the frontend.

**Acceptance:**

- API responses do not expose database entities directly
- response shapes match the frontend needs
- errors return a clear and consistent message
- success, empty, and error responses are easy to distinguish

---

## Epic 5: UI Pages

**Goal:** Build the list and detail pages that show imported data.

**Why this matters:** This is the user-facing part of the solution.

### Card 5.1: Build the user list page

**Goal:** Show the imported users in a browser-friendly list.

**Acceptance:**

- the page shows the imported users
- each user item shows the key summary fields
- the selected item is visually clear

### Card 5.2: Build the user detail page

**Goal:** Show one selected user with full information and related posts.

**Acceptance:**

- the page shows the selected user's full details
- the page shows the related posts
- the page updates when a different user is selected

### Card 5.3: Add page navigation

**Goal:** Move from the list view to the detail view in a simple flow.

**Acceptance:**

- clicking a user opens the detail view
- the detail view reflects the selected user
- returning to the list view is straightforward

### Card 5.4: Add loading, empty, and error states

**Goal:** Make the pages handle missing or delayed data clearly.

**Acceptance:**

- loading state appears while data is being fetched
- empty state appears when no data exists
- error state appears when a request fails
- the page remains readable in each state

---

## Epic 6: Sync Feedback

**Goal:** Show clear feedback after each sync action.

**Why this matters:** Users need to know whether data changed or not.

### Card 6.1: Add sync button behavior

**Goal:** Let the user trigger the import flow from the UI.

**Acceptance:**

- the sync button triggers the backend sync action
- the UI does not freeze while the sync is running
- the sync action can be repeated

### Card 6.2: Add success and update feedback

**Goal:** Tell the user when a sync run changed data successfully.

**Acceptance:**

- the UI shows a success message after a successful sync
- the UI shows an update message when records changed
- the message is easy to understand

### Card 6.3: Add no-change feedback

**Goal:** Tell the user when the sync found no new changes.

**Acceptance:**

- the UI shows an info message when the payload hash did not change
- the message makes it clear that no update was needed
- the message appears after the sync finishes

### Card 6.4: Add error feedback

**Goal:** Show a clear error state when sync or save fails.

**Acceptance:**

- the UI shows an error message if sync fails
- the UI shows an error message if saving data fails
- the error state is visible and easy to read

---

## Epic 7: Testing

**Goal:** Prove the core behaviors with a small TDD-oriented test set.

**Why this matters:** The most important flows should be verified before release.

### Card 7.1: Add mapping tests

**Goal:** Verify that external JSON maps into the correct internal records.

**Acceptance:**

- user JSON maps into the correct fields
- post JSON maps into the correct fields
- nested user data is handled correctly

### Card 7.2: Add hash comparison tests

**Goal:** Verify that repeated sync can detect changed and unchanged records.

**Acceptance:**

- unchanged data produces the same hash
- changed data produces a different hash
- hash comparison can drive the no-change path

### Card 7.3: Add sync and persistence tests

**Goal:** Verify that sync stores data correctly and avoids duplicates.

**Acceptance:**

- initial sync stores data successfully
- repeated sync skips unchanged records
- repeated sync updates changed records

### Card 7.4: Add API tests

**Goal:** Verify that the backend returns the correct data and status responses.

**Acceptance:**

- list, detail, and posts endpoints return the expected data
- sync endpoint returns success, info, and error responses
- error cases are covered

### Card 7.5: Add UI smoke tests

**Goal:** Verify that the main UI states render correctly.

**Acceptance:**

- list page renders successfully
- detail page renders successfully
- loading, empty, success, info, and error states are covered

---

## Epic 8: Delivery

**Goal:** Make the project easy to run and review.

**Why this matters:** The assessment should be simple to start and verify.

### Card 8.1: Finalize Docker Compose

**Goal:** Make the whole application start from one command.

**Acceptance:**

- backend starts through Docker Compose
- frontend starts through Docker Compose
- PostgreSQL starts through Docker Compose
- service startup order is clear

### Card 8.2: Finalize README instructions

**Goal:** Provide clear startup and verification instructions.

**Acceptance:**

- the README explains how to run the project
- the README explains the required environment setup
- the README explains how to verify the system

### Card 8.3: Add environment setup notes

**Goal:** Document the environment values and runtime expectations.

**Acceptance:**

- required environment variables are listed
- local setup instructions are clear
- the runtime assumptions are visible to the reviewer

### Card 8.4: Run final verification

**Goal:** Confirm the full system works as intended.

**Acceptance:**

- the main flows run successfully end to end
- the core tests pass
- the project is ready for review
