## 1. Overview

This project is a small enterprise-style integration application that consumes Users and Posts from JSONPlaceholder, stores them in a SQL database, and exposes them through a user-friendly web interface.

Success for this project means:
- external data can be imported reliably
- users can browse the user list and user details
- associated posts are visible from the selected user
- the system can be started with minimal setup
- the solution is easy to understand, test, and extend

## 2. Scope / Assumptions / Non-goals

### Scope

The system includes:
- importing Users and Posts from JSONPlaceholder
- persisting imported data in PostgreSQL
- rendering a frontend for user browsing and post viewing
- supporting a repeatable startup flow through Docker Compose

### Assumptions

- JSONPlaceholder is the external source of truth for this assessment
- The dataset is relatively small and suitable for full synchronization
- External API data is stable enough for startup-time import
- Reviewers should be able to run the application without manual infrastructure setup
- Authentication and authorization are not required for this assessment

### Non-goals

This assessment does not include:
- user registration or login
- creating, editing, or deleting users/posts
- advanced search, filtering, or sorting
- complex caching or background job infrastructure
- multi-environment deployment pipelines
- production-grade observability stack

### 2.4 Requirements

> **Goal:** Define what the system should do and what counts as done.  
> **Purpose:** Give the rest of the document a clear reference point.

#### User Stories

- As a user, I want to view a list of users so that I can browse available records.
- As a user, I want to view the details of a selected user so that I can understand that user's information.
- As a user, I want to view posts related to a selected user so that I can understand the related content.
- As a system administrator, I want the system to import data from the external source so that the local data is available for browsing.

#### Acceptance Criteria

- The user list page shows all imported users.
- Each user entry shows the user's name, email, and company.
- The user can open a detail view from the list.
- The detail view shows the selected user's basic information.
- The detail view shows the selected user's related posts.
- If no data exists, the page shows a clear empty state.
- If importing or saving data fails, the page shows a clear error state.
- If importing succeeds, the page shows a clear success or info message.
- If re-running the import changes nothing, the page shows a clear info message.
- If re-running the import updates existing records, the page shows a clear success or info message.
- The system stores the imported data in the database.
- The system keeps the original imported source data or payload for traceability.
- Re-running the import does not create duplicate records.

#### Functional Requirements

- The system shall import Users from JSONPlaceholder.
- The system shall import Posts from JSONPlaceholder.
- The system shall persist imported data in PostgreSQL.
- The system shall display a list of users.
- The system shall display details for a selected user.
- The system shall display posts associated with the selected user.
- The system shall support repeatable imports without duplicate records.
- The system shall show import success, info, and error messages to the user.
- The system shall preserve the original imported source data or payload for traceability.
- The system shall provide a single sync action that triggers both Users and Posts imports.

#### Non-functional Requirements

- Maintainability: the code should be easy to read, change, and test.
- Reliability: the system should avoid duplicate imports and inconsistent data.
- Accessibility: the UI should be usable with keyboard navigation and readable with good contrast.
- Simplicity: the application should be easy to run with a single command.
- Scalability: the design should leave room for pagination, filtering, and future growth.
- Extensibility: the import layer should isolate external schema changes so that new source formats, such as Excel or CSV, can be added with minimal impact on the core logic.

## 3. Design

> **Goal:** Organize the technical solution into one clear design section.  
> **Purpose:** Keep the architecture, data flow, workflow, database, API, UI, patterns, and class design together so the document is easier to follow.

### 3.1 Analysis

The system has two main problems to solve:

- how to get external JSON data into the local database
- how to read local data and render it on the web page

The external data comes from JSONPlaceholder. The internal data is stored in PostgreSQL. The UI reads from the local database instead of reading directly from the external API.

### 3.2 Architecture Design

The system uses a simple layered architecture:

- **Import flow:** External API -> Import layer -> Service layer -> Database
- **Query flow:** Frontend -> API layer -> Service layer -> Database

The main idea is to keep import logic, business logic, and UI logic separate. MVC is used at the web boundary, and the overall design keeps the same separation that clean architecture tries to achieve.

### 3.3 Data Flow Design

The main data paths are:

- external JSON is read from JSONPlaceholder
- the import layer maps the raw data into internal records
- the service layer compares hashes and decides whether data changed
- the repository layer writes to PostgreSQL
- the frontend reads from the API and renders the local data

### 3.4 Workflow Design

The sync workflow is simple:

1. The user clicks the sync button.
2. The backend reads Users and Posts from the external API.
3. The backend computes a hash for each raw record.
4. The backend compares the hash with the stored record.
5. If the data changed, the backend updates `raw_source` and upserts the business tables.
6. If the data did not change, the backend returns a no-change status.
7. The frontend shows success, info, or error feedback.

### 3.5 Database Design

The database uses three tables:

- `users` stores normalized user data with expanded address and company fields.
- `posts` stores normalized post data and links back to `users`.
- `raw_source` stores one raw JSON object per imported record, plus a hash for change detection.

Key points:

- `users` and `posts` are business tables.
- `raw_source` is the traceability table.
- `source_type + external_id` is unique in `raw_source`.
- `payload_hash` is used to detect whether the source data changed.

