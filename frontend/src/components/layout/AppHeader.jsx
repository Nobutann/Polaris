import React from 'react';

export function AppHeader({ onNavigateRelevance }) {
  const isDemo = import.meta.env.VITE_DEMO_MODE === 'true';

  return (
    <header className="top-header">
      <div className="brand">
        <div className="brand-logo">SEBRAE</div>
        <div className="brand-titles">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h1>Polaris</h1>
            {isDemo && (
              <span style={{
                background: '#ca8a04', color: 'white', padding: '2px 6px',
                fontSize: '0.65rem', borderRadius: '4px', fontWeight: 'bold'
              }}>MODO DEMO</span>
            )}
          </div>
          <p>Visão geral de cursos, evasão e risco operacional.</p>
        </div>
      </div>
      <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
        {onNavigateRelevance && (
          <button 
            onClick={onNavigateRelevance}
            style={{
              background: 'transparent', color: 'white', border: '1px solid rgba(255,255,255,0.3)',
              padding: '0.4rem 0.8rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem',
              fontWeight: '600'
            }}
          >
            ⚙️ Relevância
          </button>
        )}
        <span style={{ fontSize: '0.8rem', color: '#a5c0f3' }}>? Notificações</span>
        <div style={{
          width: 32, height: 32, borderRadius: 16, background: 'white',
          color: 'var(--sebrae-blue)', display: 'flex', alignItems: 'center',
          justifyContent: 'center', fontWeight: 'bold', fontSize: '0.8rem'
        }}>
          MC
        </div>
      </div>
    </header>
  );
}
