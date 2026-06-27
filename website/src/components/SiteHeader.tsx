import { Button } from '@carbon/react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Logo } from './Logo';

const NAV = [
  { to: '/', label: 'Home', end: true },
  { to: '/what-we-do', label: 'What we do', end: false },
  { to: '/who-we-help', label: 'Who we help', end: false },
  { to: '/why-us', label: 'Why us', end: false },
  { to: '/contact', label: 'Contact', end: false },
];

export function SiteHeader() {
  const navigate = useNavigate();
  return (
    <header className="site-header">
      <div className="site-header__inner">
        <Link to="/" className="site-header__brand" aria-label="Corewise home">
          <Logo />
        </Link>
        <nav className="site-header__nav" aria-label="Main">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `site-nav__link${isActive ? ' is-active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="site-header__cta">
          <Button size="md" onClick={() => navigate('/contact')}>
            Book a call
          </Button>
        </div>
      </div>
    </header>
  );
}
