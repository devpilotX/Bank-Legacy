import { Loading } from '@carbon/react';

type LoadingStateProps = {
  label?: string;
};

/** What a data screen shows while it waits. Every screen uses this, so loading looks
 * the same everywhere. */
export function LoadingState({ label = 'Loading...' }: LoadingStateProps) {
  return (
    <div className="state-block" role="status" aria-live="polite">
      <Loading small withOverlay={false} description={label} />
      <p className="state-block__text">{label}</p>
    </div>
  );
}
