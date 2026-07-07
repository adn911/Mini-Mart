Status: ready-for-agent

# PRD: Mini-Mart Runnable Commerce Monorepo

## Problem Statement

The user needs Mini-Mart to move from a broad requirements note into an implementation-ready product specification for a runnable e-commerce codebase. The original requirements define a storefront, admin console, Spring Boot backend, React frontend, inventory, orders, and a premium minimalist UI, but they leave important behavior unresolved around authentication, stock reservation, cart ownership, soft deletion, image uploads, order status, and project packaging.

Without a refined PRD, an implementation agent would need to guess at business rules that directly affect schema design, API contracts, transactions, and user experience. The user needs those decisions captured clearly so the app can be built as a runnable multi-module Gradle monorepo without additional requirements interviews.

## Solution

Build Mini-Mart as a runnable multi-module Gradle monorepo with a Spring Boot backend and a React/Tailwind frontend.

The public storefront supports anonymous shopping with a backend session-backed cart. Adding items reserves stock, removing items releases stock, and checkout converts the current session cart into an order. A scheduled job expires inactive carts and reservations after 30 minutes. Inventory APIs expose physical stock, active reserved quantity, and derived available quantity.

The admin console is separated from the storefront and protected by a simple admin authentication skeleton. Admin credentials are loaded from a JSON resource rather than the database, and successful login returns a signed JWT used to access admin APIs. Admins manage products, categories, inventory refills, product image uploads, and soft deletion.

The first version uses H2 with seed demo data, but the backend should be designed so PostgreSQL can be added later. The frontend should deliver a refined monochrome, premium, minimalist experience with clean product cards, cart panel interactions, and a dense but polished admin inventory dashboard.

## User Stories

1. As a shopper, I want to browse active products, so that I can see what Mini-Mart currently sells.
2. As a shopper, I want deleted products hidden from the storefront, so that I only see purchasable items.
3. As a shopper, I want to browse active categories, so that I can narrow the product catalog.
4. As a shopper, I want deleted categories hidden from the storefront, so that unavailable catalog structure does not appear.
5. As a shopper, I want to search products by text, so that I can quickly find items by name or description.
6. As a shopper, I want to filter products by category, so that I can focus on relevant items.
7. As a shopper, I want product images to display consistently, so that the storefront feels polished and easy to scan.
8. As a shopper, I want product prices to be visually clear, so that I can make purchase decisions quickly.
9. As a shopper, I want unavailable stock to be reflected in the UI, so that I do not try to buy items that cannot be reserved.
10. As a shopper, I want to add a product to my cart, so that I can reserve it for checkout.
11. As a shopper, I want adding to cart to reserve stock on the backend, so that other anonymous shoppers cannot overtake the same inventory.
12. As a shopper, I want the cart to survive page reloads during my session, so that I do not lose my current shopping work.
13. As a shopper, I want the backend to be the source of truth for my cart, so that cart state cannot be corrupted by browser storage.
14. As a shopper, I want to see the cart item count, so that I understand how many items I am preparing to buy.
15. As a shopper, I want a cart panel that opens over the storefront, so that I can review my cart without losing browsing context.
16. As a shopper, I want to increase item quantity in the cart, so that I can reserve more units when available.
17. As a shopper, I want to decrease item quantity in the cart, so that I can release units I no longer need.
18. As a shopper, I want to remove an item from the cart, so that its stock reservation is released.
19. As a shopper, I want clear feedback when stock is insufficient, so that I understand why an add or update failed.
20. As a shopper, I want my cart reservation to expire after inactivity, so that abandoned carts do not hold inventory forever.
21. As a shopper, I want checkout to fail if any cart reservation expired, so that I do not unknowingly buy an inconsistent cart.
22. As a shopper, I want checkout to use only my current session cart, so that the client cannot submit arbitrary product IDs and quantities.
23. As a shopper, I want checkout to create an order transactionally, so that stock and order data stay consistent.
24. As a shopper, I want a successful checkout to finalize stock deduction, so that the order reflects real inventory consumption.
25. As a shopper, I want a clean confirmation flow, so that I know the purchase order was placed.
26. As an admin, I want to access the admin console from a separate route, so that management workflows are isolated from shopping workflows.
27. As an admin, I want to log in before using the admin console, so that admin tools are not publicly accessible.
28. As an admin, I want the backend to validate my login against configured credentials, so that v1 has a simple authentication skeleton without a user database.
29. As an admin, I want admin API calls protected by a signed token, so that product and inventory changes require authentication.
30. As an admin, I want to view all active products in a table, so that I can manage inventory efficiently.
31. As an admin, I want the dashboard to show physical stock, reserved stock, and available stock, so that I can understand inventory pressure.
32. As an admin, I want low-stock warnings to be muted but visible, so that I notice risk without a noisy interface.
33. As an admin, I want to create products, so that new items can appear in the storefront.
34. As an admin, I want to edit products, so that product details and stock can be corrected.
35. As an admin, I want to soft-delete products, so that removed products disappear publicly without losing records.
36. As an admin, I want to fetch deleted products when needed, so that I can audit or potentially restore removed catalog items later.
37. As an admin, I want to create categories, so that products can be organized.
38. As an admin, I want to edit categories, so that category names can be maintained.
39. As an admin, I want to soft-delete categories, so that category records are preserved while hidden from public APIs.
40. As an admin, I want to fetch deleted categories when needed, so that I can inspect removed catalog structure.
41. As an admin, I want to refill a product quickly, so that routine restocking is fast.
42. As an admin, I want product images uploaded through the admin console, so that products can have real visual assets.
43. As an admin, I want uploaded images served through stable URLs, so that the frontend can render product images from database references.
44. As an admin, I want invalid image types rejected, so that unsupported uploads do not enter the system.
45. As an admin, I want oversized images rejected, so that local storage and frontend performance remain reasonable.
46. As a developer, I want the app delivered as a runnable monorepo, so that frontend and backend can be developed together.
47. As a developer, I want H2 seed data, so that the app is useful immediately after startup.
48. As a developer, I want PostgreSQL to be a future-friendly path, so that the data layer does not block production hardening.
49. As a developer, I want domain rules enforced in backend services, so that frontend behavior cannot bypass stock or auth constraints.
50. As a developer, I want high-level tests around public API behavior and admin flows, so that the app can evolve without breaking core commerce behavior.

