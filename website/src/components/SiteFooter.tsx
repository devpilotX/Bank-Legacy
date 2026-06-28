import { Logo } from './Logo';
import { CONTACT_EMAIL } from '@/config';

// A plain footer: our name, one line about what we do, and how to reach us.
export function SiteFooter() {
  const year = new Date().getFullYear();

  return (
    <footer className="border-t border-border bg-secondary/40">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-12 sm:px-6">
        <Logo />
        <p className="max-w-md text-sm text-muted-foreground">
          Careful help with old core systems, for community banks.
        </p>
        <p className="text-sm text-muted-foreground">
          Email us at{' '}
          <a
            href={`mailto:${CONTACT_EMAIL}`}
            className="text-primary underline-offset-4 hover:underline"
          >
            {CONTACT_EMAIL}
          </a>
          .
        </p>
        <p className="text-sm text-muted-foreground">© {year} Corewise.</p>
      </div>
    </footer>
  );
}
