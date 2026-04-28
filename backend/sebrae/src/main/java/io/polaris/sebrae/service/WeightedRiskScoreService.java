package io.polaris.sebrae.service;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.model.enums.PriorityLevel;
import io.polaris.sebrae.model.enums.RiskReason;
import io.polaris.sebrae.model.enums.SignalWeightKey;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeightedRiskScoreService {

    // Pesos padrão — usados como fallback se SignalWeightConfigService retornar mapa vazio
    private static final BigDecimal DEFAULT_WEIGHT_INACTIVITY = new BigDecimal("0.40");
    private static final BigDecimal DEFAULT_WEIGHT_CONTINUITY = new BigDecimal("0.25");
    private static final BigDecimal DEFAULT_WEIGHT_COMPLETION  = new BigDecimal("0.20");
    private static final BigDecimal DEFAULT_WEIGHT_ADVANCE     = new BigDecimal("0.15");

    private final SignalWeightConfigService signalWeightConfigService;

    public WeightedRiskScoreService(SignalWeightConfigService signalWeightConfigService) {
        this.signalWeightConfigService = signalWeightConfigService;
    }

    public WeightedRiskResult calculate(CourseMetricSnapshot snapshot) {

        // Carrega pesos dinâmicos; já inclui fallback interno em caso de falha
        Map<SignalWeightKey, BigDecimal> activeWeights = signalWeightConfigService.getActiveWeightMap();

        BigDecimal wInactivity = activeWeights.getOrDefault(SignalWeightKey.INACTIVITY,    DEFAULT_WEIGHT_INACTIVITY);
        BigDecimal wContinuity = activeWeights.getOrDefault(SignalWeightKey.CONTINUITY,    DEFAULT_WEIGHT_CONTINUITY);
        BigDecimal wCompletion = activeWeights.getOrDefault(SignalWeightKey.COMPLETION,    DEFAULT_WEIGHT_COMPLETION);
        BigDecimal wAdvance    = activeWeights.getOrDefault(SignalWeightKey.ADVANCE_DEPTH, DEFAULT_WEIGHT_ADVANCE);

        // Se INACTIVITY está desabilitado (ausente do mapa), usa zero — não contribui
        boolean inactivityActive = activeWeights.containsKey(SignalWeightKey.INACTIVITY);

        BigDecimal rInactivity = inactivityActive
                ? calculateInactivityRisk(snapshot.getDaysSinceLastActivity())
                : null;

        BigDecimal rContinuity = null;
        if (activeWeights.containsKey(SignalWeightKey.CONTINUITY) && snapshot.getContinuityRate() != null) {
            rContinuity = BigDecimal.ONE.subtract(snapshot.getContinuityRate()).multiply(new BigDecimal("100"));
        }

        BigDecimal rCompletion = null;
        if (activeWeights.containsKey(SignalWeightKey.COMPLETION) && snapshot.getCompletionRatio() != null) {
            rCompletion = BigDecimal.ONE.subtract(snapshot.getCompletionRatio()).multiply(new BigDecimal("100"));
        }

        BigDecimal rAdvance = null;
        if (activeWeights.containsKey(SignalWeightKey.ADVANCE_DEPTH) && snapshot.getAdvanceDepth() != null) {
            rAdvance = BigDecimal.ONE.subtract(snapshot.getAdvanceDepth()).multiply(new BigDecimal("100"));
        }

        // Acumula totalWeight apenas para sinais ativos com valor não-nulo
        BigDecimal totalWeight = BigDecimal.ZERO;
        if (rInactivity != null) totalWeight = totalWeight.add(wInactivity);
        if (rContinuity != null) totalWeight = totalWeight.add(wContinuity);
        if (rCompletion != null) totalWeight = totalWeight.add(wCompletion);
        if (rAdvance    != null) totalWeight = totalWeight.add(wAdvance);

        // Se nenhum sinal produz valor, score = 0
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return new WeightedRiskResult(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    PriorityLevel.BAIXA, RiskReason.SEM_SINAIS_SUFICIENTES);
        }

        BigDecimal normInactivity = rInactivity != null ? wInactivity.divide(totalWeight, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal normContinuity = rContinuity != null ? wContinuity.divide(totalWeight, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal normCompletion = rCompletion != null ? wCompletion.divide(totalWeight, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal normAdvance    = rAdvance    != null ? wAdvance.divide(totalWeight, 4, RoundingMode.HALF_UP)    : BigDecimal.ZERO;

        BigDecimal score = BigDecimal.ZERO;
        if (rInactivity != null) score = score.add(rInactivity.multiply(normInactivity));
        if (rContinuity != null) score = score.add(rContinuity.multiply(normContinuity));
        if (rCompletion != null) score = score.add(rCompletion.multiply(normCompletion));
        if (rAdvance    != null) score = score.add(rAdvance.multiply(normAdvance));

        score = score.setScale(2, RoundingMode.HALF_UP);

        PriorityLevel priorityLevel;
        if (score.compareTo(new BigDecimal("25")) < 0)       priorityLevel = PriorityLevel.BAIXA;
        else if (score.compareTo(new BigDecimal("50")) < 0)  priorityLevel = PriorityLevel.MEDIA;
        else if (score.compareTo(new BigDecimal("75")) < 0)  priorityLevel = PriorityLevel.ALTA;
        else                                                  priorityLevel = PriorityLevel.CRITICA;

        RiskReason mainReason = RiskReason.SEM_SINAIS_SUFICIENTES;
        BigDecimal maxContribution = BigDecimal.ZERO;

        Map<RiskReason, BigDecimal> contributions = new HashMap<>();
        if (rInactivity != null) contributions.put(RiskReason.INATIVIDADE_ALTA, rInactivity.multiply(normInactivity));
        if (rContinuity != null) contributions.put(RiskReason.BAIXA_CONTINUIDADE, rContinuity.multiply(normContinuity));
        if (rCompletion != null) contributions.put(RiskReason.BAIXA_CONCLUSAO, rCompletion.multiply(normCompletion));
        if (rAdvance    != null) contributions.put(RiskReason.BAIXO_AVANCO, rAdvance.multiply(normAdvance));

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
        if (daysSinceLastActivity <= 2)    return BigDecimal.ZERO;
        if (daysSinceLastActivity <= 6)    return new BigDecimal("35");
        if (daysSinceLastActivity <= 14)   return new BigDecimal("70");
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
