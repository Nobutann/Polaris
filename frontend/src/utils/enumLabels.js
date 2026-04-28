// src/utils/enumLabels.js
// Normaliza enums vindos do backend (MAIÚSCULO ou minúsculo) em rótulos legíveis.

/** @type {Record<string, string>} */
const PRIORITY_LEVEL_LABELS = {
  BAIXA: 'Baixa',
  baixa: 'Baixa',
  MEDIA: 'Média',
  media: 'Média',
  ALTA: 'Alta',
  alta: 'Alta',
  CRITICA: 'Crítica',
  critica: 'Crítica',
};

/** @type {Record<string, string>} */
const RISK_REASON_LABELS = {
  INATIVIDADE_ALTA: 'Inatividade alta',
  inatividade_alta: 'Inatividade alta',
  BAIXA_CONTINUIDADE: 'Baixa continuidade',
  baixa_continuidade: 'Baixa continuidade',
  BAIXA_CONCLUSAO: 'Baixa conclusão',
  baixa_conclusao: 'Baixa conclusão',
  BAIXO_AVANCO: 'Baixo avanço',
  baixo_avanco: 'Baixo avanço',
  SEM_SINAIS_SUFICIENTES: 'Sem sinais suficientes',
  sem_sinais_suficientes: 'Sem sinais suficientes',
};

/**
 * Retorna o rótulo legível de um priorityLevel vindo do backend.
 * @param {string|null|undefined} raw
 * @returns {string}
 */
export function formatPriorityLevel(raw) {
  if (raw == null || raw === '') return 'Não definida';
  return PRIORITY_LEVEL_LABELS[raw] ?? raw;
}

/**
 * Retorna o rótulo legível de um mainRiskReason vindo do backend.
 * @param {string|null|undefined} raw
 * @returns {string}
 */
export function formatRiskReason(raw) {
  if (raw == null || raw === '') return 'Sem motivo principal';
  return RISK_REASON_LABELS[raw] ?? raw.replace(/_/g, ' ').toLowerCase()
    .replace(/^\w/, c => c.toUpperCase());
}

/**
 * Formata um weightedRiskScore numérico ou BigDecimal (string/number).
 * @param {string|number|null|undefined} raw
 * @returns {string}
 */
export function formatWeightedScore(raw) {
  if (raw == null || raw === '') return 'Não calculado';
  const n = Number(raw);
  if (isNaN(n)) return 'Não calculado';
  return (n / 10).toFixed(1);
}

/**
 * Formata uma razão (0–1) em percentual.
 * Retorna '0%' para zero (não '-') e 'Não calculado' para null/undefined.
 * @param {number|null|undefined} ratio
 * @returns {string}
 */
export function formatRatio(ratio) {
  if (ratio == null) return '-';
  return `${Math.round(ratio * 100)}%`;
}
