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

## Environment

- Copy `.env.example` to `.env` if you want to customize local values.
- The backend reads `DB_*` and `SERVER_PORT`.
- The frontend is ready for `VITE_*` variables in future feature work.

## Project Layout

- `backend/` Spring Boot service
- `frontend/` React/Vite app
- `docker-compose.yml` local runtime for the whole stack
