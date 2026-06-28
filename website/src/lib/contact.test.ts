import { describe, it, expect } from 'vitest';

import { contactProblem, isBotSubmission } from './contact';

const valid = {
  name: 'Dana Lee',
  bank: 'Riverbend Credit Union',
  email: 'dana@riverbend.example',
  message: 'We run an old core and would like to talk.',
};

describe('contactProblem', () => {
  it('accepts a fully filled, valid form', () => {
    expect(contactProblem(valid)).toBeNull();
  });

  it('asks for every field when one is blank', () => {
    expect(contactProblem({ ...valid, message: '   ' })).toBe('Please fill in every field.');
  });

  it('asks for a real email when the address is malformed', () => {
    expect(contactProblem({ ...valid, email: 'not-an-email' })).toBe(
      'Please enter an email we can reply to.',
    );
  });
});

describe('isBotSubmission', () => {
  it('is false when the honeypot is empty', () => {
    expect(isBotSubmission('')).toBe(false);
    expect(isBotSubmission('   ')).toBe(false);
  });

  it('is true when the honeypot has anything in it', () => {
    expect(isBotSubmission('http://spam.example')).toBe(true);
  });
});
