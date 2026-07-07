Status: ready-for-agent

# End-to-end hardening and local run polish

## Parent

.scratch/mini-mart/PRD.md

## What to build

Harden the full Mini-Mart implementation after the main vertical slices are complete. Verify that a fresh checkout can run the app locally, that the shopper and admin flows work together, that error responses are consistent, and that the documented local workflow is accurate.

This slice is the integration pass for the runnable product.

## Acceptance criteria

- [ ] A fresh checkout can start the backend and frontend using the documented commands.
- [ ] Seed data appears correctly in the storefront.
- [ ] The shopper can browse, search, filter, add to cart, edit cart, and checkout successfully.
- [ ] The shopper sees clear errors for insufficient stock and expired cart reservations.
- [ ] The admin can log in, manage categories, manage products, refill stock, soft-delete records, and upload images.
- [ ] Public APIs hide deleted records while admin APIs can fetch them when requested.
- [ ] Inventory metrics remain consistent across reservation, checkout, refill, soft delete, and expiry flows.
- [ ] API error responses are consistent enough for the frontend to handle cleanly.
- [ ] The UI remains visually consistent with the monochrome premium design direction.
- [ ] Automated smoke or end-to-end tests cover the main anonymous shopper path and authenticated admin path.
- [ ] Local run instructions include any required credentials, ports, and upload-directory notes.

## Blocked by

- .scratch/mini-mart/issues/04-admin-catalog-management-soft-delete-inventory.md
- .scratch/mini-mart/issues/06-transactional-session-checkout.md
- .scratch/mini-mart/issues/07-reservation-expiry-inactive-cart-timeout.md
- .scratch/mini-mart/issues/08-admin-product-image-upload-storefront-rendering.md
