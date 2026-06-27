import {
  Content,
  Header,
  HeaderContainer,
  HeaderGlobalAction,
  HeaderGlobalBar,
  HeaderMenuButton,
  HeaderName,
  SideNav,
  SideNavItems,
  SideNavLink,
  SkipToContent,
} from '@carbon/react';
import { Asleep, Light, Logout } from '@carbon/icons-react';
import type { MouseEvent } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { PRODUCT_NAME, PRODUCT_PREFIX } from '../branding';
import { NAV_ITEMS } from '../navigation';
import { useTheme } from '../theme/ThemeContext';

function isActive(pathname: string, to: string): boolean {
  return to === '/' ? pathname === '/' : pathname === to || pathname.startsWith(`${to}/`);
}

/**
 * The frame every signed-in screen sits in: a top header with the product name, a
 * theme toggle, and a sign-out button, plus a side nav for the sections. The actual
 * screen renders in the content area through the router outlet.
 */
export function AppShell() {
  const { theme, toggleTheme } = useTheme();
  const { logout } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const onDarkTheme = theme === 'g100';

  return (
    <HeaderContainer
      render={({ isSideNavExpanded, onClickSideNavExpand }) => (
        <>
          <Header aria-label={`${PRODUCT_PREFIX} ${PRODUCT_NAME}`}>
            <SkipToContent />
            <HeaderMenuButton
              aria-label={isSideNavExpanded ? 'Close menu' : 'Open menu'}
              onClick={onClickSideNavExpand}
              isActive={isSideNavExpanded}
              aria-expanded={isSideNavExpanded}
            />
            <HeaderName
              prefix={PRODUCT_PREFIX}
              href="/"
              onClick={(event: MouseEvent<HTMLAnchorElement>) => {
                event.preventDefault();
                navigate('/');
              }}
            >
              {PRODUCT_NAME}
            </HeaderName>
            <HeaderGlobalBar>
              <HeaderGlobalAction
                aria-label={onDarkTheme ? 'Switch to the light theme' : 'Switch to the dark theme'}
                onClick={toggleTheme}
                tooltipAlignment="center"
              >
                {onDarkTheme ? <Light size={20} /> : <Asleep size={20} />}
              </HeaderGlobalAction>
              <HeaderGlobalAction aria-label="Sign out" onClick={logout} tooltipAlignment="end">
                <Logout size={20} />
              </HeaderGlobalAction>
            </HeaderGlobalBar>
            <SideNav
              aria-label="Main navigation"
              expanded={isSideNavExpanded}
              onSideNavBlur={onClickSideNavExpand}
              isPersistent
            >
              <SideNavItems>
                {NAV_ITEMS.map((item) => (
                  <SideNavLink
                    key={item.to}
                    renderIcon={item.icon}
                    href={item.to}
                    isActive={isActive(pathname, item.to)}
                    onClick={(event: MouseEvent<HTMLAnchorElement>) => {
                      event.preventDefault();
                      navigate(item.to);
                    }}
                  >
                    {item.label}
                  </SideNavLink>
                ))}
              </SideNavItems>
            </SideNav>
          </Header>
          <Content className="app-content">
            <Outlet />
          </Content>
        </>
      )}
    />
  );
}
