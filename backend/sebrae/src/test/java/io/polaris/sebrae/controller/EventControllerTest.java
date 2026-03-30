package io.polaris.sebrae.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessor.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.polaris.sebrae.dto.EventRequestDTO;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.EventRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class EventControllerTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
	
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}
	
	@Autowired
	private MockMvc mockMvc;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private EventRepository eventRepository;
	
	@BeforeEach
	void setUp() {
		eventRepository.deleteAll();
	}
	
	@Test
	void shouldReturn201WhenValidEvent() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(1L);
		dto.setCourseId(10L);
		dto.setType(EventType.LESSON_STARTED);
		
		mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value(1)).andExpect(jsonPath("$courseId").value(10));
	}
	
	@Test
	void shouldReturn400WhenMissingUserId() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setCourseId(10L);
		dto.setType(EventType.LESSON_STARTED);
		
		mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
	}
	
	@Test
	void shouldReturn400WhenMissingCourseId() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(1L);
		dto.setType(EventType.LESSON_STARTED);
		
		mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
	}
	
	@Test
	void shouldReturn400WhenMissingType() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(1L);
		dto.setCourseId(10L);
		
		mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest());
	}
	
	@Test
	void shouldPersistEventInDatabase() throws Exception {
		EventRequestDTO dto = new EventRequestDTO();
		dto.setUserId(42L);
		dto.setCourseId(99L);
		dto.setType(EventType.LESSON_STARTED);
		
		mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated());
		
		assert eventRepository.count() == 1;
	}
	
	@Test
	void shouldReturn400WhenBodyIsEmpty() throws Exception {
		mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
	}
}
