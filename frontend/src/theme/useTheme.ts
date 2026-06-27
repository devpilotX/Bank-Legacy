import { useCallback, useEffect, useState } from 'react';

export type ThemeName = 'g100' | 'g10';

const STORAGE_KEY = 'modernization.theme';

function readInitialTheme(): ThemeName {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved === 'g10' || saved === 'g100') {
      return saved;
    }
  } catch {
    // localStorage can be blocked. The dark default is a fine fallback.
  }
  return 'g100';
}

// Holds the current theme, remembers it between visits, and keeps the <html>
// attribute in step so Carbon's tokens switch. g100 is the default and lives in
// CSS, so we only set the attribute for the light theme.
export function useTheme() {
  const [theme, setTheme] = useState<ThemeName>(readInitialTheme);

  useEffect(() => {
    const root = document.documentElement;
    if (theme === 'g10') {
      root.setAttribute('data-carbon-theme', 'g10');
    } else {
      root.removeAttribute('data-carbon-theme');
    }
    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch {
      // If we cannot save it, the app still works for this visit.
    }
  }, [theme]);

  const toggleTheme = useCallback(() => {
    setTheme((current) => (current === 'g100' ? 'g10' : 'g100'));
  }, []);

  return { theme, toggleTheme };
}
