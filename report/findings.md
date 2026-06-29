# Test findings (the honest version)

This is the raw record of what I actually ran and found on 2026-06-29. The Word report
in this folder is written from these notes. Nothing here is hopeful guessing. Where I
could not check something for real, I say so.

## What I did

- Ran every existing automated test.
- Started PostgreSQL and the backend for real and hit the health check.
- Took a small but realistic COBOL program I wrote (a monthly savings interest run, with
  a calculation, a record, a COPY, and a CALL) and pushed it all the way through the real
  running API: upload, AI explain, dependency map, Java rewrite and approve, verification.
- Read the verification code to see what it really does.
- Built the internal tool and the public site, and checked the screens against the APIs
  behind them.

## The one thing I could not do

I cannot open a browser and click around with my own eyes from here. So for the screens I
confirmed two things instead: the apps build with no errors, and the APIs each screen calls
return real data (I called those APIs myself). I also read the screen code. I did not watch
the 3D hero animate, feel the smooth scroll, or eyeball the phone layout. I am calling that
out so nobody thinks I saw something I did not.

## Automated tests (all green)

- Backend: 32 tests, 0 failures, 0 errors. Maven build succeeded. Code style check (Spotless)
  clean on 106 files.
- Internal tool (frontend): 8 tests, 0 failures.
- Public site (website): 5 tests, 0 failures.
- Total: 45 tests, all passing.

## Backend and database

- The backend booted in about 6 seconds.
- It connected to PostgreSQL 18.
- Flyway validated 7 database migrations and the schema was already up to date.
- The health check returned: {"status":"ok","version":"0.0.1-SNAPSHOT"}.
- No errors in the startup log. (There is a harmless warning that Flyway has not been tested
  on Postgres 18 yet. It still worked.)

## The real end to end run

I logged in as the admin, made a client and a project, and uploaded three files: the main
program INTCALC.CBL, the copybook ACCTREC.CPY, and the subprogram INTPOST.CBL.

- Upload: worked. It detected the language (cobol vs copybook), counted lines, and saved a
  checksum. Files got ids 5, 6, 7.
- AI explain: worked, and the explanation was genuinely good, not filler. On the cheap model
  (gpt-4o-mini) it correctly named the ACCTMAST file, the 0.25 percent monthly rate, the
  skip-closed-accounts logic, the interest calculation, and the call to INTPOST. It even
  flagged that the program has no error handling for a bad file read. That is real
  understanding of my specific code.
- Dependency map: worked, and it found the real links by parsing the code. It found
  INTCALC calls INTPOST, and INTCALC includes (COPY) ACCTREC. Both were marked as parsed and
  confirmed. One honest nuance: the referenced name (INTPOST) shows up as a separate node
  from the uploaded file (INTPOST.CBL). It finds the reference but does not yet tie a
  referenced name back to the uploaded source file.
- Modernization: worked. I created a work unit, asked the AI for a Java draft, saved a human
  version, and approved it. Approval recorded who approved it and when. The AI draft was a
  reasonable first sketch (right class, rate, totals, the skip-closed branch, rounding) but
  it left TODO notes where it needed the record layout and the INTPOST call, and it wrapped
  the code in markdown fences. So it is a starting point a person has to finish, not done Java.
- Verification: I added a case with an expected output, then ran it twice. This is the
  important part, see below.

## The verification truth (the heart of the business)

Plain answer: verification does NOT compile or run the new Java. It compares an output that
the caller hands it against the expected output, and records a pass or fail.

How I proved it three ways:

1. Live test. I made a case with expected output "interest=250". When I sent an actual output
   of "interest=250" it passed. When I sent "interest=999" it failed with "Line 1 did not
   match. Expected interest=250 but got interest=999." The result depended only on the string
   I sent, not on running any Java. The Java I approved was never executed.
2. The API doc says it plainly: the verify endpoint takes "the output the new Java produced"
   as input, and "the harness that compiles and runs the Java to produce those outputs is a
   later step."
3. The code. In VerificationService.runForUnit it calls
   VerificationComparator.evaluate(expectedOutput, suppliedActualOutput) and saves the result.
   The rewritten Java is never referenced. There is no javac, no process run, nothing that
   executes code.

The comparison itself is real and decent (line by line, with a clear reason on a failure).
The case management is real (add by hand, confirm, or let the AI draft cases that a person
confirms). What is missing is the engine that actually runs the new Java (and the old code)
to produce the outputs. That is the core promise, and today a human supplies the outputs.

