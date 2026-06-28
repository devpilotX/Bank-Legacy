# Website (public)

The small public site for Corewise. It is its own app, separate from the internal tool, and
it shares no code with it. One long page that you scroll, with a fixed nav at the top that
jumps to each section, and a contact form at the bottom.

## The stack

- React and TypeScript, built with Vite.
- Tailwind CSS for styling.
- shadcn/ui for the base components (button, card, and the form fields).
- GSAP with ScrollTrigger for the gentle section reveals.
- Lenis for smooth scrolling, kept in step with ScrollTrigger so the two never fight.
- React Three Fiber and drei (three.js underneath) for the one small 3D moment in the hero.

The hero has a single calm 3D moment: an outlined old block giving way to a solid modern one.
It loads only on a wide screen, on a capable device, and only when the visitor has not asked
for reduced motion. Everyone else sees a clean static panel that says the same thing. The 3D
code sits in its own chunk, so it never slows down the first load.

If a visitor has reduced motion turned on, the smooth scroll, the reveals, and the 3D all turn
off, and the static version shows. That is on purpose: calmer and more accessible.

## Run it

From this folder:

```bash
npm install
npm run dev
```

Then open the address Vite prints, usually http://localhost:5174.

## The contact form

The form takes a name, bank, email, and a short message. By default, with no endpoint set, it
keeps each message in the browser and logs it to the console, so the flow works end to end
while you develop. Before the site goes live, set these two values so messages reach a real
inbox:

- `VITE_CONTACT_ENDPOINT` the URL that receives the message (an email service, or a small
  handler of your own). The form sends it a JSON POST.
- `VITE_CONTACT_EMAIL` the address shown on the page and in the thank-you.

Set them in a `.env` file in this folder. Vite reads it automatically, and Git ignores it:

```
VITE_CONTACT_ENDPOINT=https://your-handler.example.com/contact
VITE_CONTACT_EMAIL=hello@your-real-address.com
```

Until you set the endpoint, you can see what the form captured in the browser console and in
localStorage under the key `corewise.contact`.

## Build it

```bash
npm run build
```

This type-checks, lints, and writes a production build to `dist/`. To preview that build
locally:

```bash
npm run preview
```

## A note on the name

"Corewise" is a placeholder name. To change it, search the `website` folder for "Corewise" and
update the wordmark in `src/components/Logo.tsx`, the footer line, the page title in
`index.html`, and this README.

The site never talks to the internal tool or its backend.
