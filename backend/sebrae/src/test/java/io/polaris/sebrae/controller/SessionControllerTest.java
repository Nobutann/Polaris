package io.polaris.sebrae.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.polaris.sebrae.dto.SessionRequestDTO;
import io.polaris.sebrae.repository.SessionRepository;
import io.polaris.sebrae.repository.SignalRepository;
import io.polaris.sebrae.model.Session;
import io.polaris.sebrae.model.Signal;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
public class SessionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private SignalRepository signalRepository;

	@Autowired
	private io.polaris.sebrae.repository.CourseLessonCountRepository lessonCountRepository;

	@BeforeEach
	void setUp() {
		signalRepository.deleteAll();
		sessionRepository.deleteAll();
		lessonCountRepository.deleteAll();
		// Some tests don't specify courseId for session, but some real data might.
	}

	@Test
	void shouldStartSession() throws Exception {
		SessionRequestDTO dto = new SessionRequestDTO();
		dto.setUserId(1L);

		mockMvc.perform(post("/api/sessions").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated());

		assertEquals(1, sessionRepository.count());
		assertEquals(1, signalRepository.count());
		Signal signal = signalRepository.findAll().get(0);
		assertEquals("SESSION_STARTED", signal.getType());
	}

	@Test
	void shouldReturn404WhenEndingNonExistentSession() throws Exception {
		mockMvc.perform(post("/api/sessions/9999/end").with(csrf()))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldEndSessionAndGenerateSignal() throws Exception {
		Session session = new Session();
		session.setUserId(10L);
		session = sessionRepository.save(session);

		mockMvc.perform(post("/api/sessions/" + session.getId() + "/end").with(csrf()))
				.andExpect(status().isOk());

		Session ended = sessionRepository.findById(session.getId()).orElseThrow();
		assertNotNull(ended.getEndTime());

		assertEquals(1, signalRepository.count());
		Signal signal = signalRepository.findAll().get(0);
		assertEquals("SESSION_ENDED", signal.getType());
		assertEquals(10L, signal.getUserId());
	}
}
