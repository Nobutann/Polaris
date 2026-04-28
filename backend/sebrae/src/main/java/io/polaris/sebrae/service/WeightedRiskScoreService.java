package io.polaris.sebrae.service;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.model.enums.PriorityLevel;
import io.polaris.sebrae.model.enums.RiskReason;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeightedRiskScoreService {

    private static final BigDecimal WEIGHT_INACTIVITY = new BigDecimal("0.40");
    private static final BigDecimal WEIGHT_CONTINUITY = new BigDecimal("0.25");
    private static final BigDecimal WEIGHT_COMPLETION = new BigDecimal("0.20");
    private static final BigDecimal WEIGHT_ADVANCE = new BigDecimal("0.15");

    public WeightedRiskResult calculate(CourseMetricSnapshot snapshot) {
        BigDecimal rInactivity = calculateInactivityRisk(snapshot.getDaysSinceLastActivity());
        
        BigDecimal rContinuity = null;
        if (snapshot.getContinuityRate() != null) {
            rContinuity = BigDecimal.ONE.subtract(snapshot.getContinuityRate()).multiply(new BigDecimal("100"));
        }
        
        BigDecimal rCompletion = null;
        if (snapshot.getCompletionRatio() != null) {
            rCompletion = BigDecimal.ONE.subtract(snapshot.getCompletionRatio()).multiply(new BigDecimal("100"));
        }
        
        BigDecimal rAdvance = null;
        if (snapshot.getAdvanceDepth() != null) {
            rAdvance = BigDecimal.ONE.subtract(snapshot.getAdvanceDepth()).multiply(new BigDecimal("100"));
        }

        BigDecimal totalWeight = WEIGHT_INACTIVITY;
        if (rContinuity != null) totalWeight = totalWeight.add(WEIGHT_CONTINUITY);
        if (rCompletion != null) totalWeight = totalWeight.add(WEIGHT_COMPLETION);
        if (rAdvance != null) totalWeight = totalWeight.add(WEIGHT_ADVANCE);

        BigDecimal normalizedWeightInactivity = WEIGHT_INACTIVITY.divide(totalWeight, 4, RoundingMode.HALF_UP);
        BigDecimal normalizedWeightContinuity = rContinuity != null ? WEIGHT_CONTINUITY.divide(totalWeight, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal normalizedWeightCompletion = rCompletion != null ? WEIGHT_COMPLETION.divide(totalWeight, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal normalizedWeightAdvance = rAdvance != null ? WEIGHT_ADVANCE.divide(totalWeight, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        BigDecimal score = rInactivity.multiply(normalizedWeightInactivity);
        if (rContinuity != null) score = score.add(rContinuity.multiply(normalizedWeightContinuity));
        if (rCompletion != null) score = score.add(rCompletion.multiply(normalizedWeightCompletion));
        if (rAdvance != null) score = score.add(rAdvance.multiply(normalizedWeightAdvance));
        
        score = score.setScale(2, RoundingMode.HALF_UP);

        PriorityLevel priorityLevel;
        if (score.compareTo(new BigDecimal("25")) < 0) priorityLevel = PriorityLevel.BAIXA;
        else if (score.compareTo(new BigDecimal("50")) < 0) priorityLevel = PriorityLevel.MEDIA;
        else if (score.compareTo(new BigDecimal("75")) < 0) priorityLevel = PriorityLevel.ALTA;
        else priorityLevel = PriorityLevel.CRITICA;

        RiskReason mainReason = RiskReason.SEM_SINAIS_SUFICIENTES;
        BigDecimal maxContribution = BigDecimal.ZERO;

        Map<RiskReason, BigDecimal> contributions = new HashMap<>();
        contributions.put(RiskReason.INATIVIDADE_ALTA, rInactivity.multiply(normalizedWeightInactivity));
        if (rContinuity != null) contributions.put(RiskReason.BAIXA_CONTINUIDADE, rContinuity.multiply(normalizedWeightContinuity));
        if (rCompletion != null) contributions.put(RiskReason.BAIXA_CONCLUSAO, rCompletion.multiply(normalizedWeightCompletion));
        if (rAdvance != null) contributions.put(RiskReason.BAIXO_AVANCO, rAdvance.multiply(normalizedWeightAdvance));

        for (Map.Entry<RiskReason, BigDecimal> entry : contributions.entrySet()) {
            if (entry.getValue().compareTo(maxContribution) > 0) {
                maxContribution = entry.getValue();
                mainReason = entry.getKey();
            }
        }

        return new WeightedRiskResult(score, priorityLevel, mainReason);
    }

    private BigDecimal calculateInactivityRisk(Integer daysSinceLastActivity) {
        if (daysSinceLastActivity == null) return new BigDecimal("100");
        if (daysSinceLastActivity <= 2) return BigDecimal.ZERO;
        if (daysSinceLastActivity <= 6) return new BigDecimal("35");
        if (daysSinceLastActivity <= 14) return new BigDecimal("70");
        return new BigDecimal("100");
    }

    public static class WeightedRiskResult {
        private final BigDecimal score;
        private final PriorityLevel priorityLevel;
        private final RiskReason mainRiskReason;

        public WeightedRiskResult(BigDecimal score, PriorityLevel priorityLevel, RiskReason mainRiskReason) {
            this.score = score;
            this.priorityLevel = priorityLevel;
            this.mainRiskReason = mainRiskReason;
        }

        public BigDecimal getScore() { return score; }
        public PriorityLevel getPriorityLevel() { return priorityLevel; }
        public RiskReason getMainRiskReason() { return mainRiskReason; }
    }
}
