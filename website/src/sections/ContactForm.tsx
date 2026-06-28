import { useState } from 'react';
import type { FormEvent } from 'react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { CONTACT_EMAIL, CONTACT_ENDPOINT } from '@/config';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// The contact form: name, bank, email, and a short message. It checks the fields, then
// sends the message to the endpoint from config. If no endpoint is set yet, it keeps the
// message in the browser so the flow still works while we build. It shows a plain
// thank-you when it sends and a calm message if something goes wrong.
export function ContactForm() {
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

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
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
        // No endpoint is set yet, so we keep the message in the browser and log it. This
        // lets the form work end to end while we build. Before launch, set
        // VITE_CONTACT_ENDPOINT to a real handler or email service (see the README).
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
      <div
        className="rounded-xl border border-border bg-card p-6 shadow-sm"
        role="status"
        aria-live="polite"
      >
        <h3 className="text-lg font-semibold text-foreground">Thank you</h3>
        <p className="mt-2 text-muted-foreground">
          We have your message and will be in touch soon, usually within a day or two.
        </p>
        <p className="mt-2 text-muted-foreground">
          If you would rather email, reach us at{' '}
          <a
            href={`mailto:${CONTACT_EMAIL}`}
            className="text-primary underline-offset-4 hover:underline"
          >
            {CONTACT_EMAIL}
          </a>
          .
        </p>
      </div>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      noValidate
      className="rounded-xl border border-border bg-card p-6 shadow-sm"
    >
      {error ? (
        <p
          role="alert"
          className="mb-5 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive"
        >
          {error}
        </p>
      ) : null}

      <div className="grid gap-5">
        <div className="grid gap-2">
          <Label htmlFor="contact-name">Your name</Label>
          <Input
            id="contact-name"
            name="name"
            autoComplete="name"
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
        </div>

        <div className="grid gap-2">
          <Label htmlFor="contact-bank">Your bank or credit union</Label>
          <Input
            id="contact-bank"
            name="bank"
            autoComplete="organization"
            value={bank}
            onChange={(event) => setBank(event.target.value)}
          />
        </div>

        <div className="grid gap-2">
          <Label htmlFor="contact-email">Email</Label>
          <Input
            id="contact-email"
            name="email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>

        <div className="grid gap-2">
          <Label htmlFor="contact-message">A short message</Label>
          <Textarea
            id="contact-message"
            name="message"
            rows={5}
            value={message}
            onChange={(event) => setMessage(event.target.value)}
          />
        </div>

        <Button type="submit" disabled={submitting} className="justify-self-start">
          {submitting ? 'Sending...' : 'Send'}
        </Button>
      </div>
    </form>
  );
}
