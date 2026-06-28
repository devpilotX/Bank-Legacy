import { Section, SectionHeading } from '@/components/Section';
import { CONTACT_EMAIL } from '@/config';

// The contact section. The form (built with shadcn/ui) is added in the next step; for
// now this carries the intro and the email so the anchor and the page read correctly.
export function Contact() {
  return (
    <Section id="contact" className="bg-secondary/40">
      <SectionHeading eyebrow="Contact" title="Tell us what you are running." />
      <p className="max-w-2xl text-lg text-muted-foreground">
        We will get back to you, usually within a day or two. No pitch, no pressure. Prefer
        email? Reach us at{' '}
        <a
          href={`mailto:${CONTACT_EMAIL}`}
          className="text-primary underline-offset-4 hover:underline"
        >
          {CONTACT_EMAIL}
        </a>
        .
      </p>
    </Section>
  );
}
