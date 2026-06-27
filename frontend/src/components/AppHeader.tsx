import {
  Header,
  HeaderName,
  HeaderGlobalBar,
  HeaderGlobalAction,
  SkipToContent,
} from '@carbon/react';
import { Asleep, Light } from '@carbon/icons-react';
import { PRODUCT_NAME, PRODUCT_PREFIX } from '../branding';
import type { ThemeName } from '../theme/useTheme';

type AppHeaderProps = {
  theme: ThemeName;
  onToggleTheme: () => void;
};

// The top bar. Carbon's UI Shell gives us the header, the product name, and a
// place for global actions. We keep it to the name and a theme toggle for now.
export function AppHeader({ theme, onToggleTheme }: AppHeaderProps) {
  const onDarkTheme = theme === 'g100';
  const toggleLabel = onDarkTheme ? 'Switch to the light theme' : 'Switch to the dark theme';

  return (
    <Header aria-label={`${PRODUCT_PREFIX} ${PRODUCT_NAME}`}>
      <SkipToContent />
      <HeaderName href="/" prefix={PRODUCT_PREFIX}>
        {PRODUCT_NAME}
      </HeaderName>
      <HeaderGlobalBar>
        <HeaderGlobalAction
          aria-label={toggleLabel}
          tooltipAlignment="end"
          onClick={onToggleTheme}
        >
          {onDarkTheme ? <Light size={20} /> : <Asleep size={20} />}
        </HeaderGlobalAction>
      </HeaderGlobalBar>
    </Header>
  );
}
