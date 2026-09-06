# order-service

Cart-to-order lifecycle. Port `8083`. Interactive API docs at `/swagger-ui.html`.

## Endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/orders` | authenticated | Create an order from `{ items: [{ productId, quantity }] }` |
| GET | `/api/orders` | authenticated | Paginated list of the caller's own orders |
| GET | `/api/orders/{id}` | authenticated (owner only) | Order detail; 403 if it belongs to another user |

## How order creation works
1. For each line item, calls `product-service` (`GET /api/products/{id}`) to get the current price and stock - price and stock are never trusted from the client.
2. Rejects the order (409) if requested quantity exceeds available stock.
3. Persists the order + line items, then fires a best-effort call to `notification-service` (failures are logged, never block the order).

## Config
- `services.product-service.url` (`PRODUCT_SERVICE_URL`) - where to validate stock/price.
- `services.notification-service.url` (`NOTIFICATION_SERVICE_URL`) - where to send the order-confirmation email.
- `jwt.secret` (`JWT_SECRET`) - must match auth-service's signing secret.

## Tests
Has both Mockito unit tests (`OrderServiceTest`) and a Testcontainers integration test (`OrderControllerIntegrationTest`, real Postgres, `product-service`/`notification-service` mocked) - see [CONTRIBUTING.md](../../CONTRIBUTING.md).
