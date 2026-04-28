package io.polaris.sebrae.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.polaris.sebrae.model.SignalWeightConfig;
import io.polaris.sebrae.model.enums.SignalWeightKey;
import io.polaris.sebrae.repository.SignalWeightConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
public class RelevanceConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SignalWeightConfigRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        // Popula os 4 sinais padrão antes de cada teste
        for (SignalWeightKey key : SignalWeightKey.values()) {
            BigDecimal weight = switch (key) {
                case INACTIVITY    -> new BigDecimal("0.4000");
                case CONTINUITY    -> new BigDecimal("0.2500");
                case COMPLETION    -> new BigDecimal("0.2000");
                case ADVANCE_DEPTH -> new BigDecimal("0.1500");
            };
            String label = switch (key) {
                case INACTIVITY    -> "Inatividade do aluno";
                case CONTINUITY    -> "Taxa de continuidade";
                case COMPLETION    -> "Taxa de conclusão";
                case ADVANCE_DEPTH -> "Profundidade de avanço";
            };
            repository.save(new SignalWeightConfig(key, label, weight, true));
        }
    }

    @Test
    void getAll_returns200WithFourElements() throws Exception {
        mockMvc.perform(get("/api/relevance-config/weights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void put_validBody_returns200AndPersists() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("weight", "0.5000"));

        mockMvc.perform(put("/api/relevance-config/weights/INACTIVITY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(0.5));
    }

    @Test
    void put_weightOutOfRange_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("weight", "1.5"));

        mockMvc.perform(put("/api/relevance-config/weights/INACTIVITY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_emptyBody_returns400() throws Exception {
        String body = "{}";

        mockMvc.perform(put("/api/relevance-config/weights/INACTIVITY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postReset_returns200AndRestoresDefaults() throws Exception {
        // Altera um peso antes do reset
        repository.findBySignalKey(SignalWeightKey.INACTIVITY).ifPresent(c -> {
            c.setWeight(new BigDecimal("0.9000"));
            repository.save(c);
        });

        mockMvc.perform(post("/api/relevance-config/weights/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        // Verifica que o peso foi restaurado
        SignalWeightConfig restored = repository.findBySignalKey(SignalWeightKey.INACTIVITY).orElseThrow();
        assert restored.getWeight().compareTo(new BigDecimal("0.4000")) == 0;
    }
}