### 3.5.1 Relationship Diagram

![db](https://res.cloudinary.com/dyuvwlir0/image/upload/v1780879419/Screenshot_2026-06-08_at_12.24.16_PM_qxo5h3.png)


### 3.5.2 Table Structure

#### users

| Field | Type | Null | Key | Description |
| --- | --- | --- | --- | --- |
| id | bigint | no | PK | Internal primary key |
| external_id | bigint | no | UQ | ID from JSONPlaceholder |
| raw_source_id | bigint | no | FK | Reference to `raw_source.id` |
| name | varchar(255) | no |  | User name |
| username | varchar(255) | no |  | Username |
| email | varchar(255) | no |  | Email |
| phone | varchar(100) | yes |  | Phone number |
| website | varchar(255) | yes |  | Website |
| address_street | varchar(255) | yes |  | Street |
| address_suite | varchar(255) | yes |  | Suite |
| address_city | varchar(255) | yes |  | City |
| address_zipcode | varchar(50) | yes |  | Zip code |
| address_geo_lat | varchar(50) | yes |  | Latitude |
| address_geo_lng | varchar(50) | yes |  | Longitude |
| company_name | varchar(255) | yes |  | Company name |
| company_catch_phrase | varchar(255) | yes |  | Company catch phrase |
| company_bs | varchar(255) | yes |  | Company business phrase |
| created_at | timestamp | no |  | Created time |
| updated_at | timestamp | no |  | Updated time |

#### posts

| Field | Type | Null | Key | Description |
| --- | --- | --- | --- | --- |
| id | bigint | no | PK | Internal primary key |
| external_id | bigint | no | UQ | ID from JSONPlaceholder |
| raw_source_id | bigint | no | FK | Reference to `raw_source.id` |
| user_id | bigint | no | FK | Reference to `users.id` |
| title | varchar(255) | no |  | Post title |
| body | text | no |  | Post body |
| created_at | timestamp | no |  | Created time |
| updated_at | timestamp | no |  | Updated time |

#### raw_source

| Field | Type | Null | Key | Description |
| --- | --- | --- | --- | --- |
| id | bigint | no | PK | Internal primary key |
| source_type | varchar(20) | no |  | user or post |
| external_id | bigint | no | UQ* | ID from JSONPlaceholder |
| raw_payload | jsonb | no |  | Original JSON object |
| payload_hash | varchar(64) | no |  | Hash of raw payload |
| synced_at | timestamp | no |  | Sync time |
| sync_result | varchar(20) | no |  | success / no_change / error |
| sync_batch_id | varchar(64) | yes |  | Optional batch tracking |

### 3.6 API Design

The API stays small and focused:

- `GET /api/users`
- `GET /api/users/{id}`
- `GET /api/users/{id}/posts`
- `POST /api/sync`

The query endpoints support the list page and detail page. The sync endpoint triggers the import flow and returns a clear status message.

### 3.7 UI Design
![ui](https://res.cloudinary.com/dyuvwlir0/image/upload/v1780879662/E7915CE9-5AEB-484C-BFB2-C6789E4A1DD3_gixndg.png)

The UI has two main views:

- a user list page
- a user detail page

The list page shows the users. The detail page shows the selected user and related posts. Both pages should handle loading, empty, success, info, and error states.

### 3.8 Design Patterns

The main patterns used in the solution are:

- **Layered architecture:** separates UI, API, service, and persistence concerns
- **DTO pattern:** keeps API responses separate from database entities
- **Repository pattern:** isolates database access
- **Adapter pattern:** maps external JSON into internal records

### 3.9 Class Design

The class design follows simple responsibility boundaries:

- controllers handle HTTP requests and responses
- services handle business rules and workflow decisions
- repositories handle database access
- mappers handle external-to-internal data conversion

The main class design principles are:

- **Single Responsibility Principle:** each class should do one job
- **Open/Closed Principle:** new import formats should be added through extension points
- **Dependency Inversion Principle:** higher-level logic should depend on abstractions, not database details
- **Dependency Injection:** dependencies should be passed in rather than created directly

### 3.10 Testing Strategy

The test strategy follows TDD:

1. test mapping and hash comparison first
2. test sync and persistence next
3. test API responses after that
4. test UI states last

This order keeps the implementation focused on the main behaviors first.

### 3.11 Risks, Trade-offs, and Open Questions

The main trade-offs are:

- expanded database columns vs JSON columns
- one raw record per source item vs full history storage
- manual sync button vs background sync
- simple layered design vs a more advanced architecture

The open questions are:

- do we need a historical audit trail?
- do we need search, filtering, or pagination later?
- do we need Excel or CSV import in a later version?

## 4. Implementation

> **Goal:** Describe the technology stack chosen for the implementation.  
> **Purpose:** Explain why the selected tools match the problem, the project scope, and the developer experience for this assessment.

The implementation uses four main technologies:

- **Backend:** Spring Boot
- **Frontend:** React with Vite
- **Database:** PostgreSQL
- **Runtime:** Docker and Docker Compose

### 4.1 Backend: Spring Boot with Java

Spring Boot is a good fit for the backend because this project needs a clear REST API, service logic, and database access in one place. Java is a practical choice here because it works well with Spring, it is strong for layered application design, and it is a familiar backend language for this kind of assessment.

I chose Java and Spring Boot because:

- they fit the REST API style used by the project
- they support a clean service and repository structure
- they work naturally with PostgreSQL
- they are stable and easy to organize for a small business-style application
- they match my own experience from academic and practice projects, so I can implement the solution reliably

The backend will handle:

- syncing Users and Posts from JSONPlaceholder
- mapping external JSON into internal records
- comparing `payload_hash` values to detect changes
- saving data into `raw_source`, `users`, and `posts`
- exposing the API endpoints used by the frontend

### 4.2 Frontend: React with Vite

React is a good fit for the frontend because the UI is component-based and the page needs to render user lists, user detail views, and status states cleanly. Vite is a good choice because it gives a simple and fast development setup.

I chose React because:

- the UI can be split into small reusable components
- list, detail, loading, empty, and error states are easy to manage
- the frontend only needs to consume API data, not complex client-side logic
- it is a common and practical choice for a small web application

The frontend will handle:

- showing the imported user list
- showing the selected user detail page
- showing related posts
- showing loading, empty, success, info, and error states
- calling the sync action when the user clicks the sync button

### 4.3 Database: PostgreSQL

PostgreSQL is a good fit because the project stores normalized relational data and needs clear relationships between users, posts, and raw source records. It also supports the kind of constraints and data integrity this solution needs.

I chose PostgreSQL because:

- it is a real SQL database and fits the assessment requirement
- it supports primary keys, foreign keys, and unique constraints well
- it works well for `users`, `posts`, and `raw_source`
- it gives a clean way to store structured business data and raw JSON payloads together

The database will store:

- expanded user fields, including address and company data
- posts linked to users
- raw JSON payloads for traceability
- payload hashes for no-change detection

### 4.4 Runtime: Docker and Docker Compose

Docker is used to make the project easy to run. Docker Compose is the simplest way to package the backend, frontend, and database so the reviewer can start the system with one command.

I chose Docker because:

- it makes the environment repeatable
- it reduces setup steps for reviewers
- it fits the assessment requirement for easy startup
- it keeps the runtime consistent across machines

The runtime setup will let the project start with a single Compose command and avoid manual installation steps where possible.

## 5. Testing

> **Goal:** Define what the system must prove before and after implementation.  
> **Purpose:** Keep the test plan focused on the main behaviors that matter most.

### 5.1 Test Objectives

- verify that external JSON is mapped correctly into internal records
- verify that repeated sync actions detect changed and unchanged data correctly
- verify that imported data is stored in the database without duplicates
- verify that the API returns the correct responses for list, detail, and sync actions
- verify that the UI shows the correct loading, empty, success, info, and error states

### 5.2 What to Test First

1. mapping and hash comparison
2. sync and persistence behavior
3. API responses
4. UI states

### 5.3 Core Behaviors and Expected Results

#### Mapping and Hash Comparison

- When a raw user or post record is imported, it should map into the correct internal structure.
- When the same raw record is imported again without changes, the stored hash should match.
- When the raw record changes, the new hash should differ.

#### Sync and Persistence

- When sync runs for the first time, the system should store the imported data.
- When sync runs again with unchanged data, the system should not create duplicate records.
- When sync runs again with changed data, the system should update the stored data.

#### API Responses

- The user list endpoint should return the imported users.
- The user detail endpoint should return the selected user.
- The user posts endpoint should return the selected user's related posts.
- The sync endpoint should return success, info, or error feedback.

#### UI States

- The user list page should show data when it exists.
- The detail page should show the selected user's information and posts.
- The UI should show a loading state while data is being fetched.
- The UI should show an empty state when no data exists.
- The UI should show a success or info message after sync.
- The UI should show an error state when a request fails.

### 5.4 Failure Cases That Must Be Covered

- external API request fails
- imported data cannot be mapped correctly
- database save fails
- repeated sync finds no changes
- repeated sync tries to create duplicate records
- frontend request fails while rendering the list or detail page

### 5.5 Test Priority

1. mapping and hash logic
2. sync and database persistence
3. API responses
4. UI rendering and status handling

### 5.6 Why This Test Plan Fits

- it covers the main data flow from external source to database and from database to browser
- it keeps the test scope small and focused
- it supports incremental development by checking the most important behaviors first
- it gives a clear definition of done for the core project flow

## 6. Conclusion

This solution is intentionally small and focused. It uses a layered Spring Boot backend, a React frontend, PostgreSQL for structured storage, and Docker for easy setup. The design keeps external data handling separate from local data display, which makes the system easier to understand, test, and extend.

The main idea is simple: import Users and Posts from JSONPlaceholder, store them locally with traceability, and let the user browse the saved data through a clean web interface. The test plan supports that goal by checking mapping, sync behavior, API responses, and UI states in a clear order.

This structure stays within the scope of the assessment while still showing a complete end-to-end solution.
