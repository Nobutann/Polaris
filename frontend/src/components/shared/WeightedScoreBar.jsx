import React from 'react';
import { formatWeightedScore } from '../../utils/enumLabels';

/**
 * Exibe score ponderado (0–100) com barra de progresso e cor dinâmica.
 * @param {{ score: string|number|null|undefined, showBar?: boolean }} props
 */
export function WeightedScoreBar({ score, showBar = true }) {
  const formatted = formatWeightedScore(score);
  const isCalculated = formatted !== 'Não calculado';
  const n = isCalculated ? Math.min(100, Math.max(0, Number(score))) : null;

  // cor da barra conforme intensidade (0–100)
  let barColor = 'var(--score-low)';
  let textColor = 'var(--score-low-text)';
  if (n !== null) {
    if (n >= 75)      { barColor = 'var(--score-critical)'; textColor = 'var(--score-critical-text)'; }
    else if (n >= 50) { barColor = 'var(--score-high)';     textColor = 'var(--score-high-text)'; }
    else if (n >= 25) { barColor = 'var(--score-med)';      textColor = 'var(--score-med-text)'; }
  }

  const pct = n !== null ? n : 0;

  return (
    <span className="wscore-wrap">
      <span className="wscore-value" style={{ color: isCalculated ? textColor : 'var(--text-muted)' }}>
        {formatted}
      </span>
      {showBar && isCalculated && (
        <span className="wscore-bar-bg" title={`Score: ${formatted}`}>
          <span
            className="wscore-bar-fill"
            style={{ width: `${pct}%`, background: barColor }}
          />
        </span>
      )}
    </span>
  );
}
