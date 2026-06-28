import { Button } from '@/components/ui/button';
import { Section, SectionHeading } from '@/components/Section';

// Why us, word for word from the old site: AI for speed, a person to make the call.
export function WhyUs() {
  return (
    <Section id="why-us">
      <SectionHeading eyebrow="Why us" title="AI speed, with a person who makes the call." />
      <div className="max-w-2xl space-y-5 text-lg text-muted-foreground">
        <p>
          AI can translate old code in seconds. It can also miss a rule that only your bank
          knows, and quietly break something that costs real money. Speed without understanding
          is dangerous in a bank.
        </p>
        <p>
          So we do not hand the keys to the AI. We use it for what it is good at: reading fast
          and drafting a first version. Then an experienced engineer checks every result against
          how your system actually behaves. The AI is the fast first read. A person makes the
          call.
        </p>
        <p>
          That pairing is the whole point. Fast enough to be worth doing. Careful enough to
          trust.
        </p>
      </div>
      <div className="mt-8 flex flex-col items-start gap-3 sm:flex-row sm:items-center">
        <Button asChild>
          <a href="#contact">Talk to us</a>
        </Button>
        <span className="text-sm text-muted-foreground">
          Tell us what you are running. We will be straight with you.
        </span>
      </div>
    </Section>
  );
}
