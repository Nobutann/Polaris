/**
 * @typedef {Object} SignalSummary
 * @property {string} [id] - ID opcional (gerado no front ou backend)
 * @property {('YOUTUBE'|'NPS'|'INATIVIDADE'|'EVASAO_SISTEMA')} source - Origem do sinal
 * @property {string} type - Tipo de sinal
 * @property {number} courseId - ID do curso relacionado
 * @property {string} courseName - Nome do curso relacionado
 * @property {string} collectedAt - Data/hora da coleta (ISO)
 * @property {('ALTA'|'MEDIA'|'BAIXA')} severity - Severidade do alerta
 * @property {string} description - Descrição amigável do sinal
 */
