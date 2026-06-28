import { cn } from '@/lib/utils';

type LogoProps = {
  withText?: boolean;
  className?: string;
};

// Our mark, drawn by hand. Two slightly offset rounded squares in the deep blue: an
// outlined one (the old block) becoming a solid one (the new block). Monochrome, so it
// stays clean and reads well at a small size in the nav. No image files, no clip art.
//
// NOTE: "Corewise" is still a placeholder name. To change it, edit the wordmark text
// below and the few other places that say Corewise (index.html, the footer line, the
// READMEs). Search the repo for "Corewise" to find them all.
export function Logo({ withText = true, className }: LogoProps) {
  return (
    <span className={cn('inline-flex items-center gap-2.5', className)}>
      <svg
        className="size-6 shrink-0 text-primary"
        viewBox="0 0 24 24"
        width="24"
        height="24"
        role="img"
        aria-label="Corewise"
        fill="none"
      >
        {/* The old block: outlined. */}
        <rect x="1.75" y="1.75" width="10.5" height="10.5" rx="3" stroke="currentColor" strokeWidth="1.75" />
        {/* The new block: solid, offset down and to the right. */}
        <rect x="11.75" y="11.75" width="10.5" height="10.5" rx="3" fill="currentColor" />
      </svg>
      {withText ? (
        <span className="text-base font-semibold tracking-tight text-foreground">Corewise</span>
      ) : null}
    </span>
  );
}
