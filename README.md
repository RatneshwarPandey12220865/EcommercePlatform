# E-Commerce Platform

Microservices e-commerce backend (Spring Boot) + Next.js storefront.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for service breakdown and phase roadmap.

## Structure
```
services/
  auth-service/
  product-service/
  order-service/
  payment-service/
  notification-service/
frontend/            # Next.js app
infra/
  docker/            # docker-compose, per-service Dockerfiles
  github-actions/    # CI workflows
docs/
```

## Local dev
```
docker compose -f infra/docker/docker-compose.yml up -d
```
Each service runs on its own port (see ARCHITECTURE.md). Frontend on :3000.
