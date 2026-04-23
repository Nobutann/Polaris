const isoNow = new Date().toISOString();

/**
 * @type {import('../types/signal.types').SignalSummary[]}
 */
export const MOCK_SIGNALS = [
  {
    id: "SIG-1001",
    source: "YOUTUBE",
    type: "youtube_low_engagement",
    courseId: 1,
    courseName: "Gestão Financeira para MEI",
    collectedAt: new Date(Date.now() - 2 * 3600000).toISOString(), // 2 hrs ago
    severity: "ALTA",
    description: "Queda brusca de visualizações nos vídeos do módulo de precificação no YouTube."
  },
  {
    id: "SIG-1002",
    source: "INATIVIDADE",
    type: "acesso_recorrente_nulo",
    courseId: 1,
    courseName: "Gestão Financeira para MEI",
    collectedAt: new Date(Date.now() - 5 * 3600000).toISOString(), // 5 hrs ago
    severity: "ALTA",
    description: "Mais de 60% da turma não efetuou login nos últimos 14 dias consecutivos."
  },
  {
    id: "SIG-1003",
    source: "NPS",
    type: "pesquisa_insatisfacao",
    courseId: 2,
    courseName: "Marketing Digital para Pequenos Negócios",
    collectedAt: new Date(Date.now() - 24 * 3600000).toISOString(), // 1 day ago
    severity: "MEDIA",
    description: "Multiplos relatos de dificuldade em acompanhar a carga teórica."
  },
  {
    id: "SIG-1004",
    source: "EVASAO_SISTEMA",
    type: "queda_novas_matriculas",
    courseId: 1,
    courseName: "Gestão Financeira para MEI",
    collectedAt: new Date(Date.now() - 48 * 3600000).toISOString(), // 2 days ago
    severity: "MEDIA",
    description: "Volume de novas matrículas caiu 30% em relação à semana anterior."
  },
  {
    id: "SIG-1005",
    source: "INATIVIDADE",
    type: "acesso_recorrente_baixo",
    courseId: 3,
    courseName: "Empreendedorismo e Inovação",
    collectedAt: new Date(Date.now() - 72 * 3600000).toISOString(), // 3 days ago
    severity: "BAIXA",
    description: "Taxa de retorno semanal abaixo da linha de base para cursos de gestão."
  }
];
