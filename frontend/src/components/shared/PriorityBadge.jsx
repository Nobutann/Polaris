import React from 'react';
import { formatPriorityLevel } from '../../utils/enumLabels';

const PRIORITY_CONFIG = {
  CRITICA: { colorClass: 'priority-critica', icon: '🔴', dot: true },
  critica: { colorClass: 'priority-critica', icon: '🔴', dot: true },
  ALTA: { colorClass: 'priority-alta', icon: '🟠', dot: true },
  alta: { colorClass: 'priority-alta', icon: '🟠', dot: true },
  MEDIA: { colorClass: 'priority-media', icon: '🟡', dot: true },
  media: { colorClass: 'priority-media', icon: '🟡', dot: true },
  BAIXA: { colorClass: 'priority-baixa', icon: '🟢', dot: true },
  baixa: { colorClass: 'priority-baixa', icon: '🟢', dot: true },
};

/**
 * Badge visual para priorityLevel.
 * @param {{ level: string|null|undefined, size?: 'sm'|'md' }} props
 */
export function PriorityBadge({ level, size = 'md' }) {
  const cfg = PRIORITY_CONFIG[level] ?? null;
  const label = formatPriorityLevel(level);

  if (!cfg) {
    return (
      <span className="priority-badge priority-undefined">
        — {label}
      </span>
    );
  }

  return (
    <span className={`priority-badge ${cfg.colorClass} priority-size-${size}`}>
      <span className="priority-dot" aria-hidden="true" />
      {label}
    </span>
  );
}
