import { useProjectContext } from './useProjectContext';

export function MapTab() {
  const { project } = useProjectContext();
  return <p className="page__muted">The dependency map for {project.name} is coming next.</p>;
}
