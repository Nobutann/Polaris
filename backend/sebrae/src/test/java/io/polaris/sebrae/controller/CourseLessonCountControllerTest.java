package io.polaris.sebrae.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.polaris.sebrae.dto.CourseLessonCountRequestDTO;
import io.polaris.sebrae.repository.CourseLessonCountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
public class CourseLessonCountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseLessonCountRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldReturn200WhenRegisteringLessonCount() throws Exception {
        CourseLessonCountRequestDTO dto = new CourseLessonCountRequestDTO();
        dto.setTotalLessons(10);

        mockMvc.perform(post("/api/courses/42/lesson-count").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        assertEquals(1, repository.count());
        assertEquals(10, repository.findByCourseId(42L).get().getTotalLessons());
    }

    @Test
    void shouldReturn400WhenTotalLessonsIsNegative() throws Exception {
        CourseLessonCountRequestDTO dto = new CourseLessonCountRequestDTO();
        dto.setTotalLessons(-5);

        mockMvc.perform(post("/api/courses/42/lesson-count").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