## Implementation Decisions

- Build a multi-module Gradle monorepo with separate backend and frontend modules.
- The backend uses Spring Boot, Spring Web, Spring Security, Spring Data JPA, Hibernate, and H2 for v1.
- The frontend uses React and Tailwind CSS, preferably with Vite.
- The public storefront is anonymous for v1.
- Customer accounts, customer identity on orders, shipping details, and payments are deferred.
- The schema should leave room for future customer accounts and user-linked orders.
- Admin access is separated from the storefront by route.
- Admin authentication is required in v1.
- Admin credentials are loaded from a JSON resource and remain plaintext for v1.
- Admin credentials are not stored in the database.
- Successful admin login returns a simple signed JWT.
- Admin APIs require the admin JWT.
- Product has an identifier, name, description, price, physical stock quantity, image URL, status, and category relationship.
- Category has an identifier, name, and status.
- Product and category deletion is soft deletion.
- Public APIs hide deleted products and categories.
- Admin APIs return active products and categories by default.
- Admin APIs can include deleted products and categories when explicitly requested.
- Order has an identifier, order date, total amount, and status.
- Valid order statuses are `PLACED`, `CANCELLED`, and `COMPLETED`.
- Order item has an identifier, quantity, price, product relationship, and order relationship.
- Full immutable product snapshots in order items are deferred for v1.
- The authoritative cart lives on the backend and is tied to the anonymous HTTP session.
- Frontend local storage is not the source of truth for cart contents.
- Frontend local storage may be used only for lightweight non-authoritative UI convenience.
- Cart inactivity timeout is 30 minutes.
- Cart statuses should cover active, checked out, and expired states.
- Adding a product to cart reserves stock on the backend.
- Removing a cart item releases reserved stock.
- Updating cart item quantity adjusts the reservation.
- Reservation fails if available stock is insufficient.
- Expired carts and reservations are marked expired.
- A scheduled backend job expires inactive carts and reservations.
- Physical stock is represented by `stockQuantity`.
- Active reserved stock is represented separately from physical stock.
- Available stock is derived as physical stock minus active reserved stock.
- Admin inventory surfaces show physical stock, reserved stock, and available stock.
- Checkout uses only the current session cart.
- Checkout does not accept arbitrary product IDs and quantities from the client.
- Checkout fails if any session cart item or reservation expired.
- Checkout runs transactionally.
- Checkout converts valid active reservations into an order.
- Checkout finalizes stock deduction.
- Public product listing supports search and category filtering.
- Public category listing returns active categories only.
- Cart APIs expose read, add, update, and remove operations using the current session.
- Order creation API uses the current session cart.
- Admin product APIs support create, update, soft delete, list, and refill.
- Admin category APIs support create, update, soft delete, and list.
- Product refill increases physical stock.
- Product images are uploaded by admins.
- Uploaded product images are stored on the backend filesystem and served from a stable uploads URL.
- The product image URL stored in the database is the retrievable URL/path.
- Image uploads accept JPEG, PNG, and WebP.
- Image uploads reject files larger than 5 MB.
- Image files are not deleted synchronously when a product is deleted or its image is replaced.
- Future unreferenced image cleanup can be handled by a cron.
- Seed data includes demo categories and products compatible with H2.
- The storefront UI includes branding, search, category filtering, product grid, cart badge, cart panel, quantity controls, removal, and checkout.
- The cart UI calls backend cart APIs and treats backend responses as authoritative.
- The admin UI includes login, inventory dashboard, product table, category management, product forms, image upload, quick refill, and soft-delete actions.
- The visual design uses a monochrome premium style with true black, white, slate/gray, subtle borders, low-radius corners, generous whitespace, and restrained motion.

