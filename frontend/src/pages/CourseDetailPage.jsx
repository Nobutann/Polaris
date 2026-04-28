import React from 'react';
import { CourseList } from '../components/courses/CourseList';
import { RiskBadge } from '../components/courses/RiskBadge';
import { KpiRow } from '../components/metrics/KpiRow';
import { RetentionChart } from '../components/metrics/RetentionChart';
import { EvasionTimeline } from '../components/metrics/EvasionTimeline';
import { SignalFeed } from '../components/signals/SignalFeed';
import { StatusBadge } from '../components/shared/StatusBadge';
import { PriorityBadge } from '../components/shared/PriorityBadge';
import { RiskReasonTag } from '../components/shared/RiskReasonTag';
import { WeightedScoreBar } from '../components/shared/WeightedScoreBar';
import { formatRatio } from '../utils/enumLabels';

export function CourseDetailPage({ 
  courses, 
  snapshots, 
  evasionPoints, 
  signals, 
  selectedCourseId, 
  onSelectCourse 
}) {
  const activeCourse = courses.find(c => c.courseId === selectedCourseId);

  if (!activeCourse) return null;

  const courseSignals = signals.filter(s => s.courseId === selectedCourseId);

  return (
    <div className="dashboard-grid">
      <CourseList 
        courses={courses} 
        selectedCourseId={selectedCourseId} 
        onSelectCourse={onSelectCourse} 
      />

      {/* Main Column */}
      <div className="panel" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <div style={{ borderBottom: '1px solid var(--border)', paddingBottom: '1.5rem' }}>
          <div className="insight-subtitle">DETALHAMENTO OPERACIONAL</div>
          <div className="insight-title">
            {activeCourse.courseName || `Curso ${activeCourse.courseId}`}
            <RiskBadge level={activeCourse.riskLevel} />
          </div>
          <div className="insight-desc">{activeCourse.category || 'Geral'} • {activeCourse.totalEnrolled} matriculados</div>
        </div>

        <KpiRow course={activeCourse} />

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginTop: '1rem' }}>
          <RetentionChart snapshots={snapshots} />
          <EvasionTimeline evasionPoints={evasionPoints} />
        </div>
      </div>

      {/* Right Column */}
      <SignalFeed signals={courseSignals.length > 0 ? courseSignals : signals.slice(0, 3)} />

      {/* Bottom Full Row - Tabela */}
      <div className="bottom-grid" style={{ gridColumn: '1 / -1', padding: '0 2rem 2rem 2rem' }}>
        <div className="panel">
          <h3 style={{ marginBottom: '1rem', color: 'var(--sebrae-blue)' }}>
            Alunos do curso (Top {Math.min(10, snapshots.length)})
          </h3>
          <table>
            <thead>
              <tr>
                <th>Usuário</th>
                <th>Risco</th>
                <th>Inatividade</th>
                <th>Freq 30d</th>
                <th>Conclusão</th>
                <th>Profundidade</th>
                <th>Status (Abandono)</th>
                <th style={{ color: 'var(--sebrae-blue)' }}>Score ↕</th>
                <th style={{ color: 'var(--sebrae-blue)' }}>Prioridade</th>
                <th style={{ color: 'var(--sebrae-blue)' }}>Motivo Principal</th>
              </tr>
            </thead>
            <tbody>
              {snapshots.slice(0, 10).map(s => (
                <tr key={s.userId}>
                  <td style={{ fontWeight: 600 }}>#{s.userId}</td>
                  <td>
                    <StatusBadge status={s.riskBand} />
                  </td>
                  <td>{s.daysSinceLastActivity} dias</td>
                  <td>{s.returnFrequency30d} acessos</td>
                  <td>{formatRatio(s.completionRatio)}</td>
                  <td>{formatRatio(s.advanceDepth)}</td>
                  <td>
                    {s.abandonmentStatus === 'ABANDONED' ? (
                      <span style={{color: 'var(--risk-high-text)', fontWeight: 500}}>Abandonou ({s.abandonedAt ? new Date(s.abandonedAt).toLocaleDateString() : '-'})</span>
                    ) : s.abandonmentStatus === 'RETURNED' ? (
                      <span style={{color: 'var(--risk-low-text)', fontWeight: 500}}>Retornou ({s.returnedAt ? new Date(s.returnedAt).toLocaleDateString() : '-'})</span>
                    ) : (
                      <span style={{color: 'var(--text-muted)'}}>Ativo</span>
                    )}
                  </td>
                  {/* Story 8 – Pesagem de Dados */}
                  <td>
                    <WeightedScoreBar score={s.weightedRiskScore} showBar={true} />
                  </td>
                  <td>
                    <PriorityBadge level={s.priorityLevel} size="sm" />
                  </td>
                  <td>
                    <RiskReasonTag reason={s.mainRiskReason} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
