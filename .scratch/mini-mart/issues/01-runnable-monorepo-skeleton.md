Status: resolved

# Runnable monorepo skeleton

## Parent

.scratch/mini-mart/PRD.md

## What to build

Create the runnable Mini-Mart project spine. A developer should be able to start a Spring Boot backend and a React/Tailwind frontend from a multi-module Gradle monorepo, even before the core commerce features are filled in.

This slice is intentionally narrow: it creates the shared structure that every other vertical slice needs, without owning catalog, cart, checkout, admin management, or uploads.

## Acceptance criteria

- [x] The repo is a runnable multi-module Gradle project with separate backend and frontend modules.
- [x] The backend module starts locally as a Spring Boot application.
- [x] The frontend module starts locally as a React and Tailwind CSS application.
- [x] The backend has H2 configured for local development.
- [x] The frontend can make a simple health or placeholder API call to the backend.
- [x] Basic local run instructions explain how to start both modules.
- [x] The app has enough smoke coverage or verification commands to prove both modules start.

## Blocked by

None - can start immediately.
