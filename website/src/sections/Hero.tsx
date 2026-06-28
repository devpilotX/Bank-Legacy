import { lazy, Suspense } from 'react';
import { Button } from '@/components/ui/button';
import { HeroVisualStatic } from './HeroVisualStatic';
import { useEnable3D } from '@/lib/motion';

// The 3D scene is split into its own chunk and only fetched when we decide to show it,
// so the page shows text right away and the heavy code never reaches phones, low-power
// devices, or reduced-motion visitors.
const HeroScene = lazy(() => import('./HeroScene'));

// The hero: the one strong moment at the top. The text leads, and a calm visual sits
// beside it. On a capable wide screen with motion allowed, the visual is a slow 3D
// moment. Everywhere else it is the static panel, which is also what shows while the 3D
// loads, so the hero always looks complete.
export function Hero() {
  const show3D = useEnable3D();

  return (
    <section className="px-4 pt-12 pb-20 sm:px-6 sm:pt-20 sm:pb-28">
      <div className="mx-auto grid max-w-6xl items-center gap-12 lg:grid-cols-2 lg:gap-16">
        <div className="max-w-xl">
          <h1 className="text-4xl font-semibold tracking-tight text-balance text-foreground sm:text-5xl">
            When your last COBOL expert retires, who will understand your core system?
          </h1>
          <p className="mt-6 text-lg text-muted-foreground">
            Most community banks still run on COBOL. We help you read those old systems,
            write down how they really work, and modernize them one safe piece at a time.
          </p>
          <p className="mt-4 text-lg text-muted-foreground">
            AI reads the code fast. Experienced engineers check every result, so nothing
            breaks.
          </p>
          <div className="mt-8 flex flex-col items-start gap-3 sm:flex-row sm:items-center">
            <Button asChild size="lg">
              <a href="#contact">Book a short call</a>
            </Button>
            <span className="text-sm text-muted-foreground">
              No pitch. We will listen, and tell you honestly if we can help.
            </span>
          </div>
        </div>

        <div className="lg:pl-4">
          {show3D ? (
            <Suspense fallback={<HeroVisualStatic />}>
              <HeroScene />
            </Suspense>
          ) : (
            <HeroVisualStatic />
          )}
        </div>
      </div>
    </section>
  );
}
