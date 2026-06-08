# Epic Test Case Checklist

This document lists the main test cases for each epic in the backlog. It is intended to support TDD and help the implementation stay aligned with the design.

## How to Use This Document

- Pick the epic you are working on.
- Start with the first test case in that epic.
- Write the failing test first.
- Implement the smallest change needed to pass.
- Repeat until the epic is complete.

---

## Epic 1: Project Setup

### TC-1.1: Backend project starts successfully

**Goal:** Verify that the Spring Boot backend can start in a clean local environment.

**Expected Result:**

- the application starts without errors
- the basic project structure is valid
- the build succeeds

### TC-1.2: Frontend project starts successfully

**Goal:** Verify that the React/Vite frontend can start in a clean local environment.

**Expected Result:**

- the frontend starts without errors
- the build succeeds
- the basic project structure is valid

### TC-1.3: Docker Compose starts all services

**Goal:** Verify that backend, frontend, and database can start together.

**Expected Result:**

- all containers start successfully
- the services can communicate with each other
- the application can be opened through the expected local URLs

### TC-1.4: Environment values are loaded correctly

**Goal:** Verify that configuration values are read from the environment.

**Expected Result:**

- backend reads database settings correctly
- frontend reads API settings correctly
- no sensitive values are hardcoded

### TC-1.5: README instructions are usable

**Goal:** Verify that a reviewer can follow the run instructions.

**Expected Result:**

- the README includes the correct run command
- the README includes the required setup notes
- the README is easy to follow without extra help

---

## Epic 2: Database Foundation

### TC-2.1: `raw_source` table stores one raw record per imported item

**Goal:** Verify that raw payloads can be stored with traceability data.

**Expected Result:**

- a raw user record can be inserted
- a raw post record can be inserted
- `source_type + external_id` stays unique
- `payload_hash` is stored

### TC-2.2: `users` table stores expanded user fields

**Goal:** Verify that the user schema supports the full user display structure.

**Expected Result:**

- name, username, email, phone, and website are stored
- address fields are stored separately
- company fields are stored separately
- the table can reference the raw source record

### TC-2.3: `posts` table stores post fields and user relation

**Goal:** Verify that posts are linked to the correct user.

**Expected Result:**

- title and body are stored
- `user_id` references the parent user
- the table can reference the raw source record

### TC-2.4: Database constraints prevent duplicates

**Goal:** Verify that the schema protects data integrity.

**Expected Result:**

- primary keys exist on all tables
- foreign keys exist where required
- duplicate business rows are blocked
- duplicate raw rows are blocked

### TC-2.5: Migrations create the same schema every time

**Goal:** Verify that the schema can be recreated reliably.

**Expected Result:**

- migrations run successfully on a clean database
- repeated migration execution does not break the schema
- the created schema matches the design

---

## Epic 3: Import Flow

### TC-3.1: Users endpoint response is parsed correctly

**Goal:** Verify that the external users API response can be consumed.

**Expected Result:**

- the response is read successfully
- the user list is parsed into internal records
- failure from the external API is handled clearly

### TC-3.2: Posts endpoint response is parsed correctly

**Goal:** Verify that the external posts API response can be consumed.

**Expected Result:**

- the response is read successfully
- the post list is parsed into internal records
- failure from the external API is handled clearly

### TC-3.3: User mapping preserves the required fields

**Goal:** Verify that user JSON maps into the correct internal model.

**Expected Result:**

- top-level user fields are mapped correctly
- nested address fields are mapped correctly
- nested company fields are mapped correctly

### TC-3.4: Post mapping preserves the required fields

**Goal:** Verify that post JSON maps into the correct internal model.

**Expected Result:**

- userId is preserved
- title is preserved
- body is preserved

### TC-3.5: Hash comparison detects unchanged records

**Goal:** Verify that unchanged source data produces the same hash.

**Expected Result:**

- the same record produces the same hash
- the sync flow recognizes no-change data
- no unnecessary update is triggered

### TC-3.6: Hash comparison detects changed records

**Goal:** Verify that modified source data produces a different hash.

**Expected Result:**

- changed payloads produce different hashes
- the sync flow recognizes changed data
- an update path is triggered

### TC-3.7: First sync stores imported data

**Goal:** Verify that the first import run writes data correctly.

**Expected Result:**

- `raw_source` records are created
- `users` records are created
- `posts` records are created
- the data can be read back after sync

### TC-3.8: Re-running sync skips unchanged records

**Goal:** Verify that repeated sync does not create duplicate rows.

**Expected Result:**

- unchanged records are detected
- no duplicate business records are created
- the sync result reports no change

### TC-3.9: Re-running sync updates changed records

**Goal:** Verify that changed source data updates the stored data.

**Expected Result:**

- changed raw records are updated
- business tables are upserted correctly
- the sync result reports an update

### TC-3.10: Sync failure returns a clear error

**Goal:** Verify that import or save failures are visible.

**Expected Result:**

- external API failures are reported
- database save failures are reported
- the sync result reports error

---

## Epic 4: Query API

### TC-4.1: User list endpoint returns imported users

