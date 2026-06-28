import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

// IBM Plex Sans is the body face, self-hosted so there is no outside font CDN.
import '@fontsource/ibm-plex-sans/300.css';
import '@fontsource/ibm-plex-sans/400.css';
import '@fontsource/ibm-plex-sans/500.css';
import '@fontsource/ibm-plex-sans/600.css';
// IBM Plex Mono is used only for the small code accents in the hero.
import '@fontsource/ibm-plex-mono/400.css';
import '@fontsource/ibm-plex-mono/500.css';

import './index.css';
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
