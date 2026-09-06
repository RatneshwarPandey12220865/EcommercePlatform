# product-service

Product catalog and categories. Port `8082`. Interactive API docs at `/swagger-ui.html`.

## Endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/products` | public | Paginated search, optional `categoryId` / `name` filters |
| GET | `/api/products/{id}` | public | Product detail |
| POST | `/api/products` | ADMIN | Create product |
| PUT | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Delete product |
| GET | `/api/categories` | public | List categories |
| POST | `/api/categories` | ADMIN | Create category |

Role is read from the `role` claim of the JWT (cookie or `Authorization: Bearer` header) issued by auth-service.

## Schema notes
`products` has indexes on `category_id` and `name` (see `Product` entity) since catalog browsing is the highest-traffic read path.

## Run locally
```
./mvnw spring-boot:run
```
Requires Postgres at the URL in `application.properties`.
