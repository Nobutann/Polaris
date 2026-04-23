import React from 'react';

export function StatusBadge({ status, text }) {
  let colorClass = 'green';
  if (status === 'abandono_provavel' || status === 'risco' || status === 'ALTO' || status === 'CRÍTICO') {
    colorClass = 'red';
  } else if (status === 'atencao' || status === 'MÉDIO') {
    colorClass = 'orange';
  }

  const label = text || (status ? status.replace(/_/g, ' ') : '-');

  return (
    <span className={`badge ${colorClass}`}>
      {label}
    </span>
  );
}
