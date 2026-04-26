import React from 'react';
import { formatRiskReason } from '../../utils/enumLabels';

const REASON_CONFIG = {
  INATIVIDADE_ALTA:        { icon: '💤', colorClass: 'reason-inatividade' },
  inatividade_alta:        { icon: '💤', colorClass: 'reason-inatividade' },
  BAIXA_CONTINUIDADE:      { icon: '🔁', colorClass: 'reason-continuidade' },
  baixa_continuidade:      { icon: '🔁', colorClass: 'reason-continuidade' },
  BAIXA_CONCLUSAO:         { icon: '📋', colorClass: 'reason-conclusao' },
  baixa_conclusao:         { icon: '📋', colorClass: 'reason-conclusao' },
  BAIXO_AVANCO:            { icon: '📉', colorClass: 'reason-avanco' },
  baixo_avanco:            { icon: '📉', colorClass: 'reason-avanco' },
  SEM_SINAIS_SUFICIENTES:  { icon: '❓', colorClass: 'reason-sem-sinais' },
  sem_sinais_suficientes:  { icon: '❓', colorClass: 'reason-sem-sinais' },
};

/**
 * Tag visual para mainRiskReason.
 * @param {{ reason: string|null|undefined }} props
 */
export function RiskReasonTag({ reason }) {
  const cfg = REASON_CONFIG[reason] ?? null;
  const label = formatRiskReason(reason);

  if (!cfg) {
    return <span className="reason-tag reason-undefined">{label}</span>;
  }

  return (
    <span className={`reason-tag ${cfg.colorClass}`}>
      <span className="reason-icon" aria-hidden="true">{cfg.icon}</span>
      {label}
    </span>
  );
}
