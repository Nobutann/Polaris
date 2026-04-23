package io.polaris.sebrae.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.repository.CourseMetricSnapshotRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
public class CourseMetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseMetricSnapshotRepository snapshotRepository;

    @BeforeEach
    void setUp() {
        snapshotRepository.deleteAll();
    }

    @Test
    void shouldTriggerRecalculation() throws Exception {
        // Just checking if the endpoint responds ok. Testing the actual recalculation logic is meant for service tests
        mockMvc.perform(post("/api/metrics/courses/10/recalculate").with(csrf()))
                .andExpect(status().isOk());
    }
}
