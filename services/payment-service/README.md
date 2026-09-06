# payment-service

Razorpay order creation and payment verification. Port `8084`. Interactive API docs at `/swagger-ui.html`.

## Endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/payments` | authenticated | Create a Razorpay order for `{ orderId, amount }`, returns the Razorpay order id + publishable key for Checkout.js |
| POST | `/api/payments/verify` | public | Verify a completed payment's signature (`razorpayOrderId`, `razorpayPaymentId`, `razorpaySignature`) |
| GET | `/api/payments/order/{orderId}` | authenticated | Look up payment status for an order |
| POST | `/api/payments/webhook` | public (signature-verified) | Razorpay server-to-server webhook (`payment.captured` / `payment.failed`) - a durability backstop alongside `/verify` |

## Setup
Copy `.env.example` to `.env` and fill in real Razorpay **test-mode** credentials:
```
RAZORPAY_KEY_ID=rzp_test_...
RAZORPAY_KEY_SECRET=...
RAZORPAY_WEBHOOK_SECRET=...
```
Without real credentials the service still starts (placeholder defaults), but any call that hits the Razorpay API will fail with a 502.

## Notification
On successful verification, fires a best-effort call to `notification-service` for a payment-confirmation email (failures logged, never block the response).
