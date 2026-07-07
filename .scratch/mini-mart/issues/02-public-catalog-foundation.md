Status: ready-for-agent

# Public catalog foundation

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add the first demoable storefront catalog path. The backend should expose active categories and products from H2 seed data, and the frontend should let shoppers browse, search, and filter seeded products in the Mini-Mart visual style.

This slice should not include cart, checkout, admin authentication, or catalog mutation.

## Acceptance criteria

- [ ] H2 seed data creates a few active categories and products.
- [ ] Product and category records support active and deleted statuses.
- [ ] Public product APIs return active products with category data and derived availability fields where available.
- [ ] Public category APIs return active categories.
- [ ] Product search and category filtering work through the public API.
- [ ] Deleted products and categories are hidden from public APIs.
- [ ] The storefront renders Mini-Mart branding, search, category filtering, a product grid, product images, prices, and add-to-cart affordances that can remain disabled or non-functional until the cart slice.
- [ ] The UI follows the monochrome premium design direction with restrained spacing, borders, radius, and transitions.
- [ ] Automated tests cover public catalog listing, search/filter behavior, seeded data availability, and hiding deleted catalog records.

## Blocked by

- .scratch/mini-mart/issues/01-runnable-monorepo-skeleton.md
