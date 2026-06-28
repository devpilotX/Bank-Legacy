import { Section, SectionHeading } from '@/components/Section';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

// Two parts, in the exact words from the old site: read and map first (nothing live is
// touched), then modernize one small piece at a time.
export function WhatWeDo() {
  return (
    <Section id="what-we-do">
      <SectionHeading eyebrow="What we do" title="We do the work in two parts." />
      <p className="mb-10 max-w-2xl text-lg text-muted-foreground">
        The first part is safe. We touch nothing that is running.
      </p>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <p className="text-sm font-medium text-primary">Part one</p>
            <CardTitle className="text-xl">Read and map</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            We take in your old code and read it, using AI to move fast and engineers to make
            sense of it. We write down what each part does and how the pieces fit together. You
            end up with a clear picture of your own system, in plain words, not locked in one
            person's memory. We change nothing that is live.
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <p className="text-sm font-medium text-primary">Part two</p>
            <CardTitle className="text-xl">Modernize, one piece at a time</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            When you are ready, we rewrite small pieces into modern Java while the old system
            keeps running. Before any new piece goes near production, we check that it behaves
            exactly like the old one. Small steps. Each one tested. Nothing rushed.
          </CardContent>
        </Card>
      </div>

      <div className="mt-12 max-w-2xl">
        <h3 className="text-xl font-semibold tracking-tight text-foreground">Low risk, on purpose</h3>
        <p className="mt-3 text-lg text-muted-foreground">
          You are never asked to flip a switch and hope. Nothing moves until it has been checked
          against how your system really behaves. If a piece is not ready, it waits. That is the
          deal.
        </p>
      </div>
    </Section>
  );
}
