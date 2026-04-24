package io.polaris.sebrae.service;

import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AbandonmentService domain logic.
 *
 * These tests cover the official story scenarios:
 *  1. User active < 15 days → no abandonment
 *  2. User inactive ≥ 15 days → COURSE_ABANDONED persisted
 *  3. User already abandoned and still inactive → no duplicate
 *  4. User abandoned then returns → COURSE_RETURNED persisted
 *  5. User returned and still active → no duplicate return
 *  6. getStatus() reflects correct state
 */
@ExtendWith(MockitoExtension.class)
public class AbandonmentServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private AbandonmentService abandonmentService;

    private static final Long USER_ID = 42L;
    private static final Long COURSE_ID = 100L;
    private static final Set<EventType> LIFECYCLE =
            Set.of(EventType.COURSE_ABANDONED, EventType.COURSE_RETURNED);

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void mockNoLifecycleEvents() {
        when(eventRepository.findLatestByUserIdAndCourseIdAndTypes(
                eq(USER_ID), eq(COURSE_ID), eq(LIFECYCLE), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
    }

    private void mockLastLifecycleEvent(EventType type, LocalDateTime ts) {
        Event e = new Event();
        e.setType(type);
        e.setTimestamp(ts);
        when(eventRepository.findLatestByUserIdAndCourseIdAndTypes(
                eq(USER_ID), eq(COURSE_ID), eq(LIFECYCLE), any(PageRequest.class)))
                .thenReturn(List.of(e));
    }

    // -----------------------------------------------------------------------
    // Scenario 1 — User active (< 15 days) → must NOT abandon
    // -----------------------------------------------------------------------

    @Test
    void scenario1_userActiveLessThan15Days_shouldNotAbandon() {
        mockNoLifecycleEvents();
        // Last relevant activity 10 days ago → 10 < 15 → no abandonment
        when(eventRepository.findLastRelevantActivityAt(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES)))
                .thenReturn(Optional.of(LocalDateTime.now().minusDays(10)));

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void scenario1_userActiveExactly14Days_shouldNotAbandon() {
        mockNoLifecycleEvents();
        when(eventRepository.findLastRelevantActivityAt(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES)))
                .thenReturn(Optional.of(LocalDateTime.now().minusDays(14)));

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        verify(eventRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Scenario 2 — User inactive ≥ 15 days → COURSE_ABANDONED must be persisted
    // -----------------------------------------------------------------------

    @Test
    void scenario2_userInactive15Days_shouldPersistCourseAbandoned() {
        mockNoLifecycleEvents();
        when(eventRepository.findLastRelevantActivityAt(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES)))
                .thenReturn(Optional.of(LocalDateTime.now().minusDays(15)));

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(1)).save(captor.capture());
        assertEquals(EventType.COURSE_ABANDONED, captor.getValue().getType());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(COURSE_ID, captor.getValue().getCourseId());
    }

    @Test
    void scenario2_userInactive30Days_shouldPersistCourseAbandoned() {
        mockNoLifecycleEvents();
        when(eventRepository.findLastRelevantActivityAt(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES)))
                .thenReturn(Optional.of(LocalDateTime.now().minusDays(30)));

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertEquals(EventType.COURSE_ABANDONED, captor.getValue().getType());
    }

    // -----------------------------------------------------------------------
    // Scenario 3 — Already abandoned, still inactive → NO duplicate
    // -----------------------------------------------------------------------

    @Test
    void scenario3_alreadyAbandonedAndStillInactive_shouldNotDuplicate() {
        LocalDateTime abandonedAt = LocalDateTime.now().minusDays(20);
        mockLastLifecycleEvent(EventType.COURSE_ABANDONED, abandonedAt);

        // No relevant activity after the abandonment timestamp
        when(eventRepository.findLastRelevantActivityAfter(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES), eq(abandonedAt)))
                .thenReturn(Optional.empty());

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        // Nothing should be saved — the user is still in the same abandoned cycle
        verify(eventRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Scenario 4 — Abandoned user returns → COURSE_RETURNED must be persisted
    // -----------------------------------------------------------------------

    @Test
    void scenario4_abandonedUserReturns_shouldPersistCourseReturned() {
        LocalDateTime abandonedAt = LocalDateTime.now().minusDays(20);
        mockLastLifecycleEvent(EventType.COURSE_ABANDONED, abandonedAt);

        // There IS relevant activity after the abandonment
        when(eventRepository.findLastRelevantActivityAfter(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES), eq(abandonedAt)))
                .thenReturn(Optional.of(LocalDateTime.now().minusDays(1)));

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, times(1)).save(captor.capture());
        assertEquals(EventType.COURSE_RETURNED, captor.getValue().getType());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(COURSE_ID, captor.getValue().getCourseId());
    }

    // -----------------------------------------------------------------------
    // Scenario 5 — Returned user still active → NO duplicate return
    // -----------------------------------------------------------------------

    @Test
    void scenario5_returnedUserStillActive_shouldNotDuplicate() {
        // Most recent lifecycle event is COURSE_RETURNED — the user is NOT in an active abandonment cycle
        LocalDateTime returnedAt = LocalDateTime.now().minusDays(2);
        mockLastLifecycleEvent(EventType.COURSE_RETURNED, returnedAt);

        // Last relevant activity is recent (1 day ago) — well within threshold
        when(eventRepository.findLastRelevantActivityAt(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES)))
                .thenReturn(Optional.of(LocalDateTime.now().minusDays(1)));

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        // daysSince = 1 < 15 → no new abandonment, and no duplicate return
        verify(eventRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Scenario 6 — getStatus() reflects correct abandonment state
    // -----------------------------------------------------------------------

    @Test
    void scenario6a_getStatus_whenAbandoned_returnsAbandonedState() {
        LocalDateTime abandonedAt = LocalDateTime.now().minusDays(20);
        mockLastLifecycleEvent(EventType.COURSE_ABANDONED, abandonedAt);

        AbandonmentService.AbandonmentStatus status = abandonmentService.getStatus(USER_ID, COURSE_ID);

        assertTrue(status.isAbandoned());
        assertEquals(abandonedAt, status.getAbandonedAt());
        assertFalse(status.isReturnedAfterAbandonment());
        assertNull(status.getReturnedAt());
        assertEquals("ABANDONED", status.getAbandonmentStatus());
    }

    @Test
    void scenario6b_getStatus_whenNoLifecycleEvent_returnsActiveState() {
        mockNoLifecycleEvents();

        AbandonmentService.AbandonmentStatus status = abandonmentService.getStatus(USER_ID, COURSE_ID);

        assertFalse(status.isAbandoned());
        assertNull(status.getAbandonedAt());
        assertFalse(status.isReturnedAfterAbandonment());
        assertEquals("ACTIVE", status.getAbandonmentStatus());
    }

    @Test
    void scenario6c_getStatus_whenReturned_returnsReturnedState() {
        LocalDateTime returnedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime abandonedAt = LocalDateTime.now().minusDays(20);

        Event returnEvent = new Event();
        returnEvent.setType(EventType.COURSE_RETURNED);
        returnEvent.setTimestamp(returnedAt);

        Event abandonEvent = new Event();
        abandonEvent.setType(EventType.COURSE_ABANDONED);
        abandonEvent.setTimestamp(abandonedAt);

        // First call (top 1) returns the return event
        // Second call (top 20) returns both to find the preceding abandonment
        when(eventRepository.findLatestByUserIdAndCourseIdAndTypes(
                eq(USER_ID), eq(COURSE_ID), eq(LIFECYCLE), any(PageRequest.class)))
                .thenReturn(List.of(returnEvent))
                .thenReturn(List.of(returnEvent, abandonEvent));

        AbandonmentService.AbandonmentStatus status = abandonmentService.getStatus(USER_ID, COURSE_ID);

        assertFalse(status.isAbandoned());
        assertTrue(status.isReturnedAfterAbandonment());
        assertEquals(returnedAt, status.getReturnedAt());
        assertEquals(abandonedAt, status.getAbandonedAt());
        assertEquals("RETURNED", status.getAbandonmentStatus());
    }

    // -----------------------------------------------------------------------
    // Edge cases
    // -----------------------------------------------------------------------

    @Test
    void noRelevantActivity_shouldNotAbandon() {
        mockNoLifecycleEvents();
        when(eventRepository.findLastRelevantActivityAt(
                eq(USER_ID), eq(COURSE_ID), eq(EventType.RELEVANT_ACTIVITY_TYPES)))
                .thenReturn(Optional.empty());

        abandonmentService.evaluate(USER_ID, COURSE_ID);

        verify(eventRepository, never()).save(any());
    }
}
