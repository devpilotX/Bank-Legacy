import { useProjectContext } from './useProjectContext';

export function ReportTab() {
  const { project } = useProjectContext();
  return <p className="page__muted">The mapping report for {project.name} is coming next.</p>;
}
