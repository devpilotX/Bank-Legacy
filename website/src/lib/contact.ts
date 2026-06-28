// The rules that decide whether the contact form can be sent. They live here as plain
// functions so they are easy to read and easy to test on their own.

export type ContactValues = {
  name: string;
  bank: string;
  email: string;
  message: string;
};

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Returns the first problem with the form, or null when everything is fine.
export function contactProblem(values: ContactValues): string | null {
  if (
    !values.name.trim() ||
    !values.bank.trim() ||
    !values.email.trim() ||
    !values.message.trim()
  ) {
    return 'Please fill in every field.';
  }
  if (!EMAIL_PATTERN.test(values.email.trim())) {
    return 'Please enter an email we can reply to.';
  }
  return null;
}

// The form has a hidden field that real people never see or fill in. If it has any
// value, the submission almost certainly came from a bot, so we quietly ignore it.
export function isBotSubmission(honeypot: string): boolean {
  return honeypot.trim() !== '';
}
