# Architecture

## Services
| Service | Port | Responsibility | DB |
|---|---|---|---|
| api-gateway | 8080 | single entry point, routing, CORS, rate limiting, correlation IDs | - |
| auth-service | 8081 | registration, login, JWT issuance, roles | ecommerce_auth |
| product-service | 8082 | catalog, categories, search | ecommerce_product |
| order-service | 8083 | cart, orders, order lifecycle | ecommerce_order |
| payment-service | 8084 | Razorpay order creation, signature/webhook verification | ecommerce_payment |
| notification-service | 8085 | email on order/payment events (internal-only, not routed via gateway) | ecommerce_notification |
| frontend (Next.js) | 3000 | storefront UI | - |

## Communication
- Frontend -> services: REST over api-gateway (Spring Cloud Gateway Server MVC) at :8080. The browser only ever talks to this one origin; the gateway path-routes to auth/product/order/payment-service. notification-service is deliberately not routed here since it's only ever called service-to-service.
- Service -> service: REST via Spring's `RestClient` (order-service -> product-service for stock/price checks; order-service and payment-service -> notification-service, best-effort/fire-and-forget).
- Notifications are triggered by direct REST calls rather than a message broker (RabbitMQ) — call volume doesn't yet justify that infra; revisit if event fan-out or retry/replay semantics become necessary.
- Cross-cutting: the gateway stamps every request with an `X-Request-Id` correlation header (generated or passed through) and applies a simple per-IP fixed-window rate limit.

## Data ownership
Each service owns its own Postgres schema/database. No service reads another's tables directly — only through its API. Each runs its own Flyway/JPA migrations.

## Auth model
- auth-service issues short-lived JWT access tokens + refresh tokens.
- Other services are resource servers: they validate the JWT signature/claims locally (shared public key or a shared secret in dev), no per-request call back to auth-service.

## Phase status
- [x] Phase 0 - Setup & Architecture
- [x] Phase 1 - Core infra skeleton
- [x] Phase 2 - Auth service
- [x] Phase 3 - Product service
- [x] Phase 4 - Order service
- [x] Phase 5 - Payment service
- [x] Phase 6 - Notification service
- [x] Phase 7 - Cross-cutting (gateway, logging, rate limiting)
- [ ] Phase 8 - Testing
- [ ] Phase 9 - CI/CD
- [ ] Phase 10 - Deployment
- [ ] Phase 11 - Hardening & polish
