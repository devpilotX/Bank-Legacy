import { Section, SectionHeading } from '@/components/Section';
import { CONTACT_EMAIL } from '@/config';
import { ContactForm } from './ContactForm';

// The contact section: a short, plain intro on one side and the form on the other.
export function Contact() {
  return (
    <Section id="contact" className="bg-secondary/40">
      <SectionHeading eyebrow="Contact" title="Tell us what you are running." />
      <div className="grid gap-10 lg:grid-cols-2 lg:gap-16">
        <div className="max-w-md space-y-4 text-lg text-muted-foreground">
          <p>We will get back to you, usually within a day or two. No pitch, no pressure.</p>
          <p>We read every message ourselves. You will hear back from a person, not a bot.</p>
          <p className="text-base">
            Prefer email? Reach us at{' '}
            <a
              href={`mailto:${CONTACT_EMAIL}`}
              className="text-primary underline-offset-4 hover:underline"
            >
              {CONTACT_EMAIL}
            </a>
            .
          </p>
        </div>
        <ContactForm />
      </div>
    </Section>
  );
}
