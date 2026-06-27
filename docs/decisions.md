# Decisions and notes

A plain log of the choices we made while building, so anyone picking this up later knows why.

## 2026-06-27, foundation setup

- **Working name in code is "corewise".** The Java package root is `com.corewise.modernization`.
  This is a placeholder product name. If we settle on a real company name, now is the cheap
  time to change it, while the project is still small. Say the word and I will rename it.

- **Node stays at 24.** The brief asked for Node 22 LTS. This machine already had Node 24,
  which is itself a current LTS line, and it runs Carbon, Vite, React, and TypeScript fine.
  Downgrading could break the other Node projects on this machine, so we kept 24.

- **Java 21 and Maven are portable installs.** We unzipped Temurin JDK 21 and Maven 3.9.16
  into `C:\Users\Dipan\tools` and pointed `JAVA_HOME`, `MAVEN_HOME`, and the user `PATH` at
  them. No admin rights were needed, and nothing else on the machine was touched. The backend
  also ships a Maven wrapper, so builds use a pinned Maven version no matter what.

- **PostgreSQL 18 is already here and running.** We added its `bin` folder to PATH so `psql`
  works in a new terminal.

- **The `carbon-main` folder is reference only.** It is the IBM Carbon source we downloaded.
  Git ignores it. The frontend will use the official `@carbon/react` package as the real
  dependency.


## 2026-06-27, backend and database

- **Spring Boot is pinned to 3.5.16.** Our stack is locked to 3.5.x. Spring Initializr now
  defaults to 4.1.0, so we asked it for the latest 3.5.x on purpose. The Maven wrapper keeps
  the build the same on every machine.

- **Database name is `modernization`.** A plain name that says what it is.

- **The app connects as a limited role, not as the postgres superuser.** We made a role called
  `modernization_app` that owns only its own database. The backend signs in as that role, so a
  mistake or a leak cannot touch the rest of the server. Its password is generated and kept in
  `application-local.properties`, which Git ignores. It is never committed.

- **Local PostgreSQL trusts localhost without a password.** On this machine, Postgres lets
  local connections in without checking a password (this is the "trust" setting). That is handy
  for local work, but it must never be the setup in production. On the real server we use real
  passwords and lock down who can connect.

- **Flyway owns the schema; Hibernate only checks it.** Migrations build and change the tables.
  Hibernate runs in "validate" mode, so it never changes the database on its own. It just warns
  us if the Java side and the tables ever fall out of step.

- **The health check is open; everything else needs a login.** `/health` is a public liveness
  probe so monitoring and Cloudflare can use it. Every other route requires a signed-in caller.
  We turned CSRF off for now because this is a stateless API with no browser session yet, and we
  will set up the real token based sign-in when we build the login flow.

- **Tests do not need a database yet.** The health test is a web-only slice test, so the build
  stays fast and self-contained. We will add full database tests (with a throwaway PostgreSQL)
  when we start writing data code.


## 2026-06-27, frontend foundation

- **Vite is the build tool.** It is the current standard for React and TypeScript and is fast.
  Create React App is no longer maintained, so we did not use it.

- **React 19.** Carbon 1.110 supports it, and it is the current release.

- **I hand wrote the project files instead of running the Vite scaffolder.** The interactive
  scaffolder stalls in this non-interactive setup, so writing the small set of config files by
  hand was the reliable way and gave us exactly the layout we want.

- **Dark by default, switchable, and remembered.** The tool starts in Carbon's g100 dark theme
  because engineers stare at it for hours. The header toggle flips to the g10 light theme, and
  the choice is saved. A tiny script in index.html sets the saved theme before the page paints,
  so there is no flash of the wrong colors.

- **IBM Plex Sans is self-hosted.** We load Carbon's typeface through fontsource so there is no
  outside font CDN to depend on when this runs on our VPS behind Cloudflare.

- **The dev server proxies to the backend.** Calls to /health and /api go to the Spring Boot app
  on port 8080 during development, so the browser does not hit cross-origin problems. The first
  screen uses this to show whether the backend is reachable, in plain words.

- **Carbon's full stylesheet is about 89 KB gzipped.** That is fine for an internal tool. If we
  ever want it smaller, we can import only the component styles we use.


## 2026-06-27, backend engine

- **Sign-in uses JWT with two roles.** Logging in issues an HS256 token (built with Spring
  Security's Nimbus support, no extra library). The roles admin and engineer ride inside the
  token, so checks need no extra database hit. The signing secret and the first admin account
  both come from config, never the code.
- **Files are stored on disk, not in the database.** Uploads stream to disk in small chunks, so
  a huge COBOL file or a big zip never sits in memory. The database keeps the facts about each
  file (name, type, size, line count, checksum, who uploaded) and a key pointing at the bytes.
- **Every AI call goes through one place.** A swappable provider, Claude or GPT picked by config,
  sits behind a wrapper that adds timeouts, retries with backoff, and clear errors. Keys live in
  config only. The app starts fine without a key; AI calls just return a clear "not set up" error.
- **Anything the AI produces is a draft a person approves.** That holds for code explanations,
  suggested dependency links, first-draft Java translations, and AI-drafted test cases. We record
  who approved and when, so a human is always in the loop before anything is trusted.
- **The dependency map mixes parsing and AI.** Parsing finds the obvious CALL and COPY links and
  marks them confirmed. The AI suggests trickier links, marked for a human to confirm or reject.
  Building the map never depends on the AI; the AI part is opt-in.
- **The verification engine that compiles and runs the rewritten Java is future work.** For now we
  store test cases and runs and compare outputs, so the data and the API are ready for it.
- **Flyway owns the schema, now V1 through V7.** Each table carries plain-language comments in the
  database itself. Errors come back in one ApiError shape, in plain words, with no stack traces.
- See `docs/api.md` for what every endpoint does, takes, and returns.


## 2026-06-27, frontend app frame

- **Routing with React Router.** The app is one protected area (the Carbon UI Shell with the side
  nav and the seven sections) plus a public login route. Signed-out people are sent to login.
- **One API layer.** Every backend call goes through `src/api/client.ts`, which attaches the token,
  expects JSON, and turns any failure into a single `ApiClientError` with a plain, ready-to-show
  message. A 401 clears the token and signs the user out everywhere at once.
- **The backend URL comes from config** (`VITE_API_BASE_URL`), empty in dev so the Vite proxy
  forwards `/api` and `/health` to port 8080. Set it for production.
- **The token lives in localStorage** so a refresh keeps you signed in. That suits an internal tool
  behind a login. If we ever need a higher bar, the move is httpOnly cookies plus CSRF, or an
  in-memory token with a refresh token. All token access goes through one session module.
- **Theme is a provider now**, so it covers the login screen too. g100 dark is the default, g10 is
  the light option, and the choice is remembered. Colors come from Carbon tokens, never hardcoded.
- **Shared LoadingState and ErrorState plus a `useApi` hook** set the pattern every data screen
  follows: show a loader, then the data, or a calm message with a way to try again. The dashboard's
  backend check is the first place that uses it.
