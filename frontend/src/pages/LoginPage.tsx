import { useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { Button, Form, InlineNotification, PasswordInput, Stack, TextInput, Tile } from '@carbon/react';
import { Navigate, useNavigate } from 'react-router-dom';
import { ApiClientError } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { PRODUCT_NAME, PRODUCT_PREFIX } from '../branding';

/** The sign-in screen. On success it stores the token (through the auth context) and
 * sends the engineer into the app. On failure it shows a plain, friendly message. */
export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(email.trim(), password);
      navigate('/', { replace: true });
    } catch (caught) {
      if (caught instanceof ApiClientError && caught.status === 0) {
        setError('We could not reach the server. Please try again.');
      } else {
        setError('That email or password did not work. Try again.');
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="login">
      <Tile className="login__card">
        <p className="login__brand">
          {PRODUCT_PREFIX} {PRODUCT_NAME}
        </p>
        <h1 className="login__title">Sign in</h1>
        <Form onSubmit={onSubmit}>
          <Stack gap={6}>
            {error ? (
              <InlineNotification
                kind="error"
                lowContrast
                hideCloseButton
                title="Sign in failed"
                subtitle={error}
              />
            ) : null}
            <TextInput
              id="email"
              type="email"
              labelText="Email"
              autoComplete="username"
              value={email}
              onChange={(event: ChangeEvent<HTMLInputElement>) => setEmail(event.target.value)}
            />
            <PasswordInput
              id="password"
              labelText="Password"
              autoComplete="current-password"
              value={password}
              onChange={(event: ChangeEvent<HTMLInputElement>) => setPassword(event.target.value)}
            />
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Signing in...' : 'Sign in'}
            </Button>
          </Stack>
        </Form>
      </Tile>
    </main>
  );
}
