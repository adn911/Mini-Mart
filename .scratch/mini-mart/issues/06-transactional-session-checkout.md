Status: resolved

# Transactional checkout from current session cart

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add checkout from the current anonymous session cart. Checkout must not accept arbitrary product IDs and quantities from the client. It should transactionally convert the active session cart into an order, create order items, finalize stock deduction, and return a clear success or failure response for the frontend.

This slice completes the first purchasable path for active, non-expired carts.

## Acceptance criteria

- [ ] Checkout uses only the current backend session cart.
- [ ] Checkout does not accept arbitrary product IDs and quantities from the client.
- [ ] Checkout fails with a clear bad-request response when the current cart is empty.
- [ ] Checkout runs inside a database transaction.
- [ ] Successful checkout creates an order with status `PLACED`.
- [ ] Successful checkout creates order items with quantity, price, product relationship, and order relationship.
- [ ] Successful checkout finalizes physical stock deduction.
- [ ] Successful checkout clears or marks the session cart as checked out.
- [ ] Failed checkout does not partially create orders or partially deduct stock.
- [ ] The storefront shows checkout success and checkout failure states.
- [ ] Automated tests cover successful checkout, empty cart checkout, stock deduction, order creation, cart finalization, and transaction rollback on failure.

## Blocked by

- .scratch/mini-mart/issues/05-session-cart-stock-reservation.md
