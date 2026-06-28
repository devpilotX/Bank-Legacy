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
      <SiteNav />
      <main id="top" className="flex-1 pt-16">
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
