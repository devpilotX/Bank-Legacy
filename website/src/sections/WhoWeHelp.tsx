import { Section, SectionHeading } from '@/components/Section';

// Who we help, word for word from the old site.
export function WhoWeHelp() {
  return (
    <Section id="who-we-help" className="bg-secondary/40">
      <SectionHeading
        eyebrow="Who we help"
        title="Smaller banks, the ones the big vendors skip."
      />
      <div className="max-w-2xl space-y-5 text-lg text-muted-foreground">
        <p>
          We work with community banks, credit unions, and smaller regional banks. The kind of
          place where a few people quietly keep the core running, year after year.
        </p>
        <p>
          The big vendors chase the big banks. Older core systems at a smaller scale, the layer
          you live in, mostly get ignored. We do not ignore it. It is the work we chose.
        </p>
        <p>
          If you are running an old core and worried about who really understands it, you are
          exactly who we built this for.
        </p>
      </div>
    </Section>
  );
}
