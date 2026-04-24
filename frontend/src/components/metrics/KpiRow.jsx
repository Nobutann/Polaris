import React from 'react';

export function KpiRow({ course }) {
  if (!course) return null;

  return (
    <div className="kpi-row">
      <div className="kpi-box">
        <div className="kpi-label">Índice de Conclusão</div>
        <div className="kpi-value">
          {Math.round(course.avgCompletionRatio * 100)}%
        </div>
        <div className="kpi-sub" style={{ color: 'var(--risk-low-text)' }}>
          <span style={{ fontWeight: 'bold' }}>↑</span> Bom desempenho
        </div>
      </div>
      
      <div className="kpi-box">
        <div className="kpi-label">Status atual</div>
        <div className="kpi-value" style={{ fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.5rem' }}>
          <span style={{ color: course.riskLevel === 'CRÍTICO' || course.riskLevel === 'ALTO' ? 'var(--risk-high-text)' : 'var(--risk-med-text)' }}>●</span> 
          {course.riskLevel === 'CRÍTICO' || course.riskLevel === 'ALTO' ? 'Atenção Crítica' : course.riskLevel === 'MÉDIO' ? 'Em Atenção' : 'Engajado'}
        </div>
        <div className="kpi-sub" style={{ color: 'var(--text-muted)' }}>
          Risco de evasão {course.riskLevel.toLowerCase()}
        </div>
      </div>
      
      <div className="kpi-box">
        <div className="kpi-label">Tendência de Inatividade</div>
        <div className="kpi-value" style={{ fontSize: '1.4rem', color: course.avgDaysInactive > 10 ? 'var(--risk-high-text)' : (course.avgDaysInactive > 5 ? 'var(--risk-med-text)' : 'var(--risk-low-text)') }}>
          {Math.round(course.avgDaysInactive)} dias
        </div>
        <div className="kpi-sub" style={{ color: 'var(--text-muted)' }}>
          Média por aluno
        </div>
      </div>
    </div>
  );
}
