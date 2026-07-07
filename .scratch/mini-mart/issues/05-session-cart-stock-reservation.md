Status: ready-for-agent

# Session-backed cart with stock reservation

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add anonymous backend session cart behavior with stock reservation. The frontend cart panel should talk to backend cart APIs and treat backend responses as authoritative. Adding an item reserves stock, updating quantity adjusts the reservation, and removing an item releases reserved stock.

This slice should make cart behavior demoable without requiring checkout yet.

## Acceptance criteria

- [ ] Anonymous shoppers get a backend session-backed cart.
- [ ] The frontend does not use local storage as the authoritative cart source.
- [ ] The frontend may use local storage only for lightweight non-authoritative UI convenience if needed.
- [ ] Shoppers can add a product to the cart from the storefront.
- [ ] Adding a product reserves available stock on the backend.
- [ ] Adding or increasing quantity fails clearly when available stock is insufficient.
- [ ] Shoppers can view the current cart in a right-side panel.
- [ ] Shoppers can increase or decrease cart item quantity.
- [ ] Quantity updates adjust reserved stock correctly.
- [ ] Shoppers can remove a cart item.
- [ ] Removing a cart item releases the reserved stock.
- [ ] Cart item count reflects backend cart state.
- [ ] Product availability reflects active reservations.
- [ ] Automated tests cover session cart creation, add/update/remove behavior, insufficient stock errors, reservation totals, and availability changes.

## Blocked by

- .scratch/mini-mart/issues/02-public-catalog-foundation.md
