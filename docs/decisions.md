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


## 2026-06-27, public website

- **The public site is its own app in `website/`.** It shares no code with the internal tool and
  never calls the internal backend, so nothing internal can leak through it. It runs on its own
  port (5174) in development.
- **Light theme, always.** It uses Carbon's White theme, with no theme switch, since it is public
  facing. Colors come from Carbon tokens.
- **Hand-made wordmark.** The logo is a small original SVG: two blocks and a line, the old core
  carried across to the new one. It uses currentColor, so there are no image files and no clip art.
- **The contact form keeps messages in the browser until a real handler is set.** Point
  VITE_CONTACT_ENDPOINT at an email service or a small handler, and set VITE_CONTACT_EMAIL, before
  launch. We chose this so the site stays a simple static app with nothing wired to the internal tool.
- **Five short pages, plain writing.** Home, What we do, Who we help, Why us, Contact. The words are
  the point here, so they are written plainly and honestly, in the same voice as everywhere else.



## 2026-06-29, public website rebuilt on a new stack

- **The public site moved off Carbon to its own modern stack.** It is still its own app in
  `website/`, separate from the internal tool, and still shares no code with it. The internal
  tool stays on React and Carbon and was not touched. We changed only the public site, and only
  its look and its tech, not its plain-spoken words.
- **The stack is React and TypeScript on Vite, Tailwind CSS, and shadcn/ui**, with GSAP and
  ScrollTrigger for reveals, Lenis for smooth scrolling, and React Three Fiber with drei for one
  small 3D moment in the hero. We installed current versions with npm and kept the four
  downloaded library folders as reference only. Git ignores them.
- **shadcn was set up by hand.** Its CLI is interactive and stalls in this non-interactive shell,
  the same problem we had with the Vite scaffolder. So we wrote `components.json`, the `cn`
  helper, and the component files ourselves. The result is what the CLI would have produced.
- **One page, six sections.** Hero, the problem, what we do, who we help, why us, and contact, in
  that order, with a fixed top nav that jumps to each. The nav links are built from one shared
  list, so they cannot drift out of sync with the sections.
- **Calm colors and type.** Light theme only. Slate grays, one deep blue as the single brand
  color used lightly, and plenty of white space. IBM Plex Sans for text and IBM Plex Mono for the
  small code accents, both self-hosted, so there is no outside font CDN. Colors are CSS variables.
- **The 3D is the one strong moment, kept tasteful.** It echoes the logo: an outlined old block
  giving way to a solid modern one, moving slowly and quietly. It loads in its own chunk, and only
  on a wide screen, on a capable device, with reduced motion off. The first paint never waits on it.
- **One static fallback does double duty.** The same calm panel, COBOL becoming Java, shows on
  phones, low-power devices, and for reduced motion, and it is also what shows while the 3D chunk
  loads. The page looks complete even if the 3D never runs.
- **Reduced motion is fully respected.** One switch turns off smooth scroll, the reveals, and the
  3D together, and shows the static version.
- **The contact form stays config-driven.** It posts to `VITE_CONTACT_ENDPOINT` when that is set,
  and otherwise keeps the message in the browser and logs it, so the flow works while we build. Set
  `VITE_CONTACT_ENDPOINT` and `VITE_CONTACT_EMAIL` before launch. Nothing is hardcoded, and the
  form never talks to the internal backend.
- **The logo is a hand-drawn SVG.** Two slightly offset rounded squares in the brand blue, an old
  block becoming a new one. It is monochrome and reads at a small size. "Corewise" is still a
  placeholder name, and the README says where to change it.
- **Bundle.** The main bundle is about 126 KB gzipped: React, GSAP, ScrollTrigger, Lenis, and the
  app. The 3D and three.js are split into a separate chunk that loads only when shown. The build
  type-checks and lints before it bundles.



## 2026-06-29, the real verification engine

- **Verification now actually runs both sides.** It was a shell before: a person typed the
  expected output and we compared strings. Now the engine compiles and runs the original
  COBOL to get the true expected output, compiles and runs the approved Java the same way,
  and compares the two. This is the core of what we sell, so it had to become real.
- **GnuCOBOL runs the old COBOL.** We installed a known Windows build of GnuCOBOL 3.2 that
  bundles its own C compiler, with no admin rights, and checked it against the checksum the
  Chocolatey package publishes. The backend points at it with `app.verify.gnucobol-home`.
  The Java side uses the JDK the backend already runs on.
- **Every run is sandboxed.** A separate process, a throwaway temp folder, a hard timeout
  that kills a program that runs too long, and the folder is deleted afterward. This is
  process level isolation, not a container yet. A comment in the code says container
  isolation is the production step. We do not block network at the OS level on this
  machine, which is an honest limit.
- **A case is just inputs now.** A person no longer types the expected output. The AI can
  still draft input cases, which a person confirms. The COBOL gives the golden answer.
- **The compare options are visible.** Trim trailing spaces, and a small numeric tolerance,
  both shown with the result. A formatting only difference is flagged apart from a real
  behavior difference, so the two are never confused.
- **We store the whole picture for each run.** The inputs, the COBOL output, the Java
  output, pass or fail, the diff, the settings used, the time, and who ran it. Migration V8
  adds the columns.
- **We bumped the AI model to a strong Claude, Sonnet 4.5.** A weak model wrote Java that
  often did not compile, which makes verification pointless. The model is still read from
  config and stays swappable.
- **Proven end to end.** A real interest program: the engine ran the COBOL and a correct
  Java and they matched, then a buggy Java was caught with a clear diff. This is checked in
  the build and was also run through the live API.



## 2026-07-23, docs checkpoint

- **Repo checkpoint.** Working tree was clean and level with `origin/main`, so this is a
  small housekeeping note to mark where the project stands after the verification-engine
  hardening pass: the engine runs both the old COBOL and the new Java, compares them with
  visible options, and stores the full picture of every run. No code behavior changed here.
