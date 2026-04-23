import React from 'react';

export function FiltersBar({ onClear }) {
  return (
    <div className="filters-bar">
      <div className="filter-box">
        <label>Período</label>
        <select><option>Últimos 30 dias</option></select>
      </div>
      <div className="filter-box">
        <label>Região</label>
        <select>
          <option>Todas</option>
          <option>São Paulo</option>
          <option>Rio de Janeiro</option>
          <option>Minas Gerais</option>
        </select>
      </div>
      <div className="filter-box" style={{ flex: 1, border: 'none', background: 'transparent' }}></div>
      <button 
        onClick={onClear}
        style={{ 
          background: 'transparent', border: '1px solid var(--border)', 
          padding: '0.5rem 1rem', borderRadius: 6, color: 'var(--sebrae-blue)', 
          fontWeight: 600, cursor: 'pointer' 
        }}>
        ↺ Limpar filtros
      </button>
    </div>
  );
}
