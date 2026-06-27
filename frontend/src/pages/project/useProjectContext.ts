import { useOutletContext } from 'react-router-dom';
import type { Project } from '../../api/types';

export type ProjectOutletContext = {
  project: Project;
  reload: () => void;
};

/** Tabs read the current project (and a way to refresh it) from the project page. */
export function useProjectContext(): ProjectOutletContext {
  return useOutletContext<ProjectOutletContext>();
}
