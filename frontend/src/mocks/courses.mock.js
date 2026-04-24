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
    evasionTrend: "subindo"
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
    evasionTrend: "estável"
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
    evasionTrend: "estável"
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
    evasionTrend: "caindo"
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
    evasionTrend: "estável"
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
    evasionTrend: "caindo"
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
    evasionTrend: "caindo"
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
    evasionTrend: "estável"
  }
];
