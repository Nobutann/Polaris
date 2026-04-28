import React from 'react';
import { formatWeightedScore, formatPriorityLevel, formatRiskReason } from '../../utils/enumLabels';
import { PriorityBadge } from '../shared/PriorityBadge';
import { RiskReasonTag } from '../shared/RiskReasonTag';
import { WeightedScoreBar } from '../shared/WeightedScoreBar';

/** Determina cor do kpi-box de prioridade */
function priorityBoxStyle(level) {
  if (!level) return {};
  const l = level.toUpperCase();
  if (l === 'CRITICA') return { borderColor: '#ec4899', background: '#fdf2f8' };
  if (l === 'ALTA')    return { borderColor: '#ef4444', background: '#fff5f5' };
  if (l === 'MEDIA')   return { borderColor: '#f59e0b', background: '#fffbeb' };
  if (l === 'BAIXA')   return { borderColor: '#22c55e', background: '#f0fdf4' };
  return {};
}

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

      {/* Story 8 – Score ponderado com barra visual */}
      <div className="kpi-box" style={{ borderColor: 'var(--sebrae-light-blue)', background: '#f0f5ff' }}>
        <div className="kpi-label" style={{ color: 'var(--sebrae-blue)' }}>Score de Risco Ponderado</div>
        <div className="kpi-value">
          <WeightedScoreBar score={course.weightedRiskScore} showBar={true} />
        </div>
        <div className="kpi-sub" style={{ color: 'var(--text-muted)' }}>
          Escala 0 – 10 · Média dos alunos
        </div>
      </div>

      {/* Story 8 – Nível de prioridade com badge colorido */}
      <div className="kpi-box" style={priorityBoxStyle(course.priorityLevel)}>
        <div className="kpi-label">Nível de Prioridade</div>
        <div className="kpi-value" style={{ fontSize: '1rem', marginTop: '0.5rem' }}>
          <PriorityBadge level={course.priorityLevel} size="md" />
        </div>
        <div className="kpi-sub" style={{ color: 'var(--text-muted)' }}>
          Prioridade predominante
        </div>
      </div>

      {/* Story 8 – Motivo principal com tag contextual */}
      <div className="kpi-box">
        <div className="kpi-label">Principal Motivo de Risco</div>
        <div className="kpi-value" style={{ fontSize: '0.9rem', marginTop: '0.5rem' }}>
          <RiskReasonTag reason={course.mainRiskReason} />
        </div>
        <div className="kpi-sub" style={{ color: 'var(--text-muted)' }}>
          Motivo mais frequente
        </div>
      </div>
    </div>
  );
}
