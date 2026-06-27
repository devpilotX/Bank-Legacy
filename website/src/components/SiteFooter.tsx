import { Logo } from './Logo';

export function SiteFooter() {
  const year = new Date().getFullYear();
  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <Logo />
        <p className="site-footer__line">Careful help with old core systems, for community banks.</p>
        <p className="site-footer__muted">© {year} Corewise.</p>
      </div>
    </footer>
  );
}
