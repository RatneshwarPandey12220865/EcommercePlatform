# Architecture

## Services
| Service | Port | Responsibility | DB |
|---|---|---|---|
| auth-service | 8081 | registration, login, JWT issuance, roles | ecommerce_auth |
| product-service | 8082 | catalog, categories, search | ecommerce_product |
| order-service | 8083 | cart, orders, order lifecycle | ecommerce_order |
| payment-service | 8084 | payment intents, gateway webhooks | ecommerce_payment |
| notification-service | 8085 | email/SMS on order/payment events | ecommerce_notification |
| frontend (Next.js) | 3000 | storefront UI | - |

## Communication
- Frontend -> services: REST over an API gateway (Spring Cloud Gateway) at :8080, so the browser only ever talks to one origin.
- Service -> service: REST for synchronous needs (order-service calls product-service to check stock, calls auth-service to validate a user).
- Order/payment/notification: async via events (RabbitMQ) once Phase 6 is reached — order placed -> payment requested -> payment confirmed -> notification sent. Kept REST-only until then to avoid premature complexity.

## Data ownership
Each service owns its own Postgres schema/database. No service reads another's tables directly — only through its API. Each runs its own Flyway/JPA migrations.

## Auth model
- auth-service issues short-lived JWT access tokens + refresh tokens.
- Other services are resource servers: they validate the JWT signature/claims locally (shared public key or a shared secret in dev), no per-request call back to auth-service.

## Phase status
- [ ] Phase 0 - Setup & Architecture
- [ ] Phase 1 - Core infra skeleton
- [ ] Phase 2 - Auth service
- [ ] Phase 3 - Product service
- [ ] Phase 4 - Order service
- [ ] Phase 5 - Payment service
- [ ] Phase 6 - Notification service
- [ ] Phase 7 - Cross-cutting (gateway, logging, rate limiting)
- [ ] Phase 8 - Testing
- [ ] Phase 9 - CI/CD
- [ ] Phase 10 - Deployment
- [ ] Phase 11 - Hardening & polish
