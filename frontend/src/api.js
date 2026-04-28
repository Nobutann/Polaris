// src/api.js

// ---------------------------------------------------------------------------
// Internal auth headers
// ---------------------------------------------------------------------------
// Valores lidos exclusivamente das variáveis de ambiente do Vite.
// Nunca hardcode tokens aqui. Configure VITE_INTERNAL_TOKEN e
// VITE_INTERNAL_SOURCE no arquivo .env local (nunca no repositório).
// ---------------------------------------------------------------------------

/**
 * Monta os headers obrigatórios de autenticação interna exigidos pelo backend.
 * Centralizar aqui garante que nenhuma chamada real fique sem autenticação.
 * @returns {HeadersInit}
 */
function _getAuthHeaders() {
    return {
        'Content-Type': 'application/json',
        'X-Internal-Token': import.meta.env.VITE_INTERNAL_TOKEN ?? '',
        'X-Internal-Source': import.meta.env.VITE_INTERNAL_SOURCE ?? '',
    };
}

/**
 * Normaliza respostas da API que podem vir em formatos diferentes.
 * Aceita:
 * - array direto
 * - objeto com content[]
 * - objeto com data[]
 * Lança erro explícito se o formato não for reconhecido.
 * @param {unknown} raw
 * @param {string} context
 * @returns {Array}
 */
function _normalizeArrayResponse(raw, context) {
    if (Array.isArray(raw)) {
        return raw;
    }

    if (Array.isArray(raw?.content)) {
        return raw.content;
    }

    if (Array.isArray(raw?.data)) {
        return raw.data;
    }

    console.error(`Formato inesperado em ${context}:`, raw);
    throw {
        status: 500,
        message: `Resposta inválida da API em ${context}`,
    };
}

export async function login(email, password) {
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
        throw {
            status: response.status,
            message: response.status === 401 ? 'Credenciais inválidas.' : `Erro ao autenticar: ${response.statusText}`,
        };
    }

    return await response.json();
}

// ---------------------------------------------------------------------------
// API calls
// ---------------------------------------------------------------------------


export async function fetchCourseSnapshots(courseId) {
    const res = await fetch(`/api/metrics/courses/${courseId}`, {
        headers: _getAuthHeaders(),
    });

    if (!res.ok) {
        throw {
            status: res.status,
            message: `Erro ao buscar snapshots do curso: ${res.statusText}`,
        };
    }

    const raw = await res.json();
    return _normalizeArrayResponse(raw, 'fetchCourseSnapshots');
}

export async function fetchUserSnapshot(courseId, userId) {
    const res = await fetch(`/api/metrics/courses/${courseId}/users/${userId}`, {
        headers: _getAuthHeaders(),
    });

    if (!res.ok) {
        throw {
            status: res.status,
            message: `Erro ao buscar métricas do usuário: ${res.statusText}`,
        };
    }

    return await res.json();
}

export async function fetchEvasionPoints(courseId) {
    const res = await fetch(`/api/metrics/courses/${courseId}/evasion-points`, {
        headers: _getAuthHeaders(),
    });

    if (!res.ok) {
        throw {
            status: res.status,
            message: `Erro ao buscar pontos de evasão: ${res.statusText}`,
        };
    }

    return await res.json();
}

export async function fetchAllGlobalSnapshots() {
    const res = await fetch(`/api/metrics/courses`, {
        headers: _getAuthHeaders(),
    });

    if (!res.ok) {
        throw {
            status: res.status,
            message: `Erro ao buscar snapshots globais: ${res.statusText}`,
        };
    }

    return await res.json();
}

/**
 * Função que busca snapshots globais e faz a adaptação consolidada
 * para ser consumida pela UI de Cursos.
 */
