# Mini-Mart Grilled Requirements

## Objective

Build a runnable, production-oriented first version of an e-commerce application called **Mini-Mart**.

The application should be delivered as a multi-module Gradle monorepo with:

- `backend/`: Spring Boot Java API
- `frontend/`: React + Tailwind CSS web app

The first version should support an anonymous public storefront, a protected admin console, session-backed carts, inventory reservation, image uploads, soft deletion, and demo seed data.

## Tech Stack

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- H2 database for the initial version
- Gradle multi-module project

The backend should be structured so PostgreSQL can be introduced later with minimal changes.

### Frontend

- React
- Tailwind CSS
- Modern build setup, preferably Vite
- Clean API client layer for backend communication

## UI/UX Direction

The frontend should use a premium, ultra-minimal visual style inspired by systems like Linear and Vercel.

### Visual System

- Monochromatic palette using true black, white, slate, and gray.
- Subtle borders and backgrounds.
- Muted status colors only where necessary.
- Generous whitespace.
- Thin borders.
- Low-radius corners such as `rounded-md` or `rounded-lg`.
- Smooth micro-interactions using transitions like `transition-all duration-200 ease-in-out`.

### Main Screens

The frontend should provide a minimal viewport switcher or routing structure between:

- Public Storefront
- Admin Console

The admin console must not be accessible without admin login.

## Authentication And Authorization

### Public Storefront

The storefront is anonymous for v1.

No customer login, customer identity, payment integration, or shipping details are required in the first version.

The schema should still leave room for future customer accounts and user-linked orders.

### Admin Console

Admin access must be separated by URL, for example `/admin`.

Admin authentication is required in v1, but should remain simple:

- Admin credentials are stored in `backend/src/main/resources/admin-credentials.json`.
- Credentials are plaintext for v1.
- Credentials are not stored in the database.
- Backend validates admin login against the JSON file.
- Successful login returns a simple signed JWT.
- Admin API calls require the JWT.

## Database And Domain Model

### Product

Fields:

- `id`
- `name`
- `description`
- `price`
- `stockQuantity`
- `imageUrl`
- `status`
- relationship to `Category`

Rules:

- Products are soft-deleted.
- Public APIs hide deleted products.
- Admin APIs return active products by default.
- Admin APIs can fetch deleted products when explicitly requested.
- `stockQuantity` represents physical stock.
- `availableQuantity` is derived as `stockQuantity - activeReservedQuantity`.

### Category

Fields:

- `id`
- `name`
- `status`

Rules:

- Categories are soft-deleted.
- Deleting a category sets status to `DELETED`.
- Public APIs hide deleted categories.
- Admin APIs return active categories by default.
- Admin APIs can fetch deleted categories when explicitly requested.

Suggested statuses:

- `ACTIVE`
- `DELETED`

### Cart

The authoritative cart must live on the backend, tied to the anonymous user's HTTP session.

Rules:

- The frontend should not use `localStorage` as the source of truth for cart contents.
- The frontend may use `localStorage` only for lightweight non-authoritative UI convenience if needed.
- Cart inactivity timeout is 30 minutes.
- Session/cart expiry releases reserved stock.
- Expired carts or reservations should be marked `EXPIRED`.

Suggested cart statuses:

- `ACTIVE`
- `CHECKED_OUT`
- `EXPIRED`

### Cart Item / Reservation

Stock reservation is required.

Rules:

- Adding an item to cart reserves stock on the backend.
- Removing an item from cart releases its reserved stock.
- Updating quantity adjusts the reservation.
- Reservation must fail if there is not enough available stock.
- A scheduled backend job expires inactive carts/reservations.
- Expired reservations are marked `EXPIRED`.

The backend should expose reserved and available inventory values where needed.

### Order

Fields:

- `id`
- `orderDate`
- `totalAmount`
- `status`

Valid statuses:

- `PLACED`
- `CANCELLED`
- `COMPLETED`

Rules:

- Checkout uses the current backend session cart only.
- Checkout must not accept arbitrary product IDs and quantities from the client.
- Checkout fails if any session cart item or reservation has expired.
- Checkout converts valid active reservations into an order.
- Checkout finalizes the stock deduction.
- Order/customer identity can be added later.

### Order Item

Fields:

- `id`
- `quantity`
- `price`
- relationship to `Product`
- relationship to `Order`

Keep order snapshots simple for v1. Full immutable product snapshots can be added later if needed.

## Inventory Semantics

Use this model:

- `stockQuantity`: total physical stock.
- `reservedQuantity`: sum of active reserved quantities.
- `availableQuantity`: `stockQuantity - reservedQuantity`.

Admin dashboard must show:

- `stockQuantity`
- `reservedQuantity`
- `availableQuantity`

Low-stock warnings should be muted and simple, using a fixed threshold for v1.

## Backend API Requirements

