/**
 * @type {Record<number, import('../types/evasion.types').EvasionPoint[]>}
 */
export const MOCK_EVASION_POINTS = {
  1: [
    { courseId: 1, lessonId: 1, lessonTitle: "Introdução ao MEI", evasionCount: 5, evasionRate: 1.5 },
    { courseId: 1, lessonId: 2, lessonTitle: "Separando Contas Pessoais", evasionCount: 12, evasionRate: 3.5 },
    { courseId: 1, lessonId: 3, lessonTitle: "Plano de Contas Básico", evasionCount: 85, evasionRate: 25.0 }, // Gargalo
    { courseId: 1, lessonId: 4, lessonTitle: "Precificação e Custos", evasionCount: 65, evasionRate: 19.1 }, // Alto tbm
    { courseId: 1, lessonId: 5, lessonTitle: "Impostos e DAS", evasionCount: 20, evasionRate: 5.8 },
    { courseId: 1, lessonId: 6, lessonTitle: "Fluxo de Caixa na Prática", evasionCount: 15, evasionRate: 4.4 }
  ],
  2: [
    { courseId: 2, lessonId: 1, lessonTitle: "O que é Inbound Marketing", evasionCount: 10, evasionRate: 3.5 },
    { courseId: 2, lessonId: 2, lessonTitle: "Criando sua Persona", evasionCount: 45, evasionRate: 16.0 }, // Gargalo
    { courseId: 2, lessonId: 3, lessonTitle: "Presença no Instagram", evasionCount: 15, evasionRate: 5.3 }
  ],
  3: [
    { courseId: 3, lessonId: 1, lessonTitle: "Visão Empreendedora", evasionCount: 2, evasionRate: 1.3 },
    { courseId: 3, lessonId: 2, lessonTitle: "Design Thinking", evasionCount: 8, evasionRate: 5.3 }
  ]
};
