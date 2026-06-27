import { Button } from '@carbon/react';

type ErrorStateProps = {
  title?: string;
  message: string;
  onRetry?: () => void;
};

/** What a data screen shows when something fails: a calm title, the plain message we
 * were given, and an optional way to try again. No stack traces, no jargon. */
export function ErrorState({ title = 'We hit a snag', message, onRetry }: ErrorStateProps) {
  return (
    <div className="state-block" role="alert">
      <h2 className="state-block__title">{title}</h2>
      <p className="state-block__text">{message}</p>
      {onRetry ? (
        <Button kind="tertiary" size="sm" onClick={onRetry}>
          Try again
        </Button>
      ) : null}
    </div>
  );
}
