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
