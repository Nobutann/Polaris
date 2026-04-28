/**
 * @type {import('../types/course.types').CourseAggregate[]}
 */
export const MOCK_COURSES = [
  {
    courseId: 1,
    courseName: "Gestão Financeira para MEI",
    category: "Finanças",
    totalEnrolled: 340,
    highRiskCount: 204, // ~60%
    medRiskCount: 68,   // ~20%
    lowRiskCount: 68,   // ~20%
    avgCompletionRatio: 0.32,
    avgDaysInactive: 14,
    riskLevel: "CRÍTICO",
    evasionTrend: "subindo",
    // Story 8
    weightedRiskScore: 79.4,
    priorityLevel: "CRITICA",
    mainRiskReason: "INATIVIDADE_ALTA",
  },
  {
    courseId: 2,
    courseName: "Marketing Digital para Pequenos Negócios",
    category: "Marketing",
    totalEnrolled: 280,
    highRiskCount: 112, // 40%
    medRiskCount: 84,   // 30%
    lowRiskCount: 84,   // 30%
    avgCompletionRatio: 0.45,
    avgDaysInactive: 8,
    riskLevel: "ALTO",
    evasionTrend: "estável",
    // Story 8
    weightedRiskScore: 61.2,
    priorityLevel: "ALTA",
    mainRiskReason: "BAIXO_AVANCO",
  },
  {
    courseId: 3,
    courseName: "Empreendedorismo e Inovação",
    category: "Gestão",
    totalEnrolled: 150,
    highRiskCount: 22,
    medRiskCount: 53,
    lowRiskCount: 75,
    avgCompletionRatio: 0.61,
    avgDaysInactive: 5,
    riskLevel: "MÉDIO",
    evasionTrend: "estável",
    // Story 8
    weightedRiskScore: 34.8,
    priorityLevel: "MEDIA",
    mainRiskReason: "BAIXA_CONCLUSAO",
  },
  {
    courseId: 4,
    courseName: "Atendimento ao Cliente",
    category: "Vendas",
    totalEnrolled: 190,
    highRiskCount: 30,
    medRiskCount: 60,
    lowRiskCount: 100,
    avgCompletionRatio: 0.70,
    avgDaysInactive: 4,
    riskLevel: "MÉDIO",
    evasionTrend: "caindo",
    // Story 8
    weightedRiskScore: 28.1,
    priorityLevel: "MEDIA",
    mainRiskReason: "BAIXA_CONTINUIDADE",
  },
  {
    courseId: 5,
    courseName: "Planejamento Estratégico",
    category: "Gestão",
    totalEnrolled: 210,
    highRiskCount: 40,
    medRiskCount: 70,
    lowRiskCount: 100,
    avgCompletionRatio: 0.58,
    avgDaysInactive: 6,
    riskLevel: "MÉDIO",
    evasionTrend: "estável",
    // Story 8
    weightedRiskScore: 31.5,
    priorityLevel: "MEDIA",
    mainRiskReason: "BAIXA_CONCLUSAO",
  },
  {
    courseId: 6,
    courseName: "Vendas e Negociação",
    category: "Vendas",
    totalEnrolled: 420,
    highRiskCount: 42,
    medRiskCount: 84,
    lowRiskCount: 294,
    avgCompletionRatio: 0.85,
    avgDaysInactive: 2,
    riskLevel: "BAIXO",
    evasionTrend: "caindo",
    // Story 8
    weightedRiskScore: 11.3,
    priorityLevel: "BAIXA",
    mainRiskReason: "SEM_SINAIS_SUFICIENTES",
  },
  {
    courseId: 7,
    courseName: "Educação Fiscal",
    category: "Finanças",
    totalEnrolled: 120,
    highRiskCount: 10,
    medRiskCount: 20,
    lowRiskCount: 90,
    avgCompletionRatio: 0.88,
    avgDaysInactive: 2,
    riskLevel: "BAIXO",
    evasionTrend: "caindo",
    // Story 8
    weightedRiskScore: 9.7,
    priorityLevel: "BAIXA",
    mainRiskReason: "SEM_SINAIS_SUFICIENTES",
  },
  {
    courseId: 8,
    courseName: "Logística e Operações",
    category: "Operações",
    totalEnrolled: 160,
    highRiskCount: 15,
    medRiskCount: 25,
    lowRiskCount: 120,
    avgCompletionRatio: 0.82,
    avgDaysInactive: 3,
    riskLevel: "BAIXO",
    evasionTrend: "estável",
    // Story 8
    weightedRiskScore: 12.0,
    priorityLevel: "BAIXA",
    mainRiskReason: "SEM_SINAIS_SUFICIENTES",
  }
];
