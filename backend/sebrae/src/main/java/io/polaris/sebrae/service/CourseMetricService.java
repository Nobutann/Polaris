package io.polaris.sebrae.service;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.CourseMetricSnapshotRepository;
import io.polaris.sebrae.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseMetricService {

    private final CourseMetricSnapshotRepository snapshotRepository;
    private final EventRepository eventRepository;
    private final io.polaris.sebrae.repository.CourseLessonCountRepository lessonCountRepository;
    private final AuditLogger auditLogger;

    public CourseMetricService(CourseMetricSnapshotRepository snapshotRepository, EventRepository eventRepository, io.polaris.sebrae.repository.CourseLessonCountRepository lessonCountRepository, AuditLogger auditLogger) {
        this.snapshotRepository = snapshotRepository;
        this.eventRepository = eventRepository;
        this.lessonCountRepository = lessonCountRepository;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public void recalculate(Long userId, Long courseId) {
        List<Event> relevantEvents = eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES);

        CourseMetricSnapshot snapshot = snapshotRepository.findByUserIdAndCourseId(userId, courseId)
                .orElse(new CourseMetricSnapshot(userId, courseId));

        LocalDateTime lastActivity = relevantEvents.isEmpty() ? null : relevantEvents.get(relevantEvents.size() - 1).getTimestamp();
        snapshot.setLastRelevantActivityAt(lastActivity);

        Integer daysSinceLast = calculateM1(lastActivity);
        snapshot.setDaysSinceLastActivity(daysSinceLast);

        String riskBand = calculateM2(daysSinceLast);
        snapshot.setRiskBand(riskBand);

        Integer returnFrequency = calculateM3(relevantEvents);
        snapshot.setReturnFrequency30d(returnFrequency);

        BigDecimal continuityRate = calculateM4(relevantEvents);
        snapshot.setContinuityRate(continuityRate);

        snapshot.setCompletionRatio(calculateM6(relevantEvents));
        calculateM7(snapshot, relevantEvents);
        snapshot.setAdvanceDepth(calculateM8(courseId, relevantEvents));

        snapshotRepository.save(snapshot);
    }

    private BigDecimal calculateM6(List<Event> events) {
        long started = events.stream()
                .filter(e -> e.getType() == EventType.LESSON_STARTED)
                .count();
        long completed = events.stream()
                .filter(e -> e.getType() == EventType.LESSON_COMPLETED)
                .count();

        if (started == 0) return null;

        // Fórmula oficial: concluídas / iniciadas
        return BigDecimal.valueOf(completed)
                .divide(BigDecimal.valueOf(started), 2, RoundingMode.HALF_UP);
    }

    private void calculateM7(CourseMetricSnapshot snapshot, List<Event> relevantEvents) {
        if (relevantEvents.isEmpty()) {
            snapshot.setFirstRelevantActivityAt(null);
            snapshot.setRetained7d(null);
            snapshot.setRetained14d(null);
            snapshot.setRetained30d(null);
            return;
        }

        LocalDateTime firstActivity = relevantEvents.get(0).getTimestamp();
        snapshot.setFirstRelevantActivityAt(firstActivity);

        boolean ret7 = false, ret14 = false, ret30 = false;

        for (Event e : relevantEvents) {
            LocalDateTime ts = e.getTimestamp();
            if (ts.isAfter(firstActivity)) {
                if (!ts.isAfter(firstActivity.plusDays(7))) ret7 = true;
                if (!ts.isAfter(firstActivity.plusDays(14))) ret14 = true;
                if (!ts.isAfter(firstActivity.plusDays(30))) ret30 = true;
            }
        }

        // Se reativou em 7d, obviamente reativou também nas janelas maiores
        if (ret7) { ret14 = true; ret30 = true; }
        else if (ret14) { ret30 = true; }

        snapshot.setRetained7d(ret7);
        snapshot.setRetained14d(ret14);
        snapshot.setRetained30d(ret30);
    }

    private BigDecimal calculateM8(Long courseId, List<Event> relevantEvents) {
        io.polaris.sebrae.model.CourseLessonCount lessonCount = lessonCountRepository.findByCourseId(courseId).orElse(null);
        if (lessonCount == null || lessonCount.getTotalLessons() == null || lessonCount.getTotalLessons() <= 0) return null;

        Long maxLessonReached = null;
        for (Event e : relevantEvents) {
            if ((e.getType() == EventType.LESSON_STARTED || e.getType() == EventType.LESSON_COMPLETED) && e.getLessonId() != null) {
                if (maxLessonReached == null || e.getLessonId() > maxLessonReached) {
                    maxLessonReached = e.getLessonId();
                }
            }
        }

        if (maxLessonReached == null) return null;
        return BigDecimal.valueOf((double) maxLessonReached / lessonCount.getTotalLessons()).setScale(2, RoundingMode.HALF_UP);
    }

// ... continuing from previous code

    private Integer calculateM1(LocalDateTime lastRelevantActivityAt) {
        if (lastRelevantActivityAt == null) return null;
        return (int) ChronoUnit.DAYS.between(lastRelevantActivityAt.toLocalDate(), LocalDate.now());
    }

    private String calculateM2(Integer daysSinceLast) {
        if (daysSinceLast == null) return "sem_atividade";
        if (daysSinceLast <= 2) return "normal";
        if (daysSinceLast <= 6) return "atencao";
        if (daysSinceLast <= 14) return "risco";
        return "abandono_provavel";
    }

    private Integer calculateM3(List<Event> events) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        List<LocalDate> activeDays = events.stream()
                .filter(e -> !e.getTimestamp().toLocalDate().isBefore(thirtyDaysAgo))
                .map(e -> e.getTimestamp().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (activeDays.isEmpty()) return 0;

        int returns = 0;
        for (int i = 1; i < activeDays.size(); i++) {
            long daysBetween = ChronoUnit.DAYS.between(activeDays.get(i - 1), activeDays.get(i));
            if (daysBetween >= 1) { // Implicit by definition but explicit for clarity
                returns++;
            }
        }
        return returns;
    }

    private BigDecimal calculateM4(List<Event> events) {
        // T2.4 - assume lessonId is sequential. Ignora nulls.
        List<Event> lessonEvents = events.stream()
                .filter(e -> (e.getType() == EventType.LESSON_STARTED || e.getType() == EventType.LESSON_COMPLETED) && e.getLessonId() != null)
                .collect(Collectors.toList());

        if (lessonEvents.isEmpty()) return null;

        List<Long> sequence = new ArrayList<>();
        for (Event e : lessonEvents) {
            Long lId = e.getLessonId();
            if (sequence.isEmpty() || !sequence.get(sequence.size() - 1).equals(lId)) {
                sequence.add(lId);
            }
        }

        if (sequence.size() < 2) return null;

        int transitionsMade = 0;
        int transitionsPossible = sequence.size() - 1;

        for (int i = 0; i < sequence.size() - 1; i++) {
            Long current = sequence.get(i);
            Long next = sequence.get(i + 1);
            if (next.equals(current + 1)) {
                transitionsMade++;
            }
        }

        if (transitionsPossible == 0) return null;
        
        return BigDecimal.valueOf((double) transitionsMade / transitionsPossible).setScale(2, RoundingMode.HALF_UP);
    }

// ... continuing from previous code
    public Map<Long, Long> findEvasionPointsByCourse(Long courseId) {
        // T2.5
        List<CourseMetricSnapshot> snapshots = snapshotRepository.findByCourseIdAndRiskBandIn(courseId, Arrays.asList("risco", "abandono_provavel"));
        
        Map<Long, Long> evasionCounts = new HashMap<>();
        
        for (CourseMetricSnapshot snap : snapshots) {
            Long uId = snap.getUserId();
            List<Event> events = eventRepository.findRelevantEventsOrdered(uId, courseId, EventType.RELEVANT_ACTIVITY_TYPES);
            if (!events.isEmpty()) {
                Event last = events.get(events.size() - 1);
                if (last.getLessonId() != null) {
                    evasionCounts.put(last.getLessonId(), evasionCounts.getOrDefault(last.getLessonId(), 0L) + 1L);
                }
            }
        }

        return evasionCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public void recalculateCourse(Long courseId) {
        List<Object[]> userCoursePairs = eventRepository.findDistinctUserCourseWithRelevantActivity(EventType.RELEVANT_ACTIVITY_TYPES);
        for (Object[] pair : userCoursePairs) {
            Long cId = ((Number) pair[1]).longValue();
            if (cId.equals(courseId)) {
                Long uId = ((Number) pair[0]).longValue();
                recalculate(uId, cId);
            }
        }
        auditLogger.logAdminAction("RECALCULATE_COURSE", "/api/metrics/courses/" + courseId + "/recalculate", Map.of("courseId", courseId));
    }
}

