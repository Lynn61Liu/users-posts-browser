# Project Summary and Reflection

For this project, I followed the Software Development Life Cycle (SDLC) approach and produced a fairly complete solution document for it. The full details can be found in [MySolution.MD](../MySolution.MD). This is my problem-solving process.

This reflection document focuses on my thinking during implementation, including how I broke down the problem, how I designed the system, why I made those design choices, what difficulties I encountered, and what I ultimately learned and reflected on.

## 1. Project Overview

The goal of this project is to fetch Users and Posts data from JSONPlaceholder, save the data into a local database, and then display it through a web page. The implementation followed the SDLC approach: first requirements analysis, then architecture design, data design, API design, UI design, and finally testing and summary.

## 2. Overall Architecture and Design Patterns

### 2.1 Overall Architecture
The system uses **separation of frontend and backend** and **layered design**. At the same time, the **import flow** and the **query flow** are handled separately so that import logic and query logic do not interfere with each other. The import logic is mainly responsible for fetching data from the external API, mapping it, and saving it to the database. The query logic is mainly responsible for reading data from the local database and returning it to the frontend. With this design, when external interfaces change or import rules are adjusted, the impact is smaller, and the system is easier to maintain and test.

### 2.2 Layered Architecture
- The backend is structured as `Controller -> Service -> Repository`.
- In the query flow, `UserQueryController` handles only the HTTP entry point, `UserQueryService` handles business decisions, and `UserQueryRepository` handles SQL.
- In the import flow, `SyncController -> SyncService -> SyncRepository` handles the synchronization process, while working with the external API client and mappers to complete data fetching and transformation.

### 2.3 DTO Pattern
- The purpose of DTOs is to separate the **data structure exposed to the outside** from the **data structure used internally**.
- The query APIs return dedicated response DTOs such as `UserListItemResponse`, `UserDetailResponse`, `UserPostResponse`, and `ApiErrorResponse`.
- The import flow also uses DTOs, and it is split into two layers: external JSON DTOs and internal import record DTOs.
- For example, `JsonPlaceholderUserDto` represents the structure from the external API, while `ImportedUserRecord` represents the internal structure that is ready to be stored in the database.

### 2.4 Repository Pattern
- Database access is centralized in the repository layer.
- Business logic does not deal with SQL directly, which makes the code structure clearer and makes testing and maintenance easier.

### 2.5 Adapter Pattern
- External JSON is first processed by the client and mapper, and then converted into internal records.
- In this way, when the external structure changes, we mainly need to adjust the adapter layer without directly affecting the core business logic.

## 3. Data Design

- In addition to `users` and `posts`, the database also keeps a `raw_source` table to store the original JSON data.
- The main purposes of `raw_source` are:
  1. To keep a traceable copy of the original data
  2. To avoid unnecessary updates by checking whether the data has changed through `payload_hash`
- `payload_hash` is used to compare the newly imported data with the data saved last time. If the hash does not change, it means the data has not changed, and the update can be skipped.
- `address` and `company` are stored as expanded fields instead of a single JSON field. This makes page rendering more direct, and it is also easier to handle if address or company information needs to be updated later.

## 4. UI Design

The current UI uses **Tailwind CSS default styles**, and the overall look stays simple so that the core functionality can be implemented and demonstrated quickly.

If a real project already has **Storybook** or an existing **product design system / component library**, those existing styles and components can be reused directly. This keeps the page style consistent and reduces duplicate work.

If the amount of data grows later, we can further add **search, filter, and paginated loading** features to make it easier for users to find and browse data.

If the system is intended for a broader user group, we also need to consider **accessibility** more carefully, so that users with visual, cognitive, or motor difficulties can still use the page smoothly and the product becomes more inclusive.

## 5. Handling Data Fetch Errors

Because the system depends on an external API for its data source, the page cannot display content properly if data fetching fails. Therefore, the system needs to clearly express the “data fetch failed” state, and that state must be observable and verifiable in the UI.

The frontend currently separates request failures into three categories:
  - **Network error**: the request was not successfully sent, or the backend could not be reached
  - **Backend returned 500**: the backend explicitly returned a 500 error
  - **Data fetch error**: the request reached the backend, but the returned data format was invalid and the frontend failed to parse it

The list page and the detail page also support displaying error states separately, so that a failure in one panel does not affect the entire page.

At the moment, manually blocking the API in DevTools can be used to simulate errors, but that is more of a debugging technique than a product-level solution. A better approach is to make error states a **controllable, reproducible, and observable** feature in the page itself. We can add a **development-only** tool area at the top of the page with the following controls:
  - Simulate network error
  - Simulate 500
  - Simulate invalid JSON
  - Reset error mode
These controls should appear only in `dev` mode and stay hidden in production. The benefit is that testing becomes more stable and does not depend on browser tools or network conditions. This is especially suitable for scenarios where we need to verify copy, state, and visual feedback at the same time.

## 6. External Data Cleaning

`User` data contains nested objects such as `address` and `company`. During data mapping, the external JSON needs to be converted into the internal model before it is stored in database fields.

The mapping stage also handles part of the **data cleaning** work. For example, fields such as phone numbers and addresses may have inconsistent formats in the external data, so basic normalization is needed during mapping. This ensures that the data enters the database in a consistent structure, and later page rendering and querying become more stable. The goal is not just to “save it as-is”, but to keep the data both complete and usable while maintaining consistency.

## 7. Manual Sync vs. Automatic Sync

This project chose **manual sync** instead of automatic sync.

The main reason is that manual sync is more intuitive. It fits the scope of this assessment and makes it easier for users to clearly know when a data update is triggered. Automatic sync may be more convenient, but it increases implementation complexity and makes it harder to clearly observe the result of each import.

## 8. Project Management

- Break the project into several large Epics based on the overall requirements, such as project initialization, database design, data import, query APIs, frontend pages, testing, and delivery.

- Then break each Epic into smaller Trello cards so that each card is responsible for one clear, small goal, which makes it easier to progress and verify step by step.

- During development, complete the cards in order, and after each card is finished, perform the corresponding verification to ensure the current feature works before moving on to the next one.

  ![trello](https://res.cloudinary.com/dyuvwlir0/image/upload/v1780926590/Screenshot_2026-06-08_at_10.11.24_PM_ogr44t.png)

## 9. Gains and Reflection

**What I gained**  
The biggest gain is that I now have a much clearer understanding of the overall design of a software development solution. In school, design patterns and requirements analysis methods often felt like abstract and easy-to-confuse concepts. Through this real project, I connected those concepts to a complete development process, and I now have a more concrete understanding of their roles and boundaries.

**What can be improved**  
There is still a lot of room for improvement in this project. For example, we can add more complete history records to strengthen data traceability. We can also add search, pagination, and filtering features to improve the user experience when the data grows larger. In addition, we can introduce more comprehensive automated tests to further improve the efficiency of regression verification.
