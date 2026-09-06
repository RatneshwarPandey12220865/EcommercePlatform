# api-gateway

Single entry point for the frontend. Port `8080`. Built on Spring Cloud Gateway Server MVC.

## Routes
| Path prefix | Forwards to |
|---|---|
| `/api/auth/**` | auth-service |
| `/api/products/**`, `/api/categories/**` | product-service |
| `/api/orders/**` | order-service |
| `/api/payments/**` | payment-service |

`notification-service` is deliberately **not** routed here - it's only ever called service-to-service, never from the browser.

## Cross-cutting behavior
- **CORS**: only `FRONTEND_ORIGIN` (default `http://localhost:3000`) is allowed, with credentials (cookies) enabled - see `CorsConfig`.
- **Correlation IDs**: every request gets an `X-Request-Id` header (generated if the client didn't send one) so a single request can be traced across every downstream service's logs - see `CorrelationIdFilter`.
- **Rate limiting**: a simple per-IP fixed-window limiter (`RATE_LIMIT_RPM`, default 120/min) - see `RateLimitFilter`. Single-instance only; a multi-instance deployment would need a shared store (e.g. Redis) instead.

## Config
Each downstream service's URL is overridable via env var: `AUTH_SERVICE_URL`, `PRODUCT_SERVICE_URL`, `ORDER_SERVICE_URL`, `PAYMENT_SERVICE_URL` (see `application.yml`).
