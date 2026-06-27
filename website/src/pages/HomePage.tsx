import { Button } from '@carbon/react';
import { useNavigate } from 'react-router-dom';

export function HomePage() {
  const navigate = useNavigate();
  return (
    <div className="container">
      <section className="hero">
        <h1 className="hero__title">
          When your last COBOL expert retires, who will understand your core system?
        </h1>
        <p className="hero__lead">
          Most community banks still run on COBOL. It works. But the people who wrote it are
          retiring, and the knowledge is walking out the door with them.
        </p>
        <p className="hero__lead">
          We help you hold on to it. We read your old systems, write down clearly how they work,
          and modernize them one safe piece at a time. AI reads the code fast. Experienced engineers
          check every result, so nothing breaks.
        </p>
        <div className="cta-row">
          <Button size="lg" onClick={() => navigate('/contact')}>
            Book a short call
          </Button>
          <span className="cta-note">No pitch. We will listen, and tell you honestly if we can help.</span>
        </div>
      </section>
    </div>
  );
}
