import React from 'react';

export function EmptyState({ message, subMessage }) {
  return (
    <div style={{ padding: '4rem 2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
      <div style={{ fontSize: '3rem', marginBottom: '1rem', opacity: 0.5 }}>📭</div>
      <h3 style={{ marginBottom: '0.5rem', color: 'var(--text-main)' }}>{message || 'Nenhum dado encontrado'}</h3>
      {subMessage && <p style={{ fontSize: '0.9rem' }}>{subMessage}</p>}
    </div>
  );
}
