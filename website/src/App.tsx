import { SiteNav } from '@/components/SiteNav';
import { SiteFooter } from '@/components/SiteFooter';
import { Hero } from '@/sections/Hero';
import { Problem } from '@/sections/Problem';
import { WhatWeDo } from '@/sections/WhatWeDo';
import { WhoWeHelp } from '@/sections/WhoWeHelp';
import { WhyUs } from '@/sections/WhyUs';
import { Contact } from '@/sections/Contact';

export function App() {
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
