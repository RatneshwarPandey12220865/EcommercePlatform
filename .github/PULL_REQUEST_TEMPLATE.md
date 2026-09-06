## Summary

<!-- What does this change and why? -->

## Affected service(s)

<!-- e.g. auth-service, frontend -->

## Test plan

- [ ] `./mvnw test` passes for each affected service
- [ ] `npm run lint && npx tsc --noEmit && npm run build` passes (if frontend touched)
- [ ] Manually verified the change locally (`docker compose up`)

## Checklist

- [ ] New/changed endpoints reflected in the relevant `SecurityConfig`
- [ ] No secrets or `.env` files committed
