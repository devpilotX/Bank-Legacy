import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

// IBM Plex Sans is Carbon's typeface. We self-host it through fontsource so there
// is no outside CDN to depend on. We load just the weights we actually use.
import '@fontsource/ibm-plex-sans/300.css';
import '@fontsource/ibm-plex-sans/400.css';
import '@fontsource/ibm-plex-sans/600.css';

import './index.scss';
import { App } from './App';

const container = document.getElementById('root');
if (!container) {
  throw new Error('The root element is missing from index.html.');
}

createRoot(container).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
