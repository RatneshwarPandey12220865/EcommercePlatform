# Security Policy

## Reporting a vulnerability
This is a learning/portfolio project, not a production service handling real payments or user data. If you find a security issue anyway, please open a GitHub issue describing it rather than a public PR with an exploit - happy to fix it.

## Current security posture
- **Auth**: JWTs are issued by auth-service and set as `httpOnly`, `SameSite=Lax` cookies - never exposed to JS, so an XSS bug can't exfiltrate the session token the way it could with `localStorage`-based tokens.
- **Passwords**: hashed with BCrypt, never stored or logged in plaintext.
- **Payments**: Razorpay webhook and client-verification calls are validated via HMAC signature (`Utils.verifyPaymentSignature` / `verifyWebhookSignature`) before a payment is ever marked `PAID`.
- **CORS**: the gateway only allows the configured `FRONTEND_ORIGIN`, with credentials enabled - not a wildcard.
- **Secrets**: `JWT_SECRET`, Razorpay keys, and SMTP credentials are read from environment variables with dev-only placeholder defaults; real values are never committed (`.env` is gitignored, only `.env.example` files are tracked).
- **SQL injection**: all data access goes through Spring Data JPA repositories (parameterized queries) - no raw string-concatenated SQL.

## Known gaps (by design, for this project's scope)
- No refresh-token rotation/revocation store - a stolen refresh token is valid until it expires.
- Rate limiting is a single-instance, in-memory, per-IP limiter - not suitable as-is for a multi-instance deployment (would need a shared store).
- No WAF/DDoS protection layer - this is a local/dev-oriented stack, not internet-hardened infrastructure.
