package io.polaris.sebrae.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import io.polaris.sebrae.dto.EventRequestDTO;
import io.polaris.sebrae.dto.InactivityRequestDTO;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.EventRepository;
import io.polaris.sebrae.repository.SignalRepository;
import io.polaris.sebrae.model.Signal;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
public class EventControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private SignalRepository signalRepository;

	@Autowired
	private io.polaris.sebrae.repository.CourseLessonCountRepository lessonCountRepository;

	@BeforeEach
	void setUp() {
		signalRepository.deleteAll();
		eventRepository.deleteAll();
		lessonCountRepository.deleteAll();
		
		lessonCountRepository.save(new io.polaris.sebrae.model.CourseLessonCount(10L, 5));
		lessonCountRepository.save(new io.polaris.sebrae.model.CourseLessonCount(99L, 20));
		lessonCountRepository.save(new io.polaris.sebrae.model.CourseLessonCount(20L, 10));
	}

	@Test
	void shouldReturn201WhenValidEvent() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(1L);
		dto.setCourseId(10L);
		dto.setType(EventType.LESSON_STARTED);

		mockMvc.perform(post("/api/events").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated());
	}

	@Test
	void shouldReturn400WhenMissingUserId() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setCourseId(10L);
		dto.setType(EventType.LESSON_STARTED);

		mockMvc.perform(post("/api/events").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenMissingCourseId() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(1L);
		dto.setType(EventType.LESSON_STARTED);

		mockMvc.perform(post("/api/events").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenMissingType() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(1L);
		dto.setCourseId(10L);

		mockMvc.perform(post("/api/events").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldPersistEventInDatabase() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(42L);
		dto.setCourseId(99L);
		dto.setType(EventType.LESSON_STARTED);

		mockMvc.perform(post("/api/events").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated());

		assertEquals(1, eventRepository.count());
		assertEquals(1, signalRepository.count());

		Signal signal = signalRepository.findAll().get(0);
		assertEquals("INTERNAL", signal.getSource().name());
		assertEquals("LESSON_STARTED", signal.getType());
	}

	@Test
	void shouldReturn400WhenBodyIsEmpty() throws Exception {
		mockMvc.perform(post("/api/events").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldGenerateSignalOnInactivity() throws Exception {
		InactivityRequestDTO dto = new InactivityRequestDTO();
		dto.setUserId(42L);
		dto.setCourseId(99L);

		mockMvc.perform(post("/api/events/inactivity").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk());

		assertEquals(1, eventRepository.count());
		assertEquals(1, signalRepository.count());

		Signal signal = signalRepository.findAll().get(0);
		assertEquals("INTERNAL", signal.getSource().name());
		assertEquals("SCREEN_INACTIVE", signal.getType());
	}

	@Test
	void shouldPreserveMetadataWhenGeneratingSignal() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(42L);
		dto.setCourseId(99L);
		dto.setType(EventType.LESSON_STARTED);
		dto.setDevice("mobile");
		dto.setBrowser("safari");
		dto.setMetadata("{\"progressPercent\": 75}");

		mockMvc.perform(post("/api/events").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isCreated());

		Signal signal = signalRepository.findAll().get(0);
		String metadata = signal.getMetadata();

		assertTrue(metadata.contains("\"device\":\"mobile\""));
		assertTrue(metadata.contains("\"browser\":\"safari\""));
		assertTrue(metadata.contains("\"progressPercent\""));
	}

	@Test
	void shouldPreserveDeviceAndBrowserOnInactivity() throws Exception {
		InactivityRequestDTO dto = new InactivityRequestDTO();
		dto.setUserId(5L);
		dto.setCourseId(20L);
		dto.setDevice("desktop");
		dto.setBrowser("chrome");

		mockMvc.perform(post("/api/events/inactivity").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk());

		Signal signal = signalRepository.findAll().get(0);
		String metadata = signal.getMetadata();

		assertTrue(metadata.contains("\"device\":\"desktop\""));
		assertTrue(metadata.contains("\"browser\":\"chrome\""));
		assertEquals("SCREEN_INACTIVE", signal.getType());
	}
}
