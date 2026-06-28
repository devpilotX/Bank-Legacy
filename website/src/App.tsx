import { SiteNav } from '@/components/SiteNav';
import { SiteFooter } from '@/components/SiteFooter';
import { Hero } from '@/sections/Hero';
import { Problem } from '@/sections/Problem';
import { WhatWeDo } from '@/sections/WhatWeDo';
import { WhoWeHelp } from '@/sections/WhoWeHelp';
import { WhyUs } from '@/sections/WhyUs';
import { Contact } from '@/sections/Contact';
import { useReducedMotion } from '@/lib/motion';
import { useSmoothScroll } from '@/lib/useSmoothScroll';
import { useSectionReveals } from '@/lib/useSectionReveals';

export function App() {
  // One switch drives all of the motion. If a visitor prefers reduced motion, we leave
  // smooth scroll and the reveals off, and the 3D hero falls back to its static panel.
  const reduced = useReducedMotion();
  useSmoothScroll(!reduced);
  useSectionReveals(!reduced);

  return (
    <div className="flex min-h-screen flex-col">
      {/* For keyboard users: jump straight past the nav to the content. Hidden until
          it is focused. */}
      <a
        href="#top"
        onClick={() => document.getElementById('top')?.focus({ preventScroll: true })}
        className="sr-only focus:not-sr-only focus:fixed focus:top-3 focus:left-4 focus:z-[60] focus:rounded-md focus:border focus:border-border focus:bg-background focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:text-foreground focus:shadow focus:ring-2 focus:ring-ring focus:outline-none"
      >
        Skip to content
      </a>
      <SiteNav />
      <main id="top" tabIndex={-1} className="flex-1 pt-16 outline-none">
        <Hero />
        <Problem />
        <WhatWeDo />
        <WhoWeHelp />
        <WhyUs />
        <Contact />
      </main>
      <SiteFooter />
    </div>
  );
}
