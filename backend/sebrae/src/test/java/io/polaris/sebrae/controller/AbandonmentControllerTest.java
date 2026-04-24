package io.polaris.sebrae.controller;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.CourseMetricSnapshotRepository;
import io.polaris.sebrae.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(roles = "ANALYTICS")
public class AbandonmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CourseMetricSnapshotRepository snapshotRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        snapshotRepository.deleteAll();
    }

    @Test
    void shouldReturnUserAbandonmentStatus() throws Exception {
        Long courseId = 2L;
        Long userId = 200L;
        
        // Save a COURSE_ABANDONED event
        Event event = new Event();
        event.setUserId(userId);
        event.setCourseId(courseId);
        event.setType(EventType.COURSE_ABANDONED);
        event.setTimestamp(LocalDateTime.of(2025, 1, 1, 10, 0));
        eventRepository.save(event);

        mockMvc.perform(get("/api/abandonment/courses/" + courseId + "/users/" + userId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.courseId").value(courseId))
                .andExpect(jsonPath("$.abandoned").value(true))
                .andExpect(jsonPath("$.returnedAfterAbandonment").value(false))
                .andExpect(jsonPath("$.abandonmentStatus").value("ABANDONED"));
    }
}
