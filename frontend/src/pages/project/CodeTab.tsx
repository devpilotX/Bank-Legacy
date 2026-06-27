import { useProjectContext } from './useProjectContext';

export function CodeTab() {
  const { project } = useProjectContext();
  return <p className="page__muted">Code intake for {project.name} is coming next.</p>;
}
