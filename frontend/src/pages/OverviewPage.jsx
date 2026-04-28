import React, { useMemo } from 'react';
import { CourseList } from '../components/courses/CourseList';
import { SignalFeed } from '../components/signals/SignalFeed';
import { RiskDistribution } from '../components/metrics/RiskDistribution';

export function OverviewPage({ courses, signals, selectedCourseId, onSelectCourse }) {
  const kpis = useMemo(() => {
    if (!courses.length) return { total: 0, highRiskPct: 0, avgComp: 0 };
    
    let highRiskCourses = 0;
    let compSum = 0;
    
    courses.forEach(c => {
      if (c.riskLevel === 'ALTO') highRiskCourses++;
      compSum += c.avgCompletionRatio;
    });

    return {
      total: courses.length,
      highRiskPct: courses.length > 0 ? Math.round((highRiskCourses / courses.length) * 100) : 0,
      avgComp: Math.round((compSum / courses.length) * 100)
    };
  }, [courses]);

  return (
    <div className="dashboard-grid">
      <CourseList 
        courses={courses} 
        selectedCourseId={selectedCourseId} 
        onSelectCourse={onSelectCourse} 
      />

      <div className="panel" style={{ display: 'flex', flexDirection: 'column' }}>
        <h2 className="panel-header">Visão Estratégica Geral</h2>
        
        <div className="kpi-row" style={{ gridTemplateColumns: 'repeat(2, 1fr)', marginBottom: '2rem' }}>
          <div className="kpi-box" style={{ textAlign: 'center', background: '#eff6ff', border: 'none' }}>
            <div className="kpi-label">Cursos Monitorados</div>
            <div className="kpi-value" style={{ color: 'var(--sebrae-blue)' }}>{kpis.total}</div>
          </div>
          <div className="kpi-box" style={{ textAlign: 'center', background: '#fee2e2', border: 'none' }}>
            <div className="kpi-label">Cursos em Risco Crítico/Alto</div>
            <div className="kpi-value" style={{ color: 'var(--risk-high-text)' }}>{kpis.highRiskPct}%</div>
          </div>
        </div>

        <RiskDistribution courses={courses} />

        <div style={{ marginTop: 'auto', background: '#f8fafc', padding: '1.5rem', borderRadius: 8, border: '1px solid var(--border)' }}>
          <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.5rem' }}>DICA OPERACIONAL</div>
          <p style={{ fontSize: '0.9rem' }}>
            Selecione um curso na lista lateral para analisar profundamente as métricas de retenção, gargalos de evasão e usuários em risco. Cursos classificados como <strong>CRÍTICO</strong> demandam intervenção imediata.
          </p>
        </div>
      </div>

      <SignalFeed signals={signals} />
    </div>
  );
}
