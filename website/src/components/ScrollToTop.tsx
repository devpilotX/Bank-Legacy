import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

/** Sends the page back to the top when you move to a new page, so you start reading
 * from the start. */
export function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  return null;
}