### Public APIs

#### Products

- `GET /api/products`

Supports:

- Search filtering
- Category filtering
- Only active products
- Derived `availableQuantity`

#### Categories

- `GET /api/categories`

Returns only active categories.

#### Cart

Suggested endpoints:

- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{itemId}`
- `DELETE /api/cart/items/{itemId}`

Rules:

- Cart APIs use the current HTTP session.
- Add/update operations reserve stock.
- Delete operations release stock.
- Responses should include current cart totals and availability-related errors.

#### Orders

- `POST /api/orders`

Rules:

- Uses the current session cart only.
- Runs inside a database transaction.
- Fails with `400 Bad Request` if any item is unavailable or expired.
- Deducts stock safely.
- Creates `Order` and `OrderItem` records.

### Admin APIs

Admin APIs require a valid admin JWT.

#### Auth

Suggested endpoint:

- `POST /api/admin/auth/login`

Request:

- `username`
- `password`

Response:

- Signed JWT

#### Products

Required endpoints:

- `POST /api/admin/products`
- `DELETE /api/admin/products/{id}`
- `PUT /api/admin/products/{id}/refill`

Additional useful endpoints:

- `GET /api/admin/products`
- `PUT /api/admin/products/{id}`

Rules:

- Delete is soft delete.
- Product list returns active products by default.
- Admin can request deleted products when needed.
- Refill increases physical `stockQuantity`.

#### Categories

Required endpoints:

- `POST /api/admin/categories`
- `DELETE /api/admin/categories/{id}`

Additional useful endpoints:

- `GET /api/admin/categories`
- `PUT /api/admin/categories/{id}`

Rules:

- Delete is soft delete.
- Category list returns active categories by default.
- Admin can request deleted categories when needed.

#### Image Uploads

Product images are uploaded by admin.

Requirements:

- Store uploaded files under `backend/uploads/products/`.
- Serve uploaded files from `/uploads/products/...`.
- Store the retrievable URL/path in the product `imageUrl` field.
- Accept JPEG, PNG, and WebP.
- Reject files larger than 5 MB.
- Do not delete image files synchronously when a product is deleted or image is replaced.
- Future cleanup can be handled by a cron that removes unreferenced files.

Suggested endpoint:

- `POST /api/admin/products/{id}/image`

## Scheduled Jobs

Implement a scheduled backend job that:

- Finds inactive carts/reservations older than 30 minutes.
- Marks them `EXPIRED`.
- Releases reserved inventory by excluding expired reservations from active reservation totals.

## Seed Data

The application should include demo data for local development.

Include a few:

- Categories
- Products
- Product images or placeholder image URLs

Seed data should be compatible with H2.

## Frontend Requirements

### Storefront

The storefront should include:

- Header with `Mini-Mart` branding.
- Inline search bar.
- Category filtering.
- Minimal cart button with item count badge.
- Product grid with clean cards.
- Uniform image aspect ratio.
- Clear price display.
- Add-to-cart button.
- Cart panel sliding from the right over a blurred backdrop.
- Cart item quantity controls.
- Remove item action.
- Checkout button labeled clearly, such as `Confirm Purchase Order`.

Cart UI must call backend cart APIs. It should not treat local state as authoritative.

### Admin Console

Admin console should include:

- Admin login screen.
- Inventory dashboard.
- Product table.
- Category management.
- Product creation/editing form.
- Image upload control.
- Quick refill action, such as `Refill (+50)`.
- Soft-delete actions.

The inventory dashboard must show:

- `stockQuantity`
- `reservedQuantity`
- `availableQuantity`
- Muted low-stock warning

## Error Handling

The API should return clear `400 Bad Request` errors for:

- Insufficient available stock
- Expired cart item during checkout
- Invalid cart item quantity
- Invalid image type
- Image exceeding 5 MB

Unauthorized admin API access should return `401 Unauthorized`.

Forbidden or invalid admin token access should return `403 Forbidden` or `401 Unauthorized`, depending on the security implementation.

## Non-Goals For V1

Do not implement these in the first version:

- Customer accounts
- Customer identity on orders
- Guest checkout forms
- Shipping address collection
- Payment integration
- PostgreSQL runtime profile, unless trivial to include
- Image cleanup cron
- Product variant support
- Promotions, coupons, or tax calculations
- Real production password storage

## Deliverable

Deliver a complete runnable monorepo codebase.

The codebase should include:

- Gradle multi-module setup
- Spring Boot backend
- React + Tailwind frontend
- H2 configuration
- Admin credentials JSON
- JWT-based admin auth skeleton
- JPA entities and repositories
- Service layer with transactional checkout and reservation logic
- Controllers for public and admin APIs
- Scheduled reservation expiry job
- Static serving for uploaded images
- Seed demo data
- Storefront UI
- Admin console UI
- Clear local run instructions
