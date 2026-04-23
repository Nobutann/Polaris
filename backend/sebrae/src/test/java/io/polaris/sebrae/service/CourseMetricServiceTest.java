package io.polaris.sebrae.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.CourseMetricSnapshotRepository;
import io.polaris.sebrae.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class CourseMetricServiceTest {

    @Mock
    private CourseMetricSnapshotRepository snapshotRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private io.polaris.sebrae.repository.CourseLessonCountRepository lessonCountRepository;

    @InjectMocks
    private CourseMetricService metricService;

    private Long userId = 1L;
    private Long courseId = 100L;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldCalculateMetricsCorrectlyForNormalActiveUser() {
        Event event1 = new Event(userId, courseId, 10L, EventType.LESSON_STARTED, null, null, null);
        event1.setTimestamp(LocalDateTime.now().minusDays(5));

        Event event2 = new Event(userId, courseId, 10L, EventType.LESSON_COMPLETED, null, null, null);
        event2.setTimestamp(LocalDateTime.now().minusDays(5).plusHours(1));

        Event event3 = new Event(userId, courseId, 11L, EventType.LESSON_STARTED, null, null, null);
        event3.setTimestamp(LocalDateTime.now().minusDays(1)); 

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(event1, event2, event3));

        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        CourseMetricSnapshot saved = captor.getValue();

        assertEquals(1, saved.getDaysSinceLastActivity());
        assertEquals("normal", saved.getRiskBand());
        assertEquals(1, saved.getReturnFrequency30d()); 
        
        assertEquals(new BigDecimal("1.00"), saved.getContinuityRate());
    }

    @Test
    void shouldCalculateRiskBandCorrectly() {
        Event event1 = new Event(userId, courseId, 10L, EventType.LESSON_STARTED, null, null, null);
        event1.setTimestamp(LocalDateTime.now().minusDays(10)); 

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(event1));
        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        CourseMetricSnapshot saved = captor.getValue();
        assertEquals(10, saved.getDaysSinceLastActivity());
        assertEquals("risco", saved.getRiskBand());
        assertEquals(0, saved.getReturnFrequency30d());
        assertNull(saved.getContinuityRate()); 
    }

    @Test
    void shouldCalculateContinuityWithSkipsAndNulls() {
        Event event1 = new Event(userId, courseId, 10L, EventType.LESSON_STARTED, null, null, null);
        event1.setTimestamp(LocalDateTime.now().minusDays(3));
        
        Event eventNoLesson = new Event(userId, courseId, null, EventType.PAGE_COMPLETED, null, null, null);
        eventNoLesson.setTimestamp(LocalDateTime.now().minusDays(2)); // null lessonId ignored in continuity calculation

        Event event2 = new Event(userId, courseId, 12L, EventType.LESSON_STARTED, null, null, null);
        event2.setTimestamp(LocalDateTime.now().minusDays(1)); // skipped 11!

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(event1, eventNoLesson, event2));
        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        CourseMetricSnapshot saved = captor.getValue();
        
        // Continuity: sequence is 10 -> 12. Transitions possible is 1. Transitions made is 0 (since 10+1 != 12)
        assertEquals(new BigDecimal("0.00"), saved.getContinuityRate());
    }

    @Test
    void shouldFindMostCommonEvasionPoint() {
        CourseMetricSnapshot snap1 = new CourseMetricSnapshot(1L, courseId);
        snap1.setRiskBand("abandono_provavel");

        CourseMetricSnapshot snap2 = new CourseMetricSnapshot(2L, courseId);
        snap2.setRiskBand("risco");

        when(snapshotRepository.findByCourseIdAndRiskBandIn(courseId, Arrays.asList("risco", "abandono_provavel")))
                .thenReturn(Arrays.asList(snap1, snap2));

        Event evUser1 = new Event(1L, courseId, 15L, EventType.LESSON_STARTED, null, null, null);
        evUser1.setTimestamp(LocalDateTime.now().minusDays(20));

        Event evUser2 = new Event(2L, courseId, 15L, EventType.LESSON_COMPLETED, null, null, null);
        evUser2.setTimestamp(LocalDateTime.now().minusDays(10));

        when(eventRepository.findRelevantEventsOrdered(1L, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(evUser1));
        when(eventRepository.findRelevantEventsOrdered(2L, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(evUser2));

        Map<Long, Long> evasionPoints = metricService.findEvasionPointsByCourse(courseId);

        assertEquals(1, evasionPoints.size());
        assertEquals(2L, evasionPoints.get(15L)); // 2 users evaded at lesson 15
    }

    // --- BLOCO 2 TESTS ---

    @Test
    void completionRatio_Case1_Reproduction() {
        // Usuário 1001 no curso 999: L1 STARTED, L1 COMPLETED, L2 STARTED -> 0.5
        Long uId = 1001L;
        Long cId = 999L;
        Event ev1 = new Event(uId, cId, 1L, EventType.LESSON_STARTED, null, null, null);
        ev1.setTimestamp(LocalDateTime.now());
        Event ev2 = new Event(uId, cId, 1L, EventType.LESSON_COMPLETED, null, null, null);
        ev2.setTimestamp(LocalDateTime.now());
        Event ev3 = new Event(uId, cId, 2L, EventType.LESSON_STARTED, null, null, null);
        ev3.setTimestamp(LocalDateTime.now());

        when(eventRepository.findRelevantEventsOrdered(uId, cId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(ev1, ev2, ev3));
        when(snapshotRepository.findByUserIdAndCourseId(uId, cId))
                .thenReturn(Optional.empty());

        metricService.recalculate(uId, cId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        // 1 concluída / 2 iniciadas = 0.50
        assertEquals(new BigDecimal("0.50"), captor.getValue().getCompletionRatio());
    }

    @Test
    void completionRatio_Case2_Reproduction() {
        // Usuário 1002 no curso 999: L1 STARTED, L1 COMPLETED -> 1.0
        Long uId = 1002L;
        Long cId = 999L;
        Event ev1 = new Event(uId, cId, 1L, EventType.LESSON_STARTED, null, null, null);
        ev1.setTimestamp(LocalDateTime.now());
        Event ev2 = new Event(uId, cId, 1L, EventType.LESSON_COMPLETED, null, null, null);
        ev2.setTimestamp(LocalDateTime.now());

        when(eventRepository.findRelevantEventsOrdered(uId, cId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(ev1, ev2));
        when(snapshotRepository.findByUserIdAndCourseId(uId, cId))
                .thenReturn(Optional.empty());

        metricService.recalculate(uId, cId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        // 1 concluída / 1 iniciada = 1.00
        assertEquals(new BigDecimal("1.00"), captor.getValue().getCompletionRatio());
    }

    @Test
    void completionRatio_noneCompleted_shouldReturnZero() {
        // 1 iniciada, 0 concluídas -> 0.00
        Event ev1 = new Event(userId, courseId, 10L, EventType.LESSON_STARTED, null, null, null);
        ev1.setTimestamp(LocalDateTime.now());

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(ev1));
        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        assertEquals(new BigDecimal("0.00"), captor.getValue().getCompletionRatio());
    }

    @Test
    void completionRatio_zeroStarted_shouldReturnNull() {
        // 0 iniciadas (apenas outro tipo relevante) -> null
        Event ev1 = new Event(userId, courseId, 10L, EventType.PAGE_COMPLETED, null, null, null);
        ev1.setTimestamp(LocalDateTime.now());

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(ev1));
        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId))
                .thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        assertNull(captor.getValue().getCompletionRatio());
    }


    @Test
    void shouldMarkRetainedTrueWhenActivityWithin7Days() {
        Event event1 = new Event(userId, courseId, 10L, EventType.LESSON_STARTED, null, null, null);
        event1.setTimestamp(LocalDateTime.now().minusDays(10)); 

        Event event2 = new Event(userId, courseId, 11L, EventType.LESSON_STARTED, null, null, null);
        event2.setTimestamp(LocalDateTime.now().minusDays(5)); // within 7 days of event1

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(event1, event2));
        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        CourseMetricSnapshot saved = captor.getValue();
        assertEquals(true, saved.getRetained7d());
        assertEquals(true, saved.getRetained14d());
        assertEquals(true, saved.getRetained30d());
    }

    @Test
    void shouldMarkRetainedFalseWhenNoActivityWithin7Days() {
        Event event1 = new Event(userId, courseId, 10L, EventType.LESSON_STARTED, null, null, null);
        event1.setTimestamp(LocalDateTime.now().minusDays(20)); 

        Event event2 = new Event(userId, courseId, 11L, EventType.LESSON_STARTED, null, null, null);
        event2.setTimestamp(LocalDateTime.now().minusDays(10)); // 10 days after event1 (missed 7d, hit 14d)

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(event1, event2));
        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        CourseMetricSnapshot saved = captor.getValue();
        assertEquals(false, saved.getRetained7d());
        assertEquals(true, saved.getRetained14d());
        assertEquals(true, saved.getRetained30d());
    }

    @Test
    void shouldCalculateAdvanceDepthCorrectly() {
        Event event1 = new Event(userId, courseId, 8L, EventType.LESSON_COMPLETED, null, null, null);
        event1.setTimestamp(LocalDateTime.now()); 

        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(event1));
        when(snapshotRepository.findByUserIdAndCourseId(userId, courseId)).thenReturn(Optional.empty());
        
        io.polaris.sebrae.model.CourseLessonCount count = new io.polaris.sebrae.model.CourseLessonCount(courseId, 10);
        when(lessonCountRepository.findByCourseId(courseId)).thenReturn(Optional.of(count));

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        assertEquals(new BigDecimal("0.80"), captor.getValue().getAdvanceDepth());
    }

    @Test
    void shouldReturnNullAdvanceDepthWhenTotalLessonsUnknown() {
        when(eventRepository.findRelevantEventsOrdered(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES)).thenReturn(Collections.emptyList());
        when(lessonCountRepository.findByCourseId(courseId)).thenReturn(Optional.empty());

        metricService.recalculate(userId, courseId);

        ArgumentCaptor<CourseMetricSnapshot> captor = ArgumentCaptor.forClass(CourseMetricSnapshot.class);
        verify(snapshotRepository).save(captor.capture());

        assertNull(captor.getValue().getAdvanceDepth());
    }
}