**Goal:** Verify that the list endpoint returns the expected data.

**Expected Result:**

- the endpoint returns all imported users
- the response contains the fields needed by the list page
- the data comes from the local database

### TC-4.2: User detail endpoint returns one selected user

**Goal:** Verify that the detail endpoint returns a single user record.

**Expected Result:**

- the endpoint returns the selected user
- the response includes the full user information
- a missing user returns a clear error

### TC-4.3: User posts endpoint returns related posts

**Goal:** Verify that the posts endpoint returns the correct related records.

**Expected Result:**

- the endpoint returns posts for the selected user
- only related posts are returned
- an empty result is handled correctly

### TC-4.4: API errors are returned in a consistent shape

**Goal:** Verify that failures are easy for the frontend to handle.

**Expected Result:**

- error responses are consistent
- success responses are distinct from errors
- empty responses are handled clearly

---

## Epic 5: UI Pages

### TC-5.1: User list page renders imported users

**Goal:** Verify that the list page shows the imported data.

**Expected Result:**

- the user list appears on screen
- each item shows the key summary fields
- the selected item is visible

### TC-5.2: User detail page renders selected user data

**Goal:** Verify that the detail page shows one user and related posts.

**Expected Result:**

- the selected user's full information appears
- related posts appear below or beside the user details
- the page updates when a different user is selected

### TC-5.3: Loading state appears while data is being fetched

**Goal:** Verify that the UI gives feedback during requests.

**Expected Result:**

- loading is visible on the list page
- loading is visible on the detail page
- the UI stays readable while waiting

### TC-5.4: Empty state appears when no data exists

**Goal:** Verify that the UI handles missing data clearly.

**Expected Result:**

- the list page shows an empty state when no users exist
- the detail page shows an empty state when no user is selected or no data exists

### TC-5.5: Error state appears when a request fails

**Goal:** Verify that the UI shows a clear failure state.

**Expected Result:**

- list page errors are visible
- detail page errors are visible
- the user can understand that the request failed

---

## Epic 6: Sync Feedback

### TC-6.1: Sync button triggers the import action

**Goal:** Verify that the UI can start the sync flow.

**Expected Result:**

- clicking sync calls the backend
- the user can repeat the action
- the UI does not freeze

### TC-6.2: Success feedback appears after a successful sync

**Goal:** Verify that the user sees success feedback.

**Expected Result:**

- success feedback is visible
- the feedback is easy to understand
- the user knows sync completed

### TC-6.3: No-change feedback appears when data is unchanged

**Goal:** Verify that the user sees an info message when nothing changed.

**Expected Result:**

- no-change feedback is visible
- the message explains that no update was needed
- the feedback appears after sync finishes

### TC-6.4: Update feedback appears when data changed

**Goal:** Verify that the user sees feedback when data was updated.

**Expected Result:**

- update feedback is visible
- the message explains that records changed
- the feedback appears after sync finishes

### TC-6.5: Error feedback appears when sync fails

**Goal:** Verify that sync failures are visible in the UI.

**Expected Result:**

- error feedback is visible
- the message is clear
- the user knows the sync did not complete successfully

---

## Epic 7: Testing

### TC-7.1: Mapping tests cover user and post conversion

**Goal:** Verify that the mapping layer is correct.

**Expected Result:**

- user fields are mapped correctly
- post fields are mapped correctly
- nested data is preserved where required

### TC-7.2: Hash tests cover changed and unchanged records

**Goal:** Verify that the hash logic supports sync decisions.

**Expected Result:**

- unchanged records produce the same hash
- changed records produce a different hash
- the hash result can drive the sync branch

### TC-7.3: Sync tests cover persistence and no duplicates

**Goal:** Verify that repeated imports behave correctly.

**Expected Result:**

- first sync stores data
- second sync with unchanged data does not duplicate rows
- changed data updates stored records

### TC-7.4: API tests cover list, detail, and sync responses

**Goal:** Verify that the backend endpoints behave correctly.

**Expected Result:**

- list endpoint returns the expected data
- detail endpoint returns the selected user
- sync endpoint returns success, info, or error

### TC-7.5: UI smoke tests cover the main pages and states

**Goal:** Verify that the main screens render without breaking.

**Expected Result:**

- list page renders successfully
- detail page renders successfully
- loading, empty, success, info, and error states are covered

---

## Epic 8: Delivery

### TC-8.1: Docker Compose starts the whole system

**Goal:** Verify that the project can run in one command.

**Expected Result:**

- backend starts
- frontend starts
- PostgreSQL starts
- the services work together

### TC-8.2: README instructions are enough to run the project

**Goal:** Verify that the documentation is usable.

**Expected Result:**

- the run steps are clear
- setup steps are clear
- verification steps are clear

### TC-8.3: Environment notes are complete

**Goal:** Verify that runtime assumptions are documented.

**Expected Result:**

- environment variables are listed
- local setup is described
- the reviewer can reproduce the environment

### TC-8.4: Final verification passes end to end

**Goal:** Verify that the full solution is ready for review.

**Expected Result:**

- the main flows work end to end
- the core tests pass
- the project is ready to be shared
