import React from 'react';

export function SignalCard({ signal }) {
  let colorClass = 'blue';
  let icon = 'ℹ';

  if (signal.severity === 'ALTA') {
    colorClass = ''; // Default in css is red/pink for high severity inside signal-card (without class, or you can add custom)
  } else if (signal.severity === 'MEDIA') {
    colorClass = 'yellow';
  }

  if (signal.source === 'YOUTUBE') icon = '▶';
  else if (signal.source === 'INATIVIDADE') icon = '↘';
  else if (signal.source === 'NPS') icon = '💬';
  else if (signal.source === 'EVASAO_SISTEMA') icon = '🚪';

  // Format date relative (simple version)
  const d = new Date(signal.collectedAt);
  const diffHours = Math.round((Date.now() - d.getTime()) / 3600000);
  let timeStr = `há ${diffHours} horas`;
  if (diffHours > 24) timeStr = `há ${Math.round(diffHours / 24)} dias`;
  if (diffHours === 0) timeStr = `agora mesmo`;

  return (
    <div className={`signal-card ${colorClass}`}>
      <div className="signal-icon">{icon}</div>
      <div className="signal-text">
        <h4>{signal.description}</h4>
        <p>Fonte: {signal.source} • {timeStr}</p>
      </div>
      <div className={`signal-val ${signal.severity === 'ALTA' ? 'bad' : ''}`}>
        {signal.severity}
      </div>
    </div>
  );
}
