# notification-service

Sends order/payment confirmation emails. Port `8085`. Interactive API docs at `/swagger-ui.html`.

Called service-to-service only (`order-service`, `payment-service`) - it is **not** routed through the API gateway and has no user-facing auth.

## Endpoints
| Method | Path | Description |
|---|---|---|
| POST | `/api/notifications/order-confirmation` | `{ userEmail, orderId, totalAmount }` |
| POST | `/api/notifications/payment-confirmation` | `{ userEmail, orderId, amount }` |

Every send attempt (success or failure) is recorded in the `notification_logs` table for audit/debugging.

## Sending mode
- `NOTIFICATION_MODE=log` (default) - writes the email to the application log instead of sending it. No SMTP setup needed for local dev.
- `NOTIFICATION_MODE=smtp` - sends for real via `spring.mail.*` / `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD`.

Same `EmailSender` interface either way (`LoggingEmailSender` vs `SmtpEmailSender`), so switching modes is a config change, not a code change.
