# Website (public)

The small public site for Corewise. It is its own React and Carbon app, fully separate from the
internal tool, and it shares no code with it. Light theme, a few short pages.

## Run it

From Git Bash, in this folder:

```bash
npm install
npm run dev
```

Then open the address Vite prints, usually http://localhost:5174.

## Build it

```bash
npm run build
```

This writes a production build to `dist/`.

## The contact form

By default the form keeps each message in the browser and logs it, so the flow works while we
develop. Before the site goes live, set these at build time so messages reach a real inbox:

- `VITE_CONTACT_ENDPOINT` the URL that receives the message (an email service, or a small handler).
- `VITE_CONTACT_EMAIL` the address shown on the contact page.

The site never talks to the internal tool or its backend.
