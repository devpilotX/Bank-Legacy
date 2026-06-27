# Runbook

This is the practical guide for running, deploying, and looking after the platform.
It is written so anyone on the team can follow it, even on a rough day. If something
here turns out to be wrong or unclear, fix it in this file so the next person has it
easier.

## What runs where

The platform is three apps plus a database.

| Part      | What it is                          | Local port | In Docker |
|-----------|-------------------------------------|------------|-----------|
| backend   | The API. Java and Spring Boot.      | 8080       | 8080      |
| frontend  | The internal tool our team uses.    | 5173 (dev) | 8081      |
| website   | The small public site.              | 5174 (dev) | 8082      |
| database  | PostgreSQL. Holds all the data.     | 5432       | private   |

The database is never open to the public. Only the backend talks to it.

## Where settings live

We never put passwords or keys in the code. Settings come from one of these:

- For local backend work: `backend/application-local.properties`. Git ignores this
  file. Copy `backend/application-example.properties` to it and fill in real values.
- For Docker and production: a `.env` file next to `docker-compose.yml`, or real
  environment variables on the server. Git ignores `.env`. Copy `.env.example` to
  `.env` and fill it in.

The settings that matter most:

- Database: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Sign-in tokens: `APP_JWT_SECRET` (must be at least 32 characters).
- First admin: `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`. Used once, only when there are
  no users yet, to create the first admin so someone can sign in.
- AI: `APP_AI_PROVIDER` (`openai` or `claude`), plus the key, base URL, and model. For
  OpenRouter, set the provider to `openai`, the base URL to `https://openrouter.ai/api`,
  the key to your OpenRouter key, and the model to something like `openai/gpt-4o-mini`.
- CORS: `APP_CORS_ORIGINS`. The web address the internal tool is served from. Only that
  origin may call the API from a browser.

## Run it locally without Docker

The README has the full steps. Short version, each in its own terminal:

```bash
cd backend && ./mvnw spring-boot:run
cd frontend && npm install && npm run dev
cd website && npm install && npm run dev
```

Check the backend is alive:

```bash
curl http://localhost:8080/health
```

## Run the whole thing with Docker

This is the simplest way to bring up everything together. You need Docker Desktop or
Docker Engine with the Compose plugin.

1. Copy the example settings and fill them in:

   ```bash
   cp .env.example .env
   # open .env and set DB_PASSWORD, APP_JWT_SECRET, the admin login, and the AI key
   ```

2. Bring it all up with one command, from the repo root:

   ```bash
   docker compose up --build
   ```

   That builds the three apps, starts PostgreSQL, waits for the database to be ready,
   then starts the backend, the frontend, and the website. The backend runs its
   database migrations on startup.

3. Open the apps:
   - Internal tool: http://localhost:8081
   - Public site: http://localhost:8082
   - API health: http://localhost:8080/health

To stop it, press Ctrl+C, then:

```bash
docker compose down
```

Your data stays in a Docker volume, so it is still there next time. To wipe the
database too, add `-v` to that command. Be careful: `-v` deletes the data.

## Deploy to a VPS behind Cloudflare

We have not run this yet. These are the steps to do it. The idea: run the same Docker
setup on a small server, and put Cloudflare in front for HTTPS and basic protection.

### 1. Get a server

Get a small Linux VPS (Ubuntu is a safe choice). Two CPUs and 4 GB of memory is plenty
to start. Point your domain's nameservers at Cloudflare (Cloudflare walks you through
this when you add the domain).

### 2. Install Docker on the server

Follow Docker's official install steps for your Linux version. Then check it works:

```bash
docker --version
docker compose version
```

### 3. Get the code and settings onto the server

```bash
git clone <your-repo-url> bank-modernization
cd bank-modernization
cp .env.example .env
# edit .env with real production values: a strong DB_PASSWORD, a long random
# APP_JWT_SECRET, the admin login, the AI key, and APP_CORS_ORIGINS set to your
# real internal web address (for example https://app.yourbank.example)
```

### 4. Start it

```bash
docker compose up --build -d
```

The `-d` runs it in the background. Check the health:

```bash
curl http://localhost:8080/health
docker compose ps
```

### 5. Put Cloudflare in front

The goal is that the public reaches Cloudflare, and Cloudflare reaches your server.
Visitors never hit your server directly.

