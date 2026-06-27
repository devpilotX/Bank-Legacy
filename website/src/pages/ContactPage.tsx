import { useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { Button, Form, InlineNotification, Stack, TextArea, TextInput } from '@carbon/react';
import { CONTACT_EMAIL, CONTACT_ENDPOINT } from '../config';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function ContactPage() {
  const [name, setName] = useState('');
  const [bank, setBank] = useState('');
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);

  function firstProblem(): string | null {
    if (!name.trim() || !bank.trim() || !email.trim() || !message.trim()) {
      return 'Please fill in every field.';
    }
    if (!EMAIL_PATTERN.test(email.trim())) {
      return 'Please enter an email we can reply to.';
    }
    return null;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    const problem = firstProblem();
    if (problem) {
      setError(problem);
      return;
    }
    setError(null);
    setSubmitting(true);
    const payload = {
      name: name.trim(),
      bank: bank.trim(),
      email: email.trim(),
      message: message.trim(),
      sentAt: new Date().toISOString(),
    };
    try {
      if (CONTACT_ENDPOINT) {
        const response = await fetch(CONTACT_ENDPOINT, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });
        if (!response.ok) {
          throw new Error('send failed');
        }
      } else {
        // No endpoint is set yet, so we keep the message in the browser and log it.
        // This lets the form work end to end in development. Before launch, set
        // VITE_CONTACT_ENDPOINT to a real handler or email service.
        try {
          const key = 'corewise.contact';
          const saved = JSON.parse(localStorage.getItem(key) ?? '[]');
          saved.push(payload);
          localStorage.setItem(key, JSON.stringify(saved));
        } catch {
          // If storage is blocked, we still show the thank-you below.
        }
        console.info('Contact message (no endpoint set, kept locally):', payload);
      }
      setSent(true);
    } catch {
      setError('Something went wrong sending your message. Please try again, or email us.');
    } finally {
      setSubmitting(false);
    }
  }

  if (sent) {
    return (
      <div className="container">
        <h1 className="page-title">Thank you</h1>
        <div className="thank-you">
          <p>Thank you. We have your message and will be in touch soon, usually within a day or two.</p>
          <p>If you would rather email, reach us at {CONTACT_EMAIL}.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h1 className="page-title">Contact</h1>
      <p className="lead">
        Tell us a little about your situation. We will get back to you, usually within a day or two.
        No pitch, no pressure.
      </p>

      <div className="contact-grid">
        <Form className="contact-form" onSubmit={onSubmit}>
          <Stack gap={6}>
            {error && (
              <InlineNotification kind="error" lowContrast hideCloseButton title="Please check the form" subtitle={error} />
            )}
            <TextInput
              id="contact-name"
              labelText="Your name"
              value={name}
              onChange={(event: ChangeEvent<HTMLInputElement>) => setName(event.target.value)}
            />
            <TextInput
              id="contact-bank"
              labelText="Your bank or credit union"
              value={bank}
              onChange={(event: ChangeEvent<HTMLInputElement>) => setBank(event.target.value)}
            />
            <TextInput
              id="contact-email"
              type="email"
              labelText="Email"
              value={email}
              onChange={(event: ChangeEvent<HTMLInputElement>) => setEmail(event.target.value)}
            />
            <TextArea
              id="contact-message"
              labelText="A short message"
              rows={5}
              value={message}
              onChange={(event: ChangeEvent<HTMLTextAreaElement>) => setMessage(event.target.value)}
            />
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Sending...' : 'Send'}
            </Button>
          </Stack>
        </Form>

        <aside className="contact-aside">
          <p>Prefer email? Reach us at {CONTACT_EMAIL}.</p>
          <p>We read every message ourselves. You will hear back from a person, not a bot.</p>
        </aside>
      </div>
    </div>
  );
}
