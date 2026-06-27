export function WhatWeDoPage() {
  return (
    <div className="container">
      <h1 className="page-title">What we do</h1>
      <p className="lead">
        We do the work in two parts. The first part is safe. We touch nothing that is running.
      </p>

      <div className="steps">
        <article className="step">
          <p className="step__num">Part one</p>
          <h2 className="step__title">Read and map</h2>
          <p className="step__text">
            We take in your old code and read it, using AI to move fast and engineers to make sense of it. We
            write down what each part does and how the pieces fit together. You end up with a clear picture of
            your own system, in plain words, not locked in one person's memory. We change nothing that is
            live.
          </p>
        </article>
        <article className="step">
          <p className="step__num">Part two</p>
          <h2 className="step__title">Modernize, one piece at a time</h2>
          <p className="step__text">
            When you are ready, we rewrite small pieces into modern Java while the old system keeps running.
            Before any new piece goes near production, we check that it behaves exactly like the old one.
            Small steps. Each one tested. Nothing rushed.
          </p>
        </article>
      </div>

      <div className="prose">
        <h2>Low risk, on purpose</h2>
        <p>
          You are never asked to flip a switch and hope. Nothing moves until it has been checked against how
          your system really behaves. If a piece is not ready, it waits. That is the deal.
        </p>
      </div>
    </div>
  );
}
