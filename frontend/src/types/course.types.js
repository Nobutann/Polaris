/**
 * @typedef {Object} CourseMetricSnapshot
 * @property {number} userId - ID do usuário (aluno)
 * @property {number} courseId - ID do curso
 * @property {number} daysSinceLastActivity - Dias desde a última atividade relevante
 * @property {('engajado'|'atencao'|'risco'|'abandono_provavel')} riskBand - Faixa de risco calculada
 * @property {number} returnFrequency30d - Frequência de retorno nos últimos 30 dias
 * @property {number} continuityRate - Taxa de continuidade (0.0 a 1.0)
 * @property {number} completionRatio - Proporção de conclusão (0.0 a 1.0)
 * @property {boolean} retained7d - Retenção em 7 dias
 * @property {boolean} retained14d - Retenção em 14 dias
 * @property {boolean} retained30d - Retenção em 30 dias
 * @property {number} advanceDepth - Profundidade de avanço (0.0 a 1.0)
 * @property {string} lastRelevantActivityAt - Data-hora ISO da última atividade
 * @property {string} calculatedAt - Data-hora ISO do cálculo
 */

/**
 * @typedef {Object} CourseAggregate
 * @property {number} courseId - ID do curso
 * @property {string} courseName - Nome do curso (adicionado para o front)
 * @property {string} category - Categoria (adicionado para o front)
 * @property {number} totalEnrolled - Total de matriculados
 * @property {number} highRiskCount - Quantidade em risco alto (risco + abandono)
 * @property {number} medRiskCount - Quantidade em risco médio (atenção)
 * @property {number} lowRiskCount - Quantidade em risco baixo (engajado)
 * @property {number} avgCompletionRatio - Média de conclusão (0.0 a 1.0)
 * @property {number} avgDaysInactive - Média de dias de inatividade
 * @property {('BAIXO'|'MÉDIO'|'ALTO'|'CRÍTICO')} riskLevel - Nível global de risco do curso
 * @property {string} evasionTrend - Tendência de evasão (e.g., 'subindo', 'estável', 'caindo')
 */
