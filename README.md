# COBOL Modernization Platform

We help small US banks read and safely modernize their old COBOL systems. AI reads the old
code fast. Our engineers check every result, so nothing breaks. COBOL goes in, Java comes out.

This repo holds three things:

- **backend** is the internal web app's API. Java 21 and Spring Boot. It takes in old code,
  calls a hosted AI to explain it, maps how the parts connect, helps rewrite pieces into
  Java, and checks the new code behaves like the old code. Only our team uses it.
- **frontend** is the screens our engineers use, built with React and IBM Carbon.
- **website** is the small public site, just enough to show banks we are real. It is its own
  React and Carbon app, fully separate from the internal tool.

## Layout

```
backend/    Java 21 + Spring Boot API, PostgreSQL, Flyway migrations
frontend/   The internal tool: React + IBM Carbon (TypeScript)
website/    The public site: a separate React + IBM Carbon app
docs/       Notes and the running list of decisions
```

A few top-level folders are third-party source we downloaded only for reference, and Git
ignores all of them: `carbon-main` (IBM Carbon), `GSAP-master`, `lenis-main`,
`react-three-fiber-master`, and `ui-main`. They are not part of our app. Where we actually
use one of these libraries we pull it in as a normal package instead — for example the
frontend and website depend on the official `@carbon/react` rather than the `carbon-main`
source.

## Get the code

The repository lives at https://github.com/devpilotX/Bank-Legacy. To clone it:

```bash
git clone https://github.com/devpilotX/Bank-Legacy.git
cd Bank-Legacy
```

## What you need

- Java 21 (we use Temurin 21)
- Maven 3.9 or newer (the backend also ships a Maven wrapper, so `./mvnw` works without a
  global Maven)
- Node 22 or newer (this machine runs Node 24)
- PostgreSQL 14 or newer (this machine runs PostgreSQL 18)
- Git

## Run the backend

From Git Bash, in the repo root:

```bash
cd backend

# Make your own local settings file from the example, then fill in real values.
cp application-example.properties application-local.properties
# Open application-local.properties and set your database password (and the url
# and username too if your local setup is different).

./mvnw spring-boot:run
```

Check it is alive:

```bash
curl http://localhost:8080/health
```

You should see something like:

```json
{"status":"ok","version":"0.0.1-SNAPSHOT"}
```

## Run the frontend

From Git Bash, in the repo root:

```bash
cd frontend
npm install
npm run dev
```

Then open the address Vite prints, usually http://localhost:5173. The dev server forwards
`/health` and `/api` calls to the backend on port 8080, so run the backend too to see the
status turn green. There is more detail in `frontend/README.md`.

## Run the website

From Git Bash, in the repo root:

```bash
cd website
npm install
npm run dev
```

Then open the address Vite prints, usually http://localhost:5174. The public site is fully
separate from the internal tool. There is more detail in `website/README.md`.

## A note on secrets

We never commit passwords or API keys. Real settings live in `application-local.properties`,
which Git ignores, or in environment variables. There is an `application-example.properties`
with fake values you can copy from.