## Testing Decisions

- Tests should verify external behavior and business outcomes rather than private implementation details.
- The highest-value backend seam is HTTP API behavior through Spring integration tests, because the core risks cross controllers, sessions, security, transactions, persistence, and scheduled expiry.
- Service-level tests are appropriate for reservation and checkout edge cases that need precise transaction and time control.
- Repository-only tests should be used sparingly, mainly for derived inventory queries and soft-delete filtering if those queries become complex.
- Admin auth should be tested through login and protected admin API requests, not by directly testing token internals.
- Cart behavior should be tested through session-aware API calls that verify add, update, remove, reservation totals, and expiry behavior.
- Checkout tests should cover successful checkout, insufficient stock, expired cart item failure, stock deduction, order creation, and reservation finalization.
- Soft-delete tests should verify public APIs hide deleted products/categories while admin APIs can fetch them when requested.
- Image upload tests should cover accepted media types, rejected media types, max-size rejection, URL persistence, and static serving behavior where practical.
- Scheduled expiry should be tested with controllable time or direct invocation of the scheduler seam, verifying expired status and released availability.
- Frontend tests should focus on user-visible flows: storefront browsing, search/filter, add-to-cart, cart edits, checkout error/success states, admin login, inventory display, product management, image upload validation, and soft delete.
- End-to-end smoke tests are useful once both modules exist, covering anonymous shopping and authenticated admin inventory management.
- Because no prior code exists in this repo, there are no existing test patterns to reuse yet; new tests should establish the project conventions.

## Out of Scope

- Customer accounts.
- Customer identity on orders.
- Guest checkout forms.
- Shipping address collection.
- Payment integration.
- Production-grade password storage.
- Database-backed admin users.
- Product variants.
- Promotions and coupons.
- Tax calculation.
- Synchronous image cleanup.
- Unreferenced image cleanup cron.
- Full immutable product snapshots in order items.
- PostgreSQL runtime profile, unless it is trivial to include without distracting from the H2-first implementation.

## Further Notes

- The original requirements were refined through a grilling session and captured in the grilled requirements document.
- The local markdown issue tracker is configured for this repo.
- This PRD is marked `ready-for-agent` and is intended to be implementable without another requirements interview.
- If the implementation is split into smaller issues later, the first issue should establish the monorepo skeleton and backend domain model before frontend polish work begins.
