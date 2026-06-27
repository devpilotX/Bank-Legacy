import { Button } from '@carbon/react';
import { useNavigate } from 'react-router-dom';

export function WhyUsPage() {
  const navigate = useNavigate();
  return (
    <div className="container">
      <h1 className="page-title">Why us</h1>
      <div className="prose">
        <p>
          AI can translate old code in seconds. It can also miss a rule that only your bank knows, and quietly
          break something that costs real money. Speed without understanding is dangerous in a bank.
        </p>
        <p>
          So we do not hand the keys to the AI. We use it for what it is good at: reading fast and drafting a
          first version. Then an experienced engineer checks every result against how your system actually
          behaves. The AI is the fast first read. A person makes the call.
        </p>
        <p>That pairing is the whole point. Fast enough to be worth doing. Careful enough to trust.</p>
      </div>
      <div className="cta-row">
        <Button onClick={() => navigate('/contact')}>Talk to us</Button>
        <span className="cta-note">Tell us what you are running. We will be straight with you.</span>
      </div>
    </div>
  );
}
