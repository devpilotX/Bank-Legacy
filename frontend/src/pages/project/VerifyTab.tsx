import { useProjectContext } from './useProjectContext';

export function VerifyTab() {
  const { project } = useProjectContext();
  return <p className="page__muted">Verification for {project.name} is coming next.</p>;
}
