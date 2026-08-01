# Users Posts Browser

Monorepo scaffold for the JSONPlaceholder users/posts browser.

## Stack

- Backend: Spring Boot
- Frontend: React + Vite
- Database: Amazon DynamoDB
- Runtime: Docker Compose

## Project Docs

If you want to understand or submit the current DCE04.2 assessment solution, read these documents in order:

1. [Assessment Step-by-Step Guide](./docs/dce04-assessment1-step-by-step-guide.md) - cloud setup, local preparation, Terraform, ECS, CI/CD, Entra SAML, screenshots, and cleanup
2. [Assessment Report Template](./docs/dce04-assessment1-report-template.md) - report structure with screenshot placeholders
3. [Solution Design](./MySolution.MD) - application design updated for the DynamoDB route

## Quick Start

1. Make sure Docker is running.
2. Make sure your AWS CLI credentials can access DynamoDB in `ap-southeast-2`.
3. Create or reuse a DynamoDB table named `dce042-users-posts` with:
   - Partition key: `pk` string
   - Sort key: `sk` string
   - Billing mode: on-demand
4. From the repository root, run:

```bash
docker compose up --build
```

5. Open the app:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- DynamoDB table: dce042-users-posts

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
  - inspect the `dce042-users-posts` DynamoDB table after sync

Useful API checks:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/transactions/config
curl -X POST http://localhost:8080/api/transactions/demo
```

The health and config endpoints do not write data. The sync button and `POST /api/transactions/demo` write items to DynamoDB.

## Development Flow

If you want to follow the same development path used in this project, start with the test cases and then move through the epics in order:

1. Read [Epic Test Cases](./epic-test-cases.md) to understand the expected behavior.
2. Read [Project Summary and Reflection](./Project_Summary_and_Reflection.md) to understand the solution thinking.
3. Use [MySolution.MD](./MySolution.MD) as the detailed design reference.
4. Then run the system using the Quick Start steps above and verify each epic as you go.

## Environment

- Copy `.env.example` to `.env` if you want to customize local values.
- The backend reads `SERVER_PORT`, `AWS_REGION`, `DYNAMODB_TABLE_NAME`, and standard AWS credential variables.
- The frontend is ready for `VITE_*` variables in future feature work.
- Optional dev-only reset tooling:
  - set `APP_DEV_RESET_ENABLED=true` to expose `POST /api/dev/reset`
  - set `VITE_ENABLE_DEV_TOOLS=true` to show the reset button in the frontend

## Project Layout

- `backend/` Spring Boot service
- `frontend/` React/Vite app
- `docker-compose.yml` local runtime for the whole stack
