// Where the contact form sends a message, and the address we show. Both come from
// config so nothing is hardcoded. The email below is a placeholder to replace with
// our real one before the site goes live.
export const CONTACT_ENDPOINT: string | undefined = import.meta.env.VITE_CONTACT_ENDPOINT;
export const CONTACT_EMAIL: string = import.meta.env.VITE_CONTACT_EMAIL ?? 'hello@corewise.com';
