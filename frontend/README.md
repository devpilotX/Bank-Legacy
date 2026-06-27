# Frontend

The screens our engineers use. React with TypeScript, built with Vite, and IBM Carbon for
the components and styling.

## Run it

From Git Bash, in this folder:

```bash
npm install
npm run dev
```

Then open the address Vite prints, usually http://localhost:5173.

The dev server sends `/health` and `/api` calls to the backend on port 8080, so start the
backend too (see the backend README) to watch the status turn green.

## Build it

```bash
npm run build
```

This type-checks the code and writes a production build to `dist/`.

## Notes

- The tool defaults to Carbon's dark theme (g100), since engineers stare at it for hours.
  Use the toggle in the header to switch to the light theme. Your choice is remembered.
- Components and icons come from `@carbon/react` and `@carbon/icons-react`. We lean on
  Carbon instead of hand rolling UI it already does well.
- The product name shows as "Corewise Modernization" for now. It is a working name and
  lives in `src/branding.ts`, so it is a one line change if we pick something else.
