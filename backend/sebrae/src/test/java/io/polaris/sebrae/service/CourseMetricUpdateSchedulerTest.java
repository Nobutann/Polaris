package io.polaris.sebrae.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class CourseMetricUpdateSchedulerTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CourseMetricService courseMetricService;

    @InjectMocks
    private CourseMetricUpdateScheduler scheduler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldRecalculateMetricsForAllUserCoursePairsHourly() {
        Object[] pair1 = new Object[]{1L, 10L};
        Object[] pair2 = new Object[]{2L, 10L};
        
        when(eventRepository.findDistinctUserCourseWithRelevantActivity(EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(pair1, pair2));

        scheduler.recalculateAllMetrics();

        verify(courseMetricService, times(1)).recalculate(1L, 10L);
        verify(courseMetricService, times(1)).recalculate(2L, 10L);
    }

    @Test
    void shouldHandleExceptionDuringRecalculationWithoutCrashingJob() {
        Object[] pair1 = new Object[]{1L, 10L};
        Object[] pair2 = new Object[]{2L, 10L};
        
        when(eventRepository.findDistinctUserCourseWithRelevantActivity(EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Arrays.asList(pair1, pair2));

        doThrow(new RuntimeException("Database error")).when(courseMetricService).recalculate(1L, 10L);

        assertDoesNotThrow(() -> scheduler.recalculateAllMetrics());

        verify(courseMetricService, times(1)).recalculate(1L, 10L);
        verify(courseMetricService, times(1)).recalculate(2L, 10L); // the second one should still execute!
    }

    @Test
    void shouldNotRecalculateWhenNoPairsFound() {
        when(eventRepository.findDistinctUserCourseWithRelevantActivity(EventType.RELEVANT_ACTIVITY_TYPES))
                .thenReturn(Collections.emptyList());

        scheduler.recalculateAllMetrics();

        verify(courseMetricService, never()).recalculate(anyLong(), anyLong());
    }
}
