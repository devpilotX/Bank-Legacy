import { Tag } from '@carbon/react';

type ReviewBadgeProps = {
  approved: boolean;
  by?: string | null;
  draftText?: string;
  approvedText?: string;
};

/**
 * Makes the difference between AI output and human-approved work unmistakable. Blue
 * means a draft a person still needs to check. Green means a person approved it.
 * Trust depends on this never being ambiguous, so it reads the same everywhere.
 */
export function ReviewBadge({
  approved,
  by,
  draftText = 'AI draft, not yet checked',
  approvedText = 'Approved by a person',
}: ReviewBadgeProps) {
  if (approved) {
    return (
      <Tag type="green" size="sm">
        {by ? `Approved by ${by}` : approvedText}
      </Tag>
    );
  }
  return (
    <Tag type="blue" size="sm">
      {draftText}
    </Tag>
  );
}
