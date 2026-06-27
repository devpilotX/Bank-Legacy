import { useProjectContext } from './useProjectContext';

export function ModernizeTab() {
  const { project } = useProjectContext();
  return <p className="page__muted">The modernization workspace for {project.name} is coming next.</p>;
}
