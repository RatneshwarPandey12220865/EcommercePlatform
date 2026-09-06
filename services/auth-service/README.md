# auth-service

Registration, login, and JWT issuance. Port `8081`. Interactive API docs at `/swagger-ui.html` once running.

## Endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Create a user, set `access_token`/`refresh_token` httpOnly cookies |
| POST | `/api/auth/login` | public | Authenticate, set the same cookies |
| POST | `/api/auth/logout` | public | Clear the auth cookies |

Register/login responses return only `{ email, role }` in the body - the tokens themselves never appear in JSON, only as httpOnly, `SameSite=Lax` cookies, so they aren't reachable from JS (XSS-resistant).

## Config
See `src/main/resources/application.properties` for the full list. Key ones:
- `JWT_SECRET` - HMAC signing secret shared with every other service (they validate, only this service issues).
- `JWT_ACCESS_TTL_MIN` / `JWT_REFRESH_TTL_DAYS` - token lifetimes.
- `JWT_COOKIE_SECURE` - set `true` behind HTTPS in production.

## Run locally
```
./mvnw spring-boot:run
```
Requires a Postgres instance reachable at the URL in `application.properties` (or use `docker compose up postgres` from the repo root).
