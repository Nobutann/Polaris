/**
 * @type {Record<number, import('../types/course.types').CourseMetricSnapshot[]>}
 */

const isoNow = new Date().toISOString();

export const MOCK_SNAPSHOTS = {
  // Gestão Financeira para MEI - CRÍTICO (1)
  1: Array.from({ length: 20 }, (_, i) => {
    // Distribuição: 50% abandono, 20% risco, 20% atenção, 10% engajado
    let riskBand = 'abandono_provavel';
    let inactive = Math.floor(Math.random() * 20) + 14; 
    let completion = Math.random() * 0.3;

    if (i >= 10 && i < 14) { riskBand = 'risco'; inactive = Math.floor(Math.random() * 7) + 7; completion = Math.random() * 0.4 + 0.1; }
    else if (i >= 14 && i < 18) { riskBand = 'atencao'; inactive = Math.floor(Math.random() * 4) + 3; completion = Math.random() * 0.5 + 0.3; }
    else if (i >= 18) { riskBand = 'engajado'; inactive = Math.floor(Math.random() * 2); completion = Math.random() * 0.4 + 0.6; }

    return {
      userId: 1000 + i,
      courseId: 1,
      daysSinceLastActivity: inactive,
      riskBand,
      returnFrequency30d: riskBand === 'engajado' ? 8 : (riskBand === 'atencao' ? 4 : (riskBand === 'risco' ? 1 : 0)),
      continuityRate: Math.random(),
      completionRatio: completion,
      retained7d: inactive <= 7,
      retained14d: inactive <= 14,
      retained30d: inactive <= 30,
      advanceDepth: completion * 1.1 > 1 ? 1 : completion * 1.1,
      lastRelevantActivityAt: new Date(Date.now() - inactive * 86400000).toISOString(),
      calculatedAt: isoNow,
      abandonmentStatus: inactive >= 15 ? 'ABANDONED' : (Math.random() > 0.8 && inactive < 15 ? 'RETURNED' : 'ACTIVE'),
      abandonedAt: inactive >= 15 ? new Date(Date.now() - (inactive - 15) * 86400000).toISOString() : (Math.random() > 0.8 && inactive < 15 ? new Date(Date.now() - 30 * 86400000).toISOString() : null),
      returnedAt: (Math.random() > 0.8 && inactive < 15) ? new Date(Date.now() - inactive * 86400000).toISOString() : null,
      // Story 8
      weightedRiskScore: riskBand === 'abandono_provavel' ? 82.5 : (riskBand === 'risco' ? 60.0 : (riskBand === 'atencao' ? 38.0 : 15.0)),
      priorityLevel: riskBand === 'abandono_provavel' ? 'CRITICA' : (riskBand === 'risco' ? 'ALTA' : (riskBand === 'atencao' ? 'MEDIA' : 'BAIXA')),
      mainRiskReason: riskBand === 'abandono_provavel' ? 'INATIVIDADE_ALTA' : (riskBand === 'risco' ? 'BAIXA_CONTINUIDADE' : (riskBand === 'atencao' ? 'BAIXA_CONCLUSAO' : 'SEM_SINAIS_SUFICIENTES')),
    };
  }),

  // Marketing Digital - ALTO (2)
  2: Array.from({ length: 15 }, (_, i) => {
    let riskBand = i < 6 ? 'abandono_provavel' : (i < 10 ? 'risco' : 'engajado');
    let inactive = riskBand === 'abandono_provavel' ? 15 : (riskBand === 'risco' ? 8 : 2);
    let completion = riskBand === 'engajado' ? 0.7 : 0.3;
    
    return {
      userId: 2000 + i,
      courseId: 2,
      daysSinceLastActivity: inactive,
      riskBand,
      returnFrequency30d: riskBand === 'engajado' ? 6 : 1,
      continuityRate: Math.random(),
      completionRatio: completion,
      retained7d: inactive <= 7,
      retained14d: inactive <= 14,
      retained30d: inactive <= 30,
      advanceDepth: completion,
      lastRelevantActivityAt: new Date(Date.now() - inactive * 86400000).toISOString(),
      calculatedAt: isoNow,
      abandonmentStatus: riskBand === 'abandono_provavel' ? 'ABANDONED' : 'ACTIVE',
      abandonedAt: riskBand === 'abandono_provavel' ? new Date().toISOString() : null,
      returnedAt: null,
      // Story 8
      weightedRiskScore: riskBand === 'abandono_provavel' ? 78.0 : (riskBand === 'risco' ? 55.0 : 20.0),
      priorityLevel: riskBand === 'abandono_provavel' ? 'ALTA' : (riskBand === 'risco' ? 'MEDIA' : 'BAIXA'),
      mainRiskReason: riskBand === 'abandono_provavel' ? 'BAIXO_AVANCO' : (riskBand === 'risco' ? 'BAIXA_CONTINUIDADE' : 'SEM_SINAIS_SUFICIENTES'),
    };
  }),
  
  // Empreendedorismo (3) - MÉDIO
  3: Array.from({ length: 10 }, (_, i) => ({
      userId: 3000 + i,
      courseId: 3,
      daysSinceLastActivity: i < 2 ? 14 : i < 6 ? 5 : 1,
      riskBand: i < 2 ? 'risco' : i < 6 ? 'atencao' : 'engajado',
      returnFrequency30d: i < 2 ? 1 : 5,
      continuityRate: 0.6,
      completionRatio: i < 2 ? 0.2 : 0.7,
      retained7d: true,
      retained14d: true,
      retained30d: true,
      advanceDepth: 0.6,
      lastRelevantActivityAt: isoNow,
      calculatedAt: isoNow,
      abandonmentStatus: 'ACTIVE',
      abandonedAt: null,
      returnedAt: null,
      // Story 8
      weightedRiskScore: i < 2 ? 52.0 : (i < 6 ? 32.0 : 10.0),
      priorityLevel: i < 2 ? 'MEDIA' : (i < 6 ? 'BAIXA' : 'BAIXA'),
      mainRiskReason: i < 2 ? 'BAIXA_CONCLUSAO' : 'SEM_SINAIS_SUFICIENTES',
  }))
};
