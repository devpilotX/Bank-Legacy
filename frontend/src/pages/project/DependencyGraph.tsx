import type { DependencyEdge, DependencyNode } from '../../api/types';

type DependencyGraphProps = {
  nodes: DependencyNode[];
  edges: DependencyEdge[];
  selectedId: number | null;
  onSelect: (id: number) => void;
};

// Node color by kind, using Carbon tokens so it follows the theme.
const KIND_FILL: Record<string, string> = {
  program: 'var(--cds-support-info)',
  copybook: 'var(--cds-support-success)',
  file: 'var(--cds-support-warning)',
  paragraph: 'var(--cds-support-info)',
  unknown: 'var(--cds-icon-secondary)',
};

/**
 * A simple, readable graph laid out on a circle. Solid lines are links we read
 * straight from the code. Dashed lines are AI suggestions waiting on a person. Click
 * a node to select it.
 */
export function DependencyGraph({ nodes, edges, selectedId, onSelect }: DependencyGraphProps) {
  const width = 820;
  const height = 560;
  const centerX = width / 2;
  const centerY = height / 2;
  const radius = Math.min(width, height) / 2 - 90;

  const positions = new Map<number, { x: number; y: number }>();
  const count = Math.max(1, nodes.length);
  nodes.forEach((node, index) => {
    const angle = (2 * Math.PI * index) / count - Math.PI / 2;
    positions.set(node.id, {
      x: centerX + radius * Math.cos(angle),
      y: centerY + radius * Math.sin(angle),
    });
  });

  return (
    <svg className="dep-graph" viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Dependency map">
      {edges
        .filter((edge) => edge.status !== 'rejected')
        .map((edge) => {
          const from = positions.get(edge.fromNodeId);
          const to = positions.get(edge.toNodeId);
          if (!from || !to) return null;
          const isAi = edge.origin === 'ai';
          const dashed = isAi && edge.status !== 'confirmed';
          return (
            <line
              key={edge.id}
              x1={from.x}
              y1={from.y}
              x2={to.x}
              y2={to.y}
              strokeWidth={1.5}
              strokeDasharray={dashed ? '6 4' : undefined}
              style={{ stroke: isAi ? 'var(--cds-border-interactive)' : 'var(--cds-border-strong)' }}
            />
          );
        })}

      {nodes.map((node) => {
        const point = positions.get(node.id);
        if (!point) return null;
        const selected = node.id === selectedId;
        const onLeft = point.x < centerX;
        return (
          <g key={node.id} className="dep-node" onClick={() => onSelect(node.id)} role="button" tabIndex={0}>
            <circle
              cx={point.x}
              cy={point.y}
              r={selected ? 11 : 8}
              strokeWidth={2}
              style={{
                fill: KIND_FILL[node.kind] ?? KIND_FILL.unknown,
                stroke: selected ? 'var(--cds-text-primary)' : 'transparent',
              }}
            />
            <text
              x={onLeft ? point.x - 14 : point.x + 14}
              y={point.y + 4}
              textAnchor={onLeft ? 'end' : 'start'}
              fontSize={12}
              style={{ fill: 'var(--cds-text-primary)' }}
            >
              {node.name}
            </text>
          </g>
        );
      })}
    </svg>
  );
}
