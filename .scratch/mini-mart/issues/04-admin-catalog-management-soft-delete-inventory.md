Status: ready-for-agent

# Admin catalog management with soft delete and inventory metrics

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add authenticated admin catalog management for products and categories. Admins should be able to create, edit, list, soft-delete, and fetch active or deleted records when needed. The admin inventory dashboard should show physical stock, reserved stock, available stock, muted low-stock warnings, and quick refill behavior.

This slice makes catalog operations manageable from the admin console while preserving public soft-delete behavior.

## Acceptance criteria

- [ ] Authenticated admins can list active products by default.
- [ ] Authenticated admins can request deleted products when needed.
- [ ] Authenticated admins can create and edit products.
- [ ] Authenticated admins can soft-delete products.
- [ ] Authenticated admins can refill a product, increasing physical stock.
- [ ] Authenticated admins can list active categories by default.
- [ ] Authenticated admins can request deleted categories when needed.
- [ ] Authenticated admins can create and edit categories.
- [ ] Authenticated admins can soft-delete categories.
- [ ] Public catalog APIs continue to hide deleted products and categories.
- [ ] The admin dashboard shows `stockQuantity`, `reservedQuantity`, and `availableQuantity`.
- [ ] Low-stock warnings use a simple fixed threshold and muted styling.
- [ ] The admin UI supports product/category forms, product table management, soft-delete actions, and quick refill.
- [ ] Automated tests cover admin catalog CRUD behavior, soft-delete filtering, refill behavior, and inventory metric calculation.

## Blocked by

- .scratch/mini-mart/issues/02-public-catalog-foundation.md
- .scratch/mini-mart/issues/03-admin-login-protected-console-shell.md
