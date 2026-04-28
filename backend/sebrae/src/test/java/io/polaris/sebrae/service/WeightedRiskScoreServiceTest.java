package io.polaris.sebrae.service;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.model.enums.PriorityLevel;
import io.polaris.sebrae.model.enums.RiskReason;
import io.polaris.sebrae.model.enums.SignalWeightKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeightedRiskScoreServiceTest {

    @Mock
    private SignalWeightConfigService signalWeightConfigService;

    private WeightedRiskScoreService service;

    /** Pesos padrão idênticos às constantes hardcoded — preserva comportamento dos 6 testes originais. */
    private static Map<SignalWeightKey, BigDecimal> defaultWeights() {
        Map<SignalWeightKey, BigDecimal> m = new EnumMap<>(SignalWeightKey.class);
        m.put(SignalWeightKey.INACTIVITY,    new BigDecimal("0.40"));
        m.put(SignalWeightKey.CONTINUITY,    new BigDecimal("0.25"));
        m.put(SignalWeightKey.COMPLETION,    new BigDecimal("0.20"));
        m.put(SignalWeightKey.ADVANCE_DEPTH, new BigDecimal("0.15"));
        return m;
    }

    @BeforeEach
    void setUp() {
        when(signalWeightConfigService.getActiveWeightMap()).thenReturn(defaultWeights());
        service = new WeightedRiskScoreService(signalWeightConfigService);
    }

    // -------------------------------------------------------------------------
    // Regressão — 6 testes originais preservados
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnCriticalRiskWhenInactivityIsNullAndOthersAreNull() {
        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(null);
        snapshot.setContinuityRate(null);
        snapshot.setCompletionRatio(null);
        snapshot.setAdvanceDepth(null);

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        assertEquals(new BigDecimal("100.00"), result.getScore());
        assertEquals(PriorityLevel.CRITICA, result.getPriorityLevel());
        assertEquals(RiskReason.INATIVIDADE_ALTA, result.getMainRiskReason());
    }

    @Test
    void shouldReturnLowRiskWhenActiveRecentlyAndOthersArePerfect() {
        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(1);
        snapshot.setContinuityRate(new BigDecimal("1.00"));
        snapshot.setCompletionRatio(new BigDecimal("1.00"));
        snapshot.setAdvanceDepth(new BigDecimal("1.00"));

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        assertEquals(new BigDecimal("0.00"), result.getScore());
        assertEquals(PriorityLevel.BAIXA, result.getPriorityLevel());
    }

    @Test
    void shouldReturnHighRiskWhenOnlyInactivityIsPresent() {
        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(10);
        snapshot.setContinuityRate(null);
        snapshot.setCompletionRatio(null);
        snapshot.setAdvanceDepth(null);

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        assertEquals(new BigDecimal("70.00"), result.getScore());
        assertEquals(PriorityLevel.ALTA, result.getPriorityLevel());
        assertEquals(RiskReason.INATIVIDADE_ALTA, result.getMainRiskReason());
    }

    @Test
    void shouldCalculateScoreWhenContinuityDominates() {
        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(1);
        snapshot.setContinuityRate(new BigDecimal("0.00"));
        snapshot.setCompletionRatio(null);
        snapshot.setAdvanceDepth(null);

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        assertEquals(new BigDecimal("38.46"), result.getScore());
        assertEquals(PriorityLevel.MEDIA, result.getPriorityLevel());
        assertEquals(RiskReason.BAIXA_CONTINUIDADE, result.getMainRiskReason());
    }

    @Test
    void shouldRedistributeWeightsWhenSomeAreNull() {
        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(5); // risk 35
        snapshot.setContinuityRate(null);
        snapshot.setCompletionRatio(null);
        snapshot.setAdvanceDepth(new BigDecimal("0.50")); // risk 50

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        assertEquals(new BigDecimal("39.09"), result.getScore());
        assertEquals(PriorityLevel.MEDIA, result.getPriorityLevel());
        assertEquals(RiskReason.INATIVIDADE_ALTA, result.getMainRiskReason());
    }

    @Test
    void shouldCalculateScoreWhenAllPresentAndAdvanceDominates() {
        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(0);
        snapshot.setContinuityRate(new BigDecimal("1.00"));
        snapshot.setCompletionRatio(new BigDecimal("1.00"));
        snapshot.setAdvanceDepth(new BigDecimal("0.00"));

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        assertEquals(new BigDecimal("15.00"), result.getScore());
        assertEquals(PriorityLevel.BAIXA, result.getPriorityLevel());
        assertEquals(RiskReason.BAIXO_AVANCO, result.getMainRiskReason());
    }

    // -------------------------------------------------------------------------
    // Novos cenários
    // -------------------------------------------------------------------------

    @Test
    void shouldChangesScoreWhenInactivityWeightIncreased() {
        // Apenas INACTIVITY ativo, com peso alto — garante que score == rInactivity == 70 (ALTA)
        Map<SignalWeightKey, BigDecimal> customWeights = new EnumMap<>(SignalWeightKey.class);
        customWeights.put(SignalWeightKey.INACTIVITY, new BigDecimal("0.70"));
        when(signalWeightConfigService.getActiveWeightMap()).thenReturn(customWeights);
        service = new WeightedRiskScoreService(signalWeightConfigService);

        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(10);   // risk 70
        snapshot.setContinuityRate(null);
        snapshot.setCompletionRatio(null);
        snapshot.setAdvanceDepth(null);

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        assertEquals(new BigDecimal("70.00"), result.getScore());
        assertEquals(PriorityLevel.ALTA, result.getPriorityLevel());
        assertEquals(RiskReason.INATIVIDADE_ALTA, result.getMainRiskReason());
    }

    @Test
    void shouldIgnoreDisabledSignalAndRedistributeWeights() {
        // Simula CONTINUITY desabilitado: mapa sem a chave CONTINUITY
        Map<SignalWeightKey, BigDecimal> partialWeights = new EnumMap<>(SignalWeightKey.class);
        partialWeights.put(SignalWeightKey.INACTIVITY,    new BigDecimal("0.40"));
        partialWeights.put(SignalWeightKey.COMPLETION,    new BigDecimal("0.20"));
        partialWeights.put(SignalWeightKey.ADVANCE_DEPTH, new BigDecimal("0.15"));
        when(signalWeightConfigService.getActiveWeightMap()).thenReturn(partialWeights);
        service = new WeightedRiskScoreService(signalWeightConfigService);

        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(1);
        snapshot.setContinuityRate(new BigDecimal("0.00")); // sinal disabled — deve ser ignorado
        snapshot.setCompletionRatio(new BigDecimal("1.00"));
        snapshot.setAdvanceDepth(new BigDecimal("1.00"));

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        // Score deve ser 0 (inactivity=0, completion=0, advance=0; continuity ignorada)
        assertEquals(new BigDecimal("0.00"), result.getScore());
        assertEquals(PriorityLevel.BAIXA, result.getPriorityLevel());
    }

    @Test
    void shouldUseFallbackWhenActiveWeightMapIsEmpty() {
        // Simula banco vazio — service retorna mapa vazio
        when(signalWeightConfigService.getActiveWeightMap()).thenReturn(new EnumMap<>(SignalWeightKey.class));
        service = new WeightedRiskScoreService(signalWeightConfigService);

        CourseMetricSnapshot snapshot = new CourseMetricSnapshot(1L, 100L);
        snapshot.setDaysSinceLastActivity(null); // risco máximo de inatividade
        snapshot.setContinuityRate(null);
        snapshot.setCompletionRatio(null);
        snapshot.setAdvanceDepth(null);

        WeightedRiskScoreService.WeightedRiskResult result = service.calculate(snapshot);

        // Com mapa vazio, nenhum sinal contribui → score 0 e BAIXA (edge case sem sinais)
        assertEquals(new BigDecimal("0.00"), result.getScore());
        assertEquals(PriorityLevel.BAIXA, result.getPriorityLevel());
        assertEquals(RiskReason.SEM_SINAIS_SUFICIENTES, result.getMainRiskReason());
    }
}
