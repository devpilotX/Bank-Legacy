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
