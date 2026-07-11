# Mini-Mart

Mini-Mart is a runnable commerce monorepo with a Spring Boot backend and a React/Tailwind frontend.

## Prerequisites

- Java 21
- Gradle 8.7 or compatible
- Node.js 18+
- npm 9+

## Run Backend

```sh
GRADLE_USER_HOME="$PWD/.gradle-home" gradle :backend:bootRun
```

The backend starts on `http://localhost:8080`.

Health check:

```sh
curl http://localhost:8080/api/health
```

Expected response:

```json
{"service":"mini-mart-backend","status":"ok"}
```

## Run Frontend

```sh
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173` and proxies `/api` calls to the backend.

## Default Credentials

- **Admin login:** `admin` / `admin123`
- Login at `http://localhost:5173/admin`

## Seed Data

On startup, the backend seeds:
- **8 categories** (Beverages, Snacks, Dairy, Bakery, Produce, Meat, Frozen, Pantry)
- **24 products** with realistic names, prices, stock, and picsum image URLs

Product images use the `/uploads/` path. The upload directory defaults to `uploads/products` and is configurable via `app.upload.path` in `application.yml`.

To reset seed data, restart the backend.

## Full Local Run

1. Start the backend:
   ```sh
   GRADLE_USER_HOME="$PWD/.gradle-home" gradle :backend:bootRun
   ```

2. Start the frontend (separate terminal):
   ```sh
   cd frontend
   npm install
   npm run dev
   ```

3. Open `http://localhost:5173` in a browser.

### Shopper flow
- Browse products on the storefront
- Use the search bar to filter by name/description
- Click category buttons to filter
- Click "Add to cart" on any product
- Open the cart panel (top-right "Cart" link)
- Adjust quantities, remove items, or proceed to checkout
- On checkout, an order is placed and stock is deducted

### Admin flow
- Navigate to `http://localhost:5173/admin`
- Log in with `admin` / `admin123`
- **Products tab:** Create, edit, soft-delete, refill stock, upload images
- **Categories tab:** Create, edit, soft-delete categories
- Use "Show deleted" to view soft-deleted products/categories
- Product images must be JPEG, PNG, or WebP, max 5 MB

## Verify

Backend tests:

```sh
GRADLE_USER_HOME="$PWD/.gradle-home" gradle :backend:test
```

Frontend tests:

```sh
cd frontend
npm test -- --run
```

All checks through Gradle:

```sh
GRADLE_USER_HOME="$PWD/.gradle-home" gradle checkAll
```
