Status: resolved

# Admin login and protected console shell

## Parent

.scratch/mini-mart/PRD.md

## What to build

Add the first protected admin path. Admin credentials should be loaded from configuration JSON, validated by the backend, and exchanged for a signed JWT. The frontend should expose an admin route with a login screen and an authenticated console shell. Admin API routes should reject unauthenticated requests.

This slice proves the admin security model end to end without requiring public catalog APIs or admin product/category management.

## Acceptance criteria

- [x] Admin credentials are loaded from a JSON resource and are not stored in the database.
- [x] Admin login validates username/password against the JSON credentials.
- [x] Successful admin login returns a signed JWT.
- [x] Failed admin login returns an appropriate unauthorized error without exposing credential details.
- [x] Admin API routes require a valid admin JWT.
- [x] Requests without a token or with an invalid token cannot access protected admin APIs.
- [x] The frontend has a separated admin route with a login screen.
- [x] After successful login, the frontend shows an authenticated admin console shell.
- [x] The frontend stores and sends the admin token in a simple v1-appropriate way.
- [x] Automated tests cover successful login, failed login, protected admin API access, and rejected unauthenticated access.

## Blocked by

- .scratch/mini-mart/issues/01-runnable-monorepo-skeleton.md

## Comments

Implemented Jul 11 2026. Backend: admin-credentials.json resource, JwtUtil (JJWT-based HMAC-SHA256 tokens), AdminAuthService, JwtAuthFilter, SecurityConfig (stateless, /api/admin/** protected), AdminAuthController (POST /api/admin/login, GET /api/admin/me). Frontend: AdminLogin component, AdminConsole component, route-based navigation (/admin), localStorage token storage. Tests: 5 backend integration tests, 3 frontend tests for admin routes.
