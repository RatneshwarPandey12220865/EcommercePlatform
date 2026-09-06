# Contributing

## Local setup
1. Install Java 17, Node.js 22, Docker, and Maven is not required (each service ships its own wrapper: `./mvnw`).
2. Copy `.env.example` to `.env` at the repo root and fill in the Razorpay test keys (see [services/payment-service/.env.example](services/payment-service/.env.example)).
3. Start the stack: `docker compose -f infra/docker/docker-compose.yml up -d`.
4. Frontend dev server: `cd frontend && npm install && npm run dev`.

## Making changes
- Each service is independently buildable: `cd services/<service> && ./mvnw test`.
- Frontend: `cd frontend && npm run lint && npx tsc --noEmit && npm run build`.
- Keep changes to one service/concern per commit where practical - it keeps the history and CI matrix easy to reason about.
- New endpoints should be reflected in that service's `SecurityConfig` (public vs authenticated vs role-gated) and, where meaningful, covered by a test.

## Tests
- `auth-service` and `order-service` have both unit (Mockito) and integration (Testcontainers, real Postgres) tests - follow that pattern for new business logic in those services.
- Testcontainers tests need a running Docker daemon; they run automatically in CI (GitHub Actions ubuntu runners ship Docker preinstalled).

## Pull requests
- Run the relevant service's test suite (and the frontend's lint/typecheck/build) locally before opening a PR.
- CI (`.github/workflows/ci.yml`) runs a build+test matrix across all six backend services plus the frontend on every PR.
