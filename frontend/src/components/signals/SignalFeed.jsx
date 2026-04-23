import React from 'react';
import { SignalCard } from './SignalCard';
import { EmptyState } from '../shared/EmptyState';

export function SignalFeed({ signals }) {
  if (!signals || signals.length === 0) {
    return <EmptyState message="Sem sinais recentes" subMessage="Tudo parece calmo no momento." />;
  }

  return (
    <div className="panel" style={{ padding: '1.5rem 1rem' }}>
      <h3 style={{ fontSize: '1.1rem', marginBottom: '0.2rem' }}>Sinais e Alertas</h3>
      <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>Monitoramento contínuo</p>

      {signals.map((signal, index) => (
        <SignalCard
          key={`signal-${index}-${signal.id ?? ''}-${signal.description ?? ''}`}
          signal={signal}
        />
      ))}
    </div>
  );
}
