type PlaceholderPageProps = {
  title: string;
  description: string;
};

/** A calm stand-in for a screen we have not built yet. The frame and navigation work;
 * the real screen lands in the next step. */
export function PlaceholderPage({ title, description }: PlaceholderPageProps) {
  return (
    <section className="page">
      <h1 className="page__title">{title}</h1>
      <p className="page__intro">{description}</p>
      <p className="page__muted">This screen is coming next. The frame and navigation are ready.</p>
    </section>
  );
}
