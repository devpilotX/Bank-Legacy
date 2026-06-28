import type { ReactNode } from 'react';
import { cn } from '@/lib/utils';

type SectionProps = {
  id: string;
  className?: string;
  children: ReactNode;
};

// Every section shares the same rhythm: generous vertical space, a centered column with
// a comfortable reading width, and a scroll offset so the fixed nav never covers the
// heading when you jump straight to it.
export function Section({ id, className, children }: SectionProps) {
  return (
    <section id={id} className={cn('scroll-mt-20 px-4 py-20 sm:px-6 sm:py-28', className)}>
      <div className="mx-auto max-w-5xl">{children}</div>
    </section>
  );
}

type SectionHeadingProps = {
  eyebrow: string;
  title: string;
  className?: string;
};

// A small label above a plain heading. The label is the one place we let the brand blue
// show in the body of the page, and we keep it light.
export function SectionHeading({ eyebrow, title, className }: SectionHeadingProps) {
  return (
    <div className={cn('mb-10 max-w-2xl', className)}>
      <p className="mb-3 text-sm font-medium tracking-widest text-primary uppercase">{eyebrow}</p>
      <h2 className="text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">{title}</h2>
    </div>
  );
}