1. In Cloudflare, add your domain and let it manage DNS.
2. Add DNS records that point at your server's IP address, with the orange cloud turned
   on (proxied). For example:
   - `app.yourbank.example` for the internal tool.
   - `www.yourbank.example` (and the root) for the public site.
3. On the server, run a reverse proxy (Caddy or nginx) that listens on ports 80 and 443
   and sends traffic to the right container: the internal tool to port 8081, the public
   site to port 8082. Caddy is the easy choice because it gets HTTPS certificates on its
   own. A small Caddy setup looks like this:

   ```
   app.yourbank.example {
       reverse_proxy localhost:8081
   }
   www.yourbank.example, yourbank.example {
       reverse_proxy localhost:8082
   }
   ```

   The backend does not need its own public name. The internal tool's container already
   forwards `/api` and `/health` to the backend for you.

### 6. Lock the server down

- Turn on the firewall and allow only what you need: SSH (port 22), HTTP (80), and
  HTTPS (443). Block everything else, including the database port and 8080, 8081, 8082
  from the outside.
- Better still, allow ports 80 and 443 only from Cloudflare's IP ranges, so people
  cannot skip Cloudflare by hitting your IP directly.
- Use SSH keys, not passwords, for logging in.

## Cloudflare settings and why

- Proxy (orange cloud) on: hides your server's real IP and routes traffic through
  Cloudflare. This is what gives you the protection.
- SSL/TLS mode set to Full (strict): traffic is encrypted both from the visitor to
  Cloudflare and from Cloudflare to your server. Use this once Caddy has real
  certificates.
- Always Use HTTPS on: anyone who types `http://` gets sent to `https://`.
- A firewall or WAF rule to keep the internal tool private: the internal tool is for our
  team only, so put it behind Cloudflare Access (email sign-in) or limit it to your
  office or VPN IP addresses. The public site stays open to everyone.
- Rate limiting on the login and API paths: slows down anyone trying many passwords.

## Keep the database private

The compose file gives the database no public port. Only the backend reaches it over
the private Docker network. Do not add a `ports:` entry to the `db` service in
production. If you need to connect for a one-off task, do it from the server itself
through an SSH session, not over the open internet.

## Logs

With Docker, logs are right there:

```bash
docker compose logs -f            # everything, live
docker compose logs -f backend    # just the backend
docker compose logs --since 1h backend
```

The backend prints clear application errors. It never sends stack traces back to the
browser. If you need more detail while chasing a bug, raise the log level by setting an
environment variable like `LOGGING_LEVEL_COM_COREWISE=DEBUG` for the backend, then put
it back when you are done.

## Back up the database

Take a backup before any deploy, and on a schedule (a daily cron job is fine).

```bash
# Make a backup file
docker compose exec -T db pg_dump -U modernization_app modernization > backup-$(date +%F).sql

# Restore from a backup (this overwrites current data, so be sure)
cat backup-2026-01-01.sql | docker compose exec -T db psql -U modernization_app -d modernization
```

Keep backups off the server too (copy them somewhere safe). A backup that lives only on
the same machine does not help if the machine is lost. Test a restore once in a while,
so you know it actually works.

## Roll back to a previous version

If a deploy goes wrong:

1. Find the last good commit:

   ```bash
   git log --oneline
   ```

2. Check it out and rebuild:

   ```bash
   git checkout <good-commit>
   docker compose up --build -d
   ```

3. If the bad version changed the database in a way the old version cannot read, restore
   the backup you took before the deploy (see above). This is why we always back up
   first.

When you have confirmed the old version is healthy, figure out what went wrong on a
branch before trying the deploy again.

## Troubleshooting

- The backend will not start and the log mentions the JWT secret. `APP_JWT_SECRET` is
  missing or shorter than 32 characters. Set a longer one.
- The backend cannot reach the database. Check the `db` container is healthy
  (`docker compose ps`), and that `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` match what
  the `db` service uses.
- AI calls come back with a 502 and "the AI is not set up". The provider, key, base URL,
  or model is missing or wrong. Check the `APP_AI_*` settings. For OpenRouter, the
  provider is `openai`, the base URL is `https://openrouter.ai/api`, and the model is a
  name OpenRouter knows.
- The browser blocks API calls with a CORS error. `APP_CORS_ORIGINS` does not match the
  address the internal tool is actually served from. Set it to that exact origin.
- Uploads get rejected. Files must be a known source type (COBOL, copybook, JCL, and
  similar), and within the size limit. Zip uploads must end in `.zip`.
