# Frontend

Next.js (App Router, TypeScript, Tailwind) storefront for the EcommercePlatform microservices.

## Pages
| Route | Description |
|---|---|
| `/` | Home |
| `/products`, `/products/[id]` | Catalog listing and detail (server components, hit product-service via the gateway) |
| `/cart` | Client-side cart (localStorage) + Razorpay checkout |
| `/login`, `/register` | Auth forms |
| `/orders` | Order history for the logged-in user |

## Auth model
Sessions are httpOnly cookies set by auth-service - the frontend never stores or reads the JWT itself. `AuthContext` only tracks `{ email, role }` (non-sensitive, for UI display) and every API call uses `credentials: "include"` so the browser attaches the cookie automatically.

## Env vars
Copy `.env.local.example` to `.env.local`:
```
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```
This should point at the api-gateway, not at an individual service.

## Commands
```
npm run dev      # dev server on :3000
npm run lint      # eslint
npx tsc --noEmit  # typecheck
npm run build     # production build
```
