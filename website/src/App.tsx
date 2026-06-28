import { SiteNav } from '@/components/SiteNav';
import { SiteFooter } from '@/components/SiteFooter';

export function App() {
  return (
    <div className="flex min-h-screen flex-col">
      <SiteNav />
      <main id="top" className="flex-1 pt-16">
        {/* Sections are added next, in order: hero, the problem we solve, what we do,
            who we help, why us, and contact. */}
      </main>
      <SiteFooter />
    </div>
  );
}
