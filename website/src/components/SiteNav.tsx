import { useState } from 'react';
import { Menu, X } from 'lucide-react';

import { Logo } from './Logo';
import { Button } from '@/components/ui/button';
import { SECTIONS } from '@/lib/sections';

// The top nav stays fixed so the section links are always one click away. The links
// are built straight from the shared SECTIONS list, so they cannot drift out of sync
// with the sections on the page. On a phone the links fold into a simple menu.
export function SiteNav() {
  const [open, setOpen] = useState(false);

  return (
    <header className="fixed inset-x-0 top-0 z-50 border-b border-border bg-background/80 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
        <a
          href="#top"
          aria-label="Corewise, back to top"
          className="rounded-sm focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none"
        >
          <Logo />
        </a>

        {/* Wide screens: the links and the call to action sit in the bar. */}
        <nav className="hidden items-center gap-1 md:flex" aria-label="Main">
          {SECTIONS.map((section) => (
            <a
              key={section.id}
              href={`#${section.id}`}
              className="rounded-md px-3 py-2 text-sm text-muted-foreground transition-colors hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none"
            >
              {section.label}
            </a>
          ))}
        </nav>

        <div className="hidden md:block">
          <Button asChild size="sm">
            <a href="#contact">Book a call</a>
          </Button>
        </div>

        {/* Narrow screens: a single button opens the menu. */}
        <button
          type="button"
          className="inline-flex size-10 items-center justify-center rounded-md text-foreground hover:bg-muted focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none md:hidden"
          aria-label={open ? 'Close menu' : 'Open menu'}
          aria-expanded={open}
          aria-controls="mobile-menu"
          onClick={() => setOpen((value) => !value)}
        >
          {open ? <X className="size-5" /> : <Menu className="size-5" />}
        </button>
      </div>

      {open ? (
        <div id="mobile-menu" className="border-t border-border bg-background md:hidden">
          <nav className="mx-auto flex max-w-6xl flex-col px-4 py-2 sm:px-6" aria-label="Main">
            {SECTIONS.map((section) => (
              <a
                key={section.id}
                href={`#${section.id}`}
                onClick={() => setOpen(false)}
                className="rounded-md px-2 py-3 text-base text-foreground hover:bg-muted focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none"
              >
                {section.label}
              </a>
            ))}
            <Button asChild className="my-2">
              <a href="#contact" onClick={() => setOpen(false)}>
                Book a call
              </a>
            </Button>
          </nav>
        </div>
      ) : null}
    </header>
  );
}
