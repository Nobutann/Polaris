package io.polaris.sebrae.model;

import io.polaris.sebrae.model.enums.PriorityLevel;
import io.polaris.sebrae.model.enums.RiskReason;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_metric_snapshots", uniqueConstraints = {
    @UniqueConstraint(name = "uq_snapshot_user_course", columnNames = {"user_id", "course_id"})
})
public class CourseMetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "days_since_last_activity")
    private Integer daysSinceLastActivity;

    @Column(name = "risk_band", length = 20)
    private String riskBand;

    @Column(name = "return_frequency_30d")
    private Integer returnFrequency30d;

    @Column(name = "continuity_rate", precision = 5, scale = 2)
    private BigDecimal continuityRate;

    @Column(name = "last_relevant_activity_at")
    private LocalDateTime lastRelevantActivityAt;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "completion_ratio", precision = 5, scale = 2)
    private BigDecimal completionRatio;

    @Column(name = "retained_7d")
    private Boolean retained7d;

    @Column(name = "retained_14d")
    private Boolean retained14d;

    @Column(name = "retained_30d")
    private Boolean retained30d;

    @Column(name = "first_relevant_activity_at")
    private LocalDateTime firstRelevantActivityAt;

    @Column(name = "advance_depth", precision = 5, scale = 2)
    private BigDecimal advanceDepth;

    @Column(name = "weighted_risk_score", precision = 5, scale = 2)
    private BigDecimal weightedRiskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level", length = 20)
    private PriorityLevel priorityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "main_risk_reason", length = 80)
    private RiskReason mainRiskReason;

    public CourseMetricSnapshot() {
        this.calculatedAt = LocalDateTime.now();
    }

    public CourseMetricSnapshot(Long userId, Long courseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.calculatedAt = LocalDateTime.now();
    }

    @PreUpdate
    @PrePersist
    public void prePersistAndUpdate() {
        this.calculatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getDaysSinceLastActivity() {
        return daysSinceLastActivity;
    }

    public void setDaysSinceLastActivity(Integer daysSinceLastActivity) {
        this.daysSinceLastActivity = daysSinceLastActivity;
    }

    public String getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(String riskBand) {
        this.riskBand = riskBand;
    }

    public Integer getReturnFrequency30d() {
        return returnFrequency30d;
    }

    public void setReturnFrequency30d(Integer returnFrequency30d) {
        this.returnFrequency30d = returnFrequency30d;
    }

    public BigDecimal getContinuityRate() {
        return continuityRate;
    }

    public void setContinuityRate(BigDecimal continuityRate) {
        this.continuityRate = continuityRate;
    }

    public LocalDateTime getLastRelevantActivityAt() {
        return lastRelevantActivityAt;
    }

    public void setLastRelevantActivityAt(LocalDateTime lastRelevantActivityAt) {
        this.lastRelevantActivityAt = lastRelevantActivityAt;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public BigDecimal getCompletionRatio() {
        return completionRatio;
    }

    public void setCompletionRatio(BigDecimal completionRatio) {
        this.completionRatio = completionRatio;
    }

    public Boolean getRetained7d() {
        return retained7d;
    }

    public void setRetained7d(Boolean retained7d) {
        this.retained7d = retained7d;
    }

    public Boolean getRetained14d() {
        return retained14d;
    }

    public void setRetained14d(Boolean retained14d) {
        this.retained14d = retained14d;
    }

    public Boolean getRetained30d() {
        return retained30d;
    }

    public void setRetained30d(Boolean retained30d) {
        this.retained30d = retained30d;
    }

    public LocalDateTime getFirstRelevantActivityAt() {
        return firstRelevantActivityAt;
    }

    public void setFirstRelevantActivityAt(LocalDateTime firstRelevantActivityAt) {
        this.firstRelevantActivityAt = firstRelevantActivityAt;
    }

    public BigDecimal getAdvanceDepth() {
        return advanceDepth;
    }

    public void setAdvanceDepth(BigDecimal advanceDepth) {
        this.advanceDepth = advanceDepth;
    }

    public BigDecimal getWeightedRiskScore() {
        return weightedRiskScore;
    }

    public void setWeightedRiskScore(BigDecimal weightedRiskScore) {
        this.weightedRiskScore = weightedRiskScore;
    }

    public PriorityLevel getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(PriorityLevel priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public RiskReason getMainRiskReason() {
        return mainRiskReason;
    }

    public void setMainRiskReason(RiskReason mainRiskReason) {
        this.mainRiskReason = mainRiskReason;
    }
}