export async function fetchCoursesAggregated() {
    const raw = await fetchAllGlobalSnapshots();
    const snapshots = _normalizeArrayResponse(raw, 'fetchCoursesAggregated');

    const map = {};

    snapshots.forEach(s => {
        const c = s.courseId;

        if (map[c] == null) {
            map[c] = {
                courseId: c,
                // courseName não vem do endpoint global; usa fallback
                courseName: s.courseName || `Curso ${c}`,
                totalEnrolled: 0,
                highRiskCount: 0,
                medRiskCount: 0,
                lowRiskCount: 0,
                sumCompletion: 0,
                sumDaysInactive: 0,
                // Story 8
                sumWeightedScore: 0,
                countWeightedScore: 0,
                priorityLevelCounts: {},
                mainRiskReasonCounts: {},
            };
        }

        map[c].totalEnrolled++;

        if (s.riskBand === 'abandono_provavel' || s.riskBand === 'risco') {
            map[c].highRiskCount++;
        } else if (s.riskBand === 'atencao') {
            map[c].medRiskCount++;
        } else {
            map[c].lowRiskCount++;
        }

        if (s.completionRatio != null) {
            map[c].sumCompletion += Number(s.completionRatio);
        }

        if (s.daysSinceLastActivity != null) {
            map[c].sumDaysInactive += s.daysSinceLastActivity;
        }

        // Story 8 – agregação por curso
        if (s.weightedRiskScore != null) {
            map[c].sumWeightedScore += Number(s.weightedRiskScore);
            map[c].countWeightedScore++;
        }
        if (s.priorityLevel) {
            map[c].priorityLevelCounts[s.priorityLevel] =
                (map[c].priorityLevelCounts[s.priorityLevel] || 0) + 1;
        }
        if (s.mainRiskReason) {
            map[c].mainRiskReasonCounts[s.mainRiskReason] =
                (map[c].mainRiskReasonCounts[s.mainRiskReason] || 0) + 1;
        }
    });

    return Object.values(map)
        .map(c => {
            const riskRatio = c.totalEnrolled > 0 ? c.highRiskCount / c.totalEnrolled : 0;

            let riskLevel = 'BAIXO';

            if (riskRatio > 0.4) {
                riskLevel = 'ALTO';
            } else if (riskRatio > 0.15) {
                riskLevel = 'MÉDIO';
            }

            // Story 8 – derivação dos campos agregados por curso
            const avgWeightedScore =
                c.countWeightedScore > 0
                    ? c.sumWeightedScore / c.countWeightedScore
                    : null;

            /** Retorna a chave com maior contagem num objeto {key: count} */
            const modeOf = (counts) => {
                const entries = Object.entries(counts);
                if (!entries.length) return null;
                return entries.reduce((best, cur) =>
                    cur[1] > best[1] ? cur : best
                )[0];
            };

            return {
                courseId: c.courseId,
                courseName: c.courseName,
                category: 'Geral',
                totalEnrolled: c.totalEnrolled,
                highRiskCount: c.highRiskCount,
                medRiskCount: c.medRiskCount,
                lowRiskCount: c.lowRiskCount,
                avgCompletionRatio: c.totalEnrolled > 0 ? c.sumCompletion / c.totalEnrolled : 0,
                avgDaysInactive: c.totalEnrolled > 0 ? c.sumDaysInactive / c.totalEnrolled : 0,
                riskLevel: riskLevel,
                evasionTrend: 'estável',
                // Story 8
                weightedRiskScore: avgWeightedScore,
                priorityLevel: modeOf(c.priorityLevelCounts),
                mainRiskReason: modeOf(c.mainRiskReasonCounts),
            };
        })
        .sort((a, b) => b.highRiskCount - a.highRiskCount);
}

export async function fetchSignals(courseId) {
    try {
        let url = `/api/signals`;

        if (courseId) {
            url += `?courseId=${courseId}`;
        }

        const res = await fetch(url, {
            headers: _getAuthHeaders(),
        });

        if (!res.ok) {
            return [];
        }

        const raw = await res.json();
        return _normalizeArrayResponse(raw, 'fetchSignals');
    } catch (err) {
        return [];
    }
}