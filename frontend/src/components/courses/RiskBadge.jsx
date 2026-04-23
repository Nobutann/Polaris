import React from 'react';

export function RiskBadge({ level }) {
  let colorClass = 'green';
  let icon = '✔';

  if (level === 'CRÍTICO') {
    colorClass = 'red';
    icon = '🚨';
  } else if (level === 'ALTO') {
    colorClass = 'red';
    icon = '⚠';
  } else if (level === 'MÉDIO') {
    colorClass = 'orange';
    icon = '●';
  } else if (level === 'BAIXO') {
    colorClass = 'green';
    icon = '✔';
  }

  return (
    <span className={`badge ${colorClass}`} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
      {icon} {level}
    </span>
  );
}
