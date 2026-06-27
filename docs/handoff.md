# Handoff

This note covers the hardening, testing, and packaging pass. It is here so the next
person can pick up without digging through commits.

## Please rotate the AI key first

The OpenRouter API key was shared in chat while we set this up, so treat it as exposed.
Rotate it in the OpenRouter dashboard and put the new one only in
`backend/application-local.properties` (local) or the `.env` used by Docker. Both files
are gitignored. The key is not committed anywhere in this repo (we checked).

## What was done

- AI works end to end. The provider is chosen from settings, and a real explanation
  comes back from OpenRouter (model `openai/gpt-4o-mini`). For local work the settings
  live in `backend/application-local.properties`. For Docker and production they come
  from environment variables.
- Security pass on the backend:
  - CORS is limited to the origins we set (`app.security.cors.allowed-origins`), not open
    to everyone.
  - Security headers are sent: HSTS, no-sniff, frame deny, a strict referrer policy, and
    a tight content security policy (the API serves only JSON).
  - Uploads are checked. Single files must be a known source type (COBOL, copybook, JCL,
    and similar). Zip uploads must end in `.zip`. Size limits were already in place.
  - Errors return clean JSON. Stack traces never reach the browser.
  - Passwords are hashed (BCrypt). Tokens are signed. Admin-only endpoints are guarded.
- Tests:
  - Backend: 32 tests. Unit tests for the AI wrapper (including timeout and retry), the
    COBOL mapping, the modernization work units, and the verification compare. Web-layer
    tests for login validation, wrong credentials, and role checks (an engineer is
    blocked from admin endpoints). The build fails if any test fails.
  - Frontend: tests for the API client (token attach, the 401 sign-out signal, error
    messages, and a server-unreachable case) and the data hook (loading, then data, or a
    calm error). These run as part of `npm run build`.
- Quality:
  - Java: Spotless tidies imports and whitespace, checked during `verify`. We kept it
    light on purpose so it does not reflow the hand-written code.
  - Frontend and website: ESLint and Prettier. `npm run build` runs lint and fails on
    errors.
- Docker: there are Dockerfiles for the backend, the frontend, and the website, plus a
  `docker-compose.yml` that brings up PostgreSQL, the backend, the frontend, and the
  website with one command. The database is not exposed to the host or the internet.
- Docs: `docs/runbook.md` covers local run, running with Docker, deploying to a VPS
  behind Cloudflare, rollback, backups, logging, and troubleshooting.

## Decisions worth knowing

- AI settings are not listed in `application.properties`. They come from the local file
  in development, or from environment variables in production. This was on purpose: when
  the same key was listed in `application.properties` with a default, the default kept
  winning over the local file. Leaving it out makes whatever you set the thing that runs.
- The internal tool's frontend container forwards `/api` and `/health` to the backend, so
  the browser only ever talks to one origin.
- We use bearer tokens, not cookies, so CORS does not allow credentialed requests and
  there is no CSRF token to manage.

## What was not done, and why

- Docker is not installed on the machine we worked on, so the Dockerfiles and compose
  file were written and read closely but not built or run here. Build them once on a
  machine that has Docker before you rely on them in production.
- For the same reason, there are no database-backed integration tests yet. The migrations
  are PostgreSQL-specific, so those tests need a real PostgreSQL, which is easiest with
  Testcontainers on a machine that has Docker. The web-layer tests we added do not need a
  database and run everywhere.
- The actual VPS deploy and the Cloudflare setup were not performed. They are written out
  step by step in the runbook so they can be followed when it is time.

## How to check it yourself

```bash
# Backend: runs the tests and the lint gate
cd backend && ./mvnw clean verify

# Frontend: typecheck, lint, tests, and bundle
cd frontend && npm install && npm run build

# Website: typecheck, lint, and bundle
cd website && npm install && npm run build
```
