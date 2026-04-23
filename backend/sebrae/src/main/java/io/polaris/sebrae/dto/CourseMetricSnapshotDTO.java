package io.polaris.sebrae.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CourseMetricSnapshotDTO {

    private Long userId;
    private Long courseId;
    private Integer daysSinceLastActivity;
    private String riskBand;
    private Integer returnFrequency30d;
    private BigDecimal continuityRate;
    private LocalDateTime lastRelevantActivityAt;
    private LocalDateTime calculatedAt;

    private BigDecimal completionRatio;
    private Boolean retained7d;
    private Boolean retained14d;
    private Boolean retained30d;
    private LocalDateTime firstRelevantActivityAt;
    private BigDecimal advanceDepth;

    public CourseMetricSnapshotDTO(Long userId, Long courseId, Integer daysSinceLastActivity, String riskBand, Integer returnFrequency30d, BigDecimal continuityRate, LocalDateTime lastRelevantActivityAt, LocalDateTime calculatedAt) {
        this.userId = userId;
        this.courseId = courseId;
        this.daysSinceLastActivity = daysSinceLastActivity;
        this.riskBand = riskBand;
        this.returnFrequency30d = returnFrequency30d;
        this.continuityRate = continuityRate;
        this.lastRelevantActivityAt = lastRelevantActivityAt;
        this.calculatedAt = calculatedAt;
    }

    public CourseMetricSnapshotDTO() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Integer getDaysSinceLastActivity() { return daysSinceLastActivity; }
    public void setDaysSinceLastActivity(Integer daysSinceLastActivity) { this.daysSinceLastActivity = daysSinceLastActivity; }

    public String getRiskBand() { return riskBand; }
    public void setRiskBand(String riskBand) { this.riskBand = riskBand; }

    public Integer getReturnFrequency30d() { return returnFrequency30d; }
    public void setReturnFrequency30d(Integer returnFrequency30d) { this.returnFrequency30d = returnFrequency30d; }

    public BigDecimal getContinuityRate() { return continuityRate; }
    public void setContinuityRate(BigDecimal continuityRate) { this.continuityRate = continuityRate; }

    public LocalDateTime getLastRelevantActivityAt() { return lastRelevantActivityAt; }
    public void setLastRelevantActivityAt(LocalDateTime lastRelevantActivityAt) { this.lastRelevantActivityAt = lastRelevantActivityAt; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

    public BigDecimal getCompletionRatio() { return completionRatio; }
    public void setCompletionRatio(BigDecimal completionRatio) { this.completionRatio = completionRatio; }

    public Boolean getRetained7d() { return retained7d; }
    public void setRetained7d(Boolean retained7d) { this.retained7d = retained7d; }

    public Boolean getRetained14d() { return retained14d; }
    public void setRetained14d(Boolean retained14d) { this.retained14d = retained14d; }

    public Boolean getRetained30d() { return retained30d; }
    public void setRetained30d(Boolean retained30d) { this.retained30d = retained30d; }

    public LocalDateTime getFirstRelevantActivityAt() { return firstRelevantActivityAt; }
    public void setFirstRelevantActivityAt(LocalDateTime firstRelevantActivityAt) { this.firstRelevantActivityAt = firstRelevantActivityAt; }

    public BigDecimal getAdvanceDepth() { return advanceDepth; }
    public void setAdvanceDepth(BigDecimal advanceDepth) { this.advanceDepth = advanceDepth; }
}
