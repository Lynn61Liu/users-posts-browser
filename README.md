# Users Posts Browser

Monorepo scaffold for the JSONPlaceholder users/posts browser.

## Stack

- Backend: Spring Boot
- Frontend: React + Vite
- Database: PostgreSQL
- Runtime: Docker Compose

## Project Docs

If you want to understand the implementation approach while setting up the system, read these documents in order:

1. [Project Summary and Reflection](./Project_Summary_and_Reflection.md) - my implementation story, design decisions, and reflection
2. [Solution Design](./MySolution.MD) - the original solution plan and architecture
3. [Epic Test Cases](./epic-test-cases.md) - the epic-by-epic test case checklist used during development

## Quick Start

1. Make sure Docker is running.
2. From the repository root, run:

```bash
docker compose up --build
```

3. Open the app:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

## Verification

- Backend tests:
```bash
cd backend
./mvnw test
```

- Frontend tests and build:
```bash
cd frontend
npm test
npm run build
```

- Full stack smoke check:
```bash
docker compose up --build
```
- After the app starts:
  - open http://localhost:3000
  - trigger sync from the header button
  - optionally inspect PostgreSQL data after sync

## Development Flow

If you want to follow the same development path used in this project, start with the test cases and then move through the epics in order:

1. Read [Epic Test Cases](./epic-test-cases.md) to understand the expected behavior.
2. Read [Project Summary and Reflection](./Project_Summary_and_Reflection.md) to understand the solution thinking.
3. Use [MySolution.MD](./MySolution.MD) as the detailed design reference.
4. Then run the system using the Quick Start steps above and verify each epic as you go.

## Environment

- Copy `.env.example` to `.env` if you want to customize local values.
- The backend reads `DB_*` and `SERVER_PORT`.
- The frontend is ready for `VITE_*` variables in future feature work.
- Optional dev-only reset tooling:
  - set `APP_DEV_RESET_ENABLED=true` to expose `POST /api/dev/reset`
  - set `VITE_ENABLE_DEV_TOOLS=true` to show the reset button in the frontend

## Project Layout

- `backend/` Spring Boot service
- `frontend/` React/Vite app
- `docker-compose.yml` local runtime for the whole stack
