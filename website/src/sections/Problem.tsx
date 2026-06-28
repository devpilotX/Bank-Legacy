import { Section, SectionHeading } from '@/components/Section';

// Names the bank's real fear plainly. The first paragraph is the exact line from the
// old site; the second stays in the same plain voice.
export function Problem() {
  return (
    <Section id="problem" className="bg-secondary/40">
      <SectionHeading
        eyebrow="The problem"
        title="The people who understand your core are retiring."
      />
      <div className="max-w-2xl space-y-5 text-lg text-muted-foreground">
        <p>
          Most community banks still run on COBOL. It works, and it has for years. But the
          people who wrote it are retiring, and the knowledge is walking out the door with
          them.
        </p>
        <p>
          When the last person who really understands your core system is gone, every change
          gets slower and riskier. The documentation is thin. The code becomes the only
          record of how the bank actually runs, and it is hard to read. That is a quiet kind
          of risk, and it grows every year.
        </p>
      </div>
    </Section>
  );
}
