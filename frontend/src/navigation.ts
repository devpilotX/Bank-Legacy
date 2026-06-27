import {
  Building,
  ChartRelationship,
  CheckmarkOutline,
  Code,
  Dashboard,
  Folder,
  Migrate,
} from '@carbon/icons-react';
import type { CarbonIconType } from '@carbon/icons-react';

export type NavItem = {
  label: string;
  to: string;
  icon: CarbonIconType;
};

// The sections of the tool. The pages behind Clients through Verify are placeholders
// for now; we build them out in the next step.
export const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', to: '/', icon: Dashboard },
  { label: 'Clients', to: '/clients', icon: Building },
  { label: 'Projects', to: '/projects', icon: Folder },
  { label: 'Code', to: '/code', icon: Code },
  { label: 'Map', to: '/map', icon: ChartRelationship },
  { label: 'Modernize', to: '/modernize', icon: Migrate },
  { label: 'Verify', to: '/verify', icon: CheckmarkOutline },
];
