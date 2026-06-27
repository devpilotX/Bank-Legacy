import { Building, Dashboard, Folder } from '@carbon/icons-react';
import type { CarbonIconType } from '@carbon/icons-react';

export type NavItem = {
  label: string;
  to: string;
  icon: CarbonIconType;
};

// The top-level sections. Code, Map, Modernize, Verify, and Report live inside a
// project, since they only make sense for a chosen project, so they are tabs on the
// project page rather than standalone nav items.
export const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', to: '/', icon: Dashboard },
  { label: 'Clients', to: '/clients', icon: Building },
  { label: 'Projects', to: '/projects', icon: Folder },
];
