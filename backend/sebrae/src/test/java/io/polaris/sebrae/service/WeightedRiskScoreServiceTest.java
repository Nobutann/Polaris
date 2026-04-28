package io.polaris.sebrae.service;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.model.enums.PriorityLevel;
import io.polaris.sebrae.model.enums.RiskReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightedRiskScoreServiceTest {

    private WeightedRiskScoreService service;

    @BeforeEach
    void setUp() {
        service = new WeightedRiskScoreService();
    }

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
}
