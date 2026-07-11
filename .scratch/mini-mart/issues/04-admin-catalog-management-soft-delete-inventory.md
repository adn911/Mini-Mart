Status: resolved

# Admin catalog management with soft delete and inventory metrics

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add authenticated admin catalog management for products and categories. Admins should be able to create, edit, list, soft-delete, and fetch active or deleted records when needed. The admin inventory dashboard should show physical stock, reserved stock, available stock, muted low-stock warnings, and quick refill behavior.

## Acceptance criteria

- [x] Authenticated admins can list active products by default.
- [x] Authenticated admins can request deleted products when needed.
- [x] Authenticated admins can create and edit products.
- [x] Authenticated admins can soft-delete products.
- [x] Authenticated admins can refill a product, increasing physical stock.
- [x] Authenticated admins can list active categories by default.
- [x] Authenticated admins can request deleted categories when needed.
- [x] Authenticated admins can create and edit categories.
- [x] Authenticated admins can soft-delete categories.
- [x] Public catalog APIs continue to hide deleted products and categories.
- [x] The admin dashboard shows stockQuantity, reservedQuantity, and availableQuantity.
- [x] Low-stock warnings use a simple fixed threshold and muted styling.
- [x] The admin UI supports product/category forms, product table management, soft-delete actions, and quick refill.
- [x] Automated tests cover admin catalog CRUD behavior, soft-delete filtering, refill behavior, and inventory metric calculation.

## Blocked by

- .scratch/mini-mart/issues/02-public-catalog-foundation.md
- .scratch/mini-mart/issues/03-admin-login-protected-console-shell.md

## Comments

Implemented Jul 11 2026. Backend: AdminProductController (GET/POST/PUT/DELETE /api/admin/products, refill), AdminCategoryController (GET/POST/PUT/DELETE /api/admin/categories), ProductService/CategoryService with CRUD and soft-delete, 10 integration tests. Frontend: Full AdminConsole rewrite with Products/Categories tabs, product table with stock metrics (stock/reserved/available), low-stock warnings (<10 threshold), inline refill input, soft-delete actions, product/category forms, show-deleted toggle, 2 frontend tests covering dashboard and low-stock display.
