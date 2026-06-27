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
