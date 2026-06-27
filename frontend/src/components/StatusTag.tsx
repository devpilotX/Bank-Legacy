import { Tag } from '@carbon/react';

type TagType =
  'red' | 'magenta' | 'purple' | 'blue' | 'cyan' | 'teal' | 'green' | 'gray' | 'cool-gray' | 'warm-gray';

// One place that decides the color for every status we show, so a stage or a state
// looks the same on every screen.
const COLORS: Record<string, TagType> = {
  // Project stages.
  intake: 'cool-gray',
  mapping: 'teal',
  modernizing: 'purple',
  verifying: 'cyan',
  done: 'green',
  // Client status.
  prospect: 'teal',
  active: 'green',
  archived: 'cool-gray',
  // Source file status.
  received: 'cool-gray',
  analyzing: 'teal',
  analyzed: 'cyan',
  rewritten: 'purple',
  verified: 'green',
  // Work unit status.
  todo: 'cool-gray',
  in_progress: 'teal',
  in_review: 'cyan',
};

function toLabel(status: string): string {
  const spaced = status.replace(/_/g, ' ');
  return spaced.charAt(0).toUpperCase() + spaced.slice(1);
}

export function StatusTag({ status }: { status: string }) {
  return (
    <Tag type={COLORS[status] ?? 'gray'} size="sm">
      {toLabel(status)}
    </Tag>
  );
}
