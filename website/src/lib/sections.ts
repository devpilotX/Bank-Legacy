// The single source of truth for the page sections. The nav builds its links from
// this, and each section uses the same id as its anchor, so the two never drift apart.
// The hero is reached by the logo (a jump to the top), so it is not listed here.
export type SectionLink = {
  id: string;
  label: string;
};

export const SECTIONS: SectionLink[] = [
  { id: 'problem', label: 'The problem' },
  { id: 'what-we-do', label: 'What we do' },
  { id: 'who-we-help', label: 'Who we help' },
  { id: 'why-us', label: 'Why us' },
  { id: 'contact', label: 'Contact' },
];
