import { Content } from '@carbon/react';
import { AppHeader } from './components/AppHeader';
import { Dashboard } from './pages/Dashboard';
import { useTheme } from './theme/useTheme';

export function App() {
  const { theme, toggleTheme } = useTheme();

  return (
    <>
      <AppHeader theme={theme} onToggleTheme={toggleTheme} />
      <Content>
        <Dashboard />
      </Content>
    </>
  );
}
