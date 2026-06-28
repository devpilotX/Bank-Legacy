import { Button } from '@/components/ui/button';
import { HeroVisualStatic } from './HeroVisualStatic';

// The hero: the one strong moment at the top. The text leads, and a calm visual sits
// beside it. The 3D version of this visual is added next; for now the static panel
// stands in, and it stays as the fallback for phones, low-power devices, and reduced
// motion, so the page always looks complete even when the 3D never loads.
export function Hero() {
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
          <HeroVisualStatic />
        </div>
      </div>
    </section>
  );
}
