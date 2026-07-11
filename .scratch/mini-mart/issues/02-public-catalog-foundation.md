Status: resolved

# Public catalog foundation

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add the first demoable storefront catalog path. The backend should expose active categories and products from H2 seed data, and the frontend should let shoppers browse, search, and filter seeded products in the Mini-Mart visual style.

This slice should not include cart, checkout, admin authentication, or catalog mutation.

## Acceptance criteria

- [x] H2 seed data creates a few active categories and products.
- [x] Product and category records support active and deleted statuses.
- [x] Public product APIs return active products with category data and derived availability fields where available.
- [x] Public category APIs return active categories.
- [x] Product search and category filtering work through the public API.
- [x] Deleted products and categories are hidden from public APIs.
- [x] The storefront renders Mini-Mart branding, search, category filtering, a product grid, product images, prices, and add-to-cart affordances that can remain disabled or non-functional until the cart slice.
- [x] The UI follows the monochrome premium design direction with restrained spacing, borders, radius, and transitions.
- [x] Automated tests cover public catalog listing, search/filter behavior, seeded data availability, and hiding deleted catalog records.

## Blocked by

- .scratch/mini-mart/issues/01-runnable-monorepo-skeleton.md

## Comments

Implemented Jul 11 2026. Backend: Category/Product entities with EntityStatus enum, repositories with soft-delete filtering, ProductService, PublicCatalogController (GET /api/products?search=&categoryId=, GET /api/categories), DataInitializer with 3 categories and 9 products. Frontend: App.tsx storefront with branding header, search input, category filter buttons, product grid using ProductCard component with image, price, category badge, disabled add-to-cart button. Tests: 7 backend integration tests covering listing, search, category filter, derived availability, and deleted record hiding. 3 frontend tests covering product loading, search input, and category filtering.