## AI quality on the cheap model (gpt-4o-mini via OpenRouter)

- Explaining code: genuinely good and specific. This is the strongest AI feature right now.
- Translating to Java: a reasonable first draft with honest gaps (TODOs, markdown fences).
  Useful as a head start, not as finished code. A real engineer has to take it the rest of
  the way, which matches how the product is meant to work.

## The internal tool (the team's app)

It is a real, built app, not an empty shell. It is React with IBM Carbon. It has a full,
typed API layer for every backend feature, a sign-in flow, a theme, and a proper app shell.
The screens are all implemented (not stubs): a dashboard that loads projects, clients, files,
and work units and shows totals; clients; projects; and a project workspace with tabs for the
code, the dependency map, the modernization workspace, verification, and a report. Every
screen is wired to the same APIs I exercised by hand. The build passes (type check, lint, 8
tests, bundle). The verification screen is honest about itself: it has a box labeled "What
the new Java produced" that the engineer fills in, which matches the backend reality that
nothing runs the Java.

I could not click it in a browser, so this is based on the clean build, reading the screen
code, and calling the same APIs the screens call.

## The public site

It builds with no errors and the tests pass. The structure is all there in the code: one
calm 3D moment in the hero that loads on its own chunk only on capable wide screens, a clean
static fallback otherwise, smooth scrolling, gentle section reveals, a contact form with
validation and a hidden honeypot, and full respect for reduced motion. The contact form does
not send anywhere real until VITE_CONTACT_ENDPOINT is set; by default it keeps messages in
the browser. I could not view it at phone and desktop widths with my own eyes.

## The four buckets

### Fully works (I ran it and saw it work)
- Sign in and roles.
- Create and list clients and projects.
- Upload old code, with language detection, line count, and checksum.
- AI explanation of code, and it is high quality.
- Dependency map by parsing: it finds the real CALL and COPY links.
- Modernization flow: create a unit, get an AI Java draft, save a human version, approve with
  a record of who and when.
- Verification case management and the output comparison.
- Backend boot, database, migrations, health check.
- All 45 automated tests.
- Both front ends build cleanly and the internal tool screens are wired to working APIs.

### Partly works (works, but with a real limit)
- Verification: it compares outputs and stores results, but it does not run the Java. A person
  has to supply what the Java produced. So it does not actually prove the Java matches the old
  code yet.
- AI Java translation: a first draft with gaps, not finished code (cheap model).
- Dependency map: finds references but does not yet tie a referenced name to its uploaded file.
- Public contact form: works, but goes nowhere real until the endpoint is set.

### Only a shell or not built yet
- The harness that compiles and runs the rewritten Java to produce real outputs. This is the
  missing core piece. Right now the outputs come from a person, not from running the code.
- There is no automated running of the old COBOL either, so the "expected output" is entered
  by a person or suggested by the AI for a person to confirm.

### Risky
- The big one: it would be easy to look at the green "passed" checks and think the system
  proved the Java is correct. It did not. A human typed in the output. Anyone using this has
  to understand that, because proving the match is the whole point of the business.
- The OpenRouter AI key is sitting in plain text in backend/application-local.properties and
  was flagged before as shared. Treat it as exposed and rotate it.
- The cheap AI model is fine for explaining, but its Java drafts need careful human review.
- Local Postgres trusts localhost with no password. Fine for a laptop, never for production.
- The Docker images have not been built or run on a machine that has Docker, so the container
  build is written but unproven.
- Tests are unit and web-layer plus the comparator and parser. There are no full
  database-backed integration tests yet.

## Small bug I found and fixed

- Bug: sending a malformed or unreadable request body returned a 500 "internal error", as if
  it were our fault. A bad body is the caller's to fix, so it should be a 400.
- Fix: I added a handler for HttpMessageNotReadableException in ApiExceptionHandler that
  returns 400 "bad_request" with a plain message, in the same style as the other handlers.
- Checked: the backend still passes all 32 tests and the style check, and I confirmed live
  that a bad body now returns 400 with the message "We could not read the request body.
  Please send valid JSON for this request."
- Note: while testing I also hit a quirk on my side (PowerShell turned a file-read string into
  an object), not a backend problem. The backend create endpoint works fine with a clean body.
