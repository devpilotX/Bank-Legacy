/// <reference types="vite/client" />

interface ImportMetaEnv {
  // Where the contact form posts. If unset, the form keeps the message locally so the
  // flow works in development; set this to a real handler or email service before launch.
  readonly VITE_CONTACT_ENDPOINT?: string;
  // The email address we show on the contact page.
  readonly VITE_CONTACT_EMAIL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
