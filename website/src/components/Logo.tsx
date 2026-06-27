type LogoProps = {
  withText?: boolean;
};

/**
 * Our mark, drawn by hand. Two blocks and a line between them: the old core on the
 * left as an outline, carried carefully across to the modern one on the right. It
 * uses currentColor, so it takes the color of the text around it. No clip art.
 */
export function Logo({ withText = true }: LogoProps) {
  return (
    <span className="logo">
      <svg className="logo__mark" viewBox="0 0 32 24" width="34" height="26" role="img" aria-label="Corewise">
        <rect
          x="1.5"
          y="6.75"
          width="10.5"
          height="10.5"
          rx="2"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.6"
        />
        <line x1="12.25" y1="12" x2="19.75" y2="12" stroke="currentColor" strokeWidth="1.6" />
        <rect x="20" y="6.75" width="10.5" height="10.5" rx="2" fill="currentColor" />
      </svg>
      {withText ? <span className="logo__word">Corewise</span> : null}
    </span>
  );
}
