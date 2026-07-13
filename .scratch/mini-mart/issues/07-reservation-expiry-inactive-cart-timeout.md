Status: resolved

# Reservation expiry and inactive cart timeout

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add 30-minute inactive cart and reservation expiry. A scheduled backend job should mark inactive carts/reservations as expired, and expired reservations should no longer reduce available stock. Checkout should fail if any session cart item or reservation has expired, and the frontend should present a clear message.

This slice prevents abandoned anonymous carts from holding inventory forever.

## Acceptance criteria

- [ ] Active carts track last activity or equivalent expiry information.
- [ ] Cart inactivity timeout is 30 minutes.
- [ ] A scheduled backend job finds inactive carts/reservations older than the timeout.
- [ ] Expired carts and reservations are marked `EXPIRED`.
- [ ] Expired reservations no longer reduce available stock.
- [ ] Cart read behavior handles expired carts consistently.
- [ ] Checkout fails if any current session cart item or reservation expired.
- [ ] The storefront shows a clear expired-cart or expired-item checkout failure message.
- [ ] Automated tests cover scheduler expiry, released availability after expiry, cart read behavior after expiry, and checkout failure for expired cart items.

## Blocked by

- .scratch/mini-mart/issues/05-session-cart-stock-reservation.md
- .scratch/mini-mart/issues/06-transactional-session-checkout.md
