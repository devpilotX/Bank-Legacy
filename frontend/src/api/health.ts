export type HealthState =
  | { status: 'loading' }
  | { status: 'ok'; version: string }
  | { status: 'down'; detail: string };

type HealthResponse = {
  status: string;
  version: string;
};

// Asks the backend whether it is up. In development this goes through the Vite
// proxy to the Spring Boot app on port 8080. We keep the failure path calm: if the
// backend is down, we say so plainly instead of throwing a scary error.
export async function fetchHealth(): Promise<HealthState> {
  try {
    const response = await fetch('/health', {
      headers: { Accept: 'application/json' },
    });
    if (!response.ok) {
      return { status: 'down', detail: `The backend answered with ${response.status}.` };
    }
    const body = (await response.json()) as HealthResponse;
    return { status: 'ok', version: body.version };
  } catch {
    return { status: 'down', detail: 'We could not reach the backend. Is it running?' };
  }
}
