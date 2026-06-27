# Backend API

This is what the backend offers, in plain words. It is an internal API, so every
route except the health check and login needs a signed-in caller.

## Signing in and tokens

You sign in once and get a token. You send that token on every other call in a header:

```
Authorization: Bearer <your token>
```

Roles: there are two, `admin` and `engineer`. Admins manage users and clients.
Engineers do the modernization work. A few routes are admin only; they say so below.

## When something goes wrong

Every error comes back in the same shape, with a plain message and, for bad input,
which fields need fixing. We never send stack traces.

```json
{
  "timestamp": "2026-06-27T20:00:00Z",
  "status": 400,
  "error": "validation_failed",
  "message": "Some fields need fixing.",
  "path": "/api/clients",
  "fieldErrors": [{ "field": "name", "message": "is required" }]
}
```

Common statuses: 400 bad input, 401 not signed in, 403 not allowed, 404 not found,
409 a clash like a duplicate email, 413 upload too big, 502 the AI could not be
reached, 500 something on our side (already logged).

---

## Health

- **GET /health** (public)
  Is the backend up. Returns `{ "status": "ok", "version": "0.0.1-SNAPSHOT" }`.

## Auth

- **POST /api/auth/login** (public)
  Takes `{ "email", "password" }`. Returns `{ "token", "expiresAt", "user" }`.
  Wrong email and wrong password give the same message on purpose.
- **GET /api/auth/me**
  Returns the signed-in user: id, email, full name, role, status.

## Users (admin only)

- **POST /api/admin/users**
  Takes `{ "email", "fullName", "password", "role" }` where role is `admin` or
  `engineer`. Password is at least 8 characters. Returns the new user (never the
  password). 409 if the email is already used.
- **GET /api/admin/users**
  Lists all team members.

## Clients (the banks)

- **GET /api/clients** — lists clients.
- **GET /api/clients/{id}** — one client.
- **POST /api/clients** (admin only)
  Takes `{ "name", "status?", "notes?" }`. Status is `prospect`, `active`, or
  `archived`, and defaults to `active`. Returns the client.
- **PUT /api/clients/{id}** (admin only)
  Same body as create. Updates the client.

## Projects (a body of work for a client)

- **GET /api/projects** — lists projects. Add `?clientId={id}` to filter to one client.
- **GET /api/projects/{id}** — one project.
- **POST /api/projects**
  Takes `{ "clientId", "name", "description?" }`. New projects start at stage
  `intake`. 404 if the client does not exist.
- **PUT /api/projects/{id}**
  Takes `{ "name", "description?" }`. The client never changes.
- **PUT /api/projects/{id}/status**
  Takes `{ "status" }`, one of `intake`, `mapping`, `modernizing`, `verifying`, `done`.

## Source files (the old code)

- **POST /api/projects/{projectId}/files**
  A multipart upload with the file under the form field `file`. Streams the file to
  disk and saves its facts (name, type, size, line count, checksum, who uploaded).
  Returns the file record.
- **POST /api/projects/{projectId}/files/zip**
  A multipart upload (field `file`) of a zip. Streams each file inside it to disk and
  saves a record for each. Returns the list.
- **GET /api/projects/{projectId}/files** — lists the files in a project.
- **GET /api/files/{id}** — one file's facts.
- **GET /api/files/{id}/content** — streams the raw bytes back for download.

## Explanations (AI reads the code)

- **POST /api/files/{fileId}/explanations**
  Takes `{ "startLine?", "endLine?" }`. Leave both out for the whole file. Asks the AI
  to explain the code and saves the answer as a draft. Returns the explanation.
- **GET /api/files/{fileId}/explanations** — lists explanations for a file, newest first.
- **PUT /api/explanations/{id}**
  Takes `{ "content" }`. Saves an engineer's edit. Editing sends it back to draft, so
  it gets approved again.
- **POST /api/explanations/{id}/approve**
  Marks the explanation approved and records who approved it and when.

## Dependency map (how the parts connect)

- **POST /api/projects/{projectId}/dependency-map**
  Builds or refreshes the map. Parsing finds the obvious CALL and COPY links. Add
  `?ai=true` to also let the AI suggest trickier links (off by default, so a build
  never depends on the AI). Returns `{ "nodes", "edges" }`.
- **GET /api/projects/{projectId}/dependency-map** — fetches the map.
- **POST /api/dependency-links/{edgeId}/confirm** — confirms an AI-suggested link.
- **POST /api/dependency-links/{edgeId}/reject** — rejects an AI-suggested link.
  (Parsed links come straight from the code, so they cannot be confirmed or rejected.)

## Work units (rewriting code into Java)

- **POST /api/projects/{projectId}/work-units**
  Takes `{ "title", "originalCode", "sourceFileId?", "ownerId?", "notes?" }`. New units
  start at `todo`. If you do not name an owner, it is you.
- **GET /api/projects/{projectId}/work-units** — lists units, newest first.
- **GET /api/work-units/{id}** — one unit, with all three versions of the code.
- **POST /api/work-units/{id}/ai-draft**
  Asks the AI for a first-draft Java translation of the original code, saved as the AI
  draft. Moves a `todo` unit to `in_progress`.
- **PUT /api/work-units/{id}/java**
  Takes `{ "java" }`. Saves the engineer's reviewed Java, kept apart from the AI draft.
- **PUT /api/work-units/{id}/status**
  Takes `{ "status" }`, one of `todo`, `in_progress`, `in_review`, `done`.
- **POST /api/work-units/{id}/approve**
  Marks the unit done and records who approved it. The engineer's Java must be saved
  first; we never approve an empty rewrite.

## Verification (does the new code behave like the old)

- **POST /api/work-units/{workUnitId}/verification-cases**
  Takes `{ "name", "input?", "expectedOutput" }`. Adds a confirmed test case.
- **POST /api/work-units/{workUnitId}/verification-cases/ai-draft**
  Asks the AI to draft cases from the old code. They are saved as suggestions until a
  person confirms them.
- **GET /api/work-units/{workUnitId}/verification-cases** — lists the cases.
- **POST /api/verification-cases/{caseId}/confirm** — confirms a suggested case.
- **POST /api/work-units/{workUnitId}/verify**
  Takes `{ "results": [ { "caseId", "actualOutput" } ] }`, the output the new Java
  produced for each case. Compares each against the expected output, records a run, and
  returns a summary with counts and the latest run per case.
  (The harness that compiles and runs the Java to produce those outputs is a later step.)
- **GET /api/work-units/{workUnitId}/verification** — the latest results and counts.
