# Users Posts Browser

Monorepo scaffold for the JSONPlaceholder users/posts browser.

## Stack

- Backend: Spring Boot
- Frontend: React + Vite
- Database: PostgreSQL
- Runtime: Docker Compose

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
