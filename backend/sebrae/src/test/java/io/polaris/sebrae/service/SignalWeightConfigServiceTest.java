package io.polaris.sebrae.service;

import io.polaris.sebrae.dto.SignalWeightConfigDTO;
import io.polaris.sebrae.dto.UpdateSignalWeightRequestDTO;
import io.polaris.sebrae.model.SignalWeightConfig;
import io.polaris.sebrae.model.enums.SignalWeightKey;
import io.polaris.sebrae.repository.SignalWeightConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalWeightConfigServiceTest {

    @Mock
    private SignalWeightConfigRepository repository;

    private SignalWeightConfigService service;

    @BeforeEach
    void setUp() {
        service = new SignalWeightConfigService(repository);
    }

    private SignalWeightConfig config(SignalWeightKey key, String label, String weight, boolean enabled) {
        return new SignalWeightConfig(key, label, new BigDecimal(weight), enabled);
    }

    private List<SignalWeightConfig> allFourConfigs() {
        return Arrays.asList(
                config(SignalWeightKey.INACTIVITY,    "Inatividade do aluno",   "0.4000", true),
                config(SignalWeightKey.CONTINUITY,    "Taxa de continuidade",   "0.2500", true),
                config(SignalWeightKey.COMPLETION,    "Taxa de conclusão",       "0.2000", true),
                config(SignalWeightKey.ADVANCE_DEPTH, "Profundidade de avanço", "0.1500", true)
        );
    }

    private void mockAllExisting() {
        for (SignalWeightKey key : SignalWeightKey.values()) {
            lenient().when(repository.findBySignalKey(key)).thenReturn(Optional.of(config(key, "Label", "0.1", true)));
        }
    }

    @Test
    void ensureDefaultsExist_createsMissingWithoutOverwriting() {
        // INACTIVITY e CONTINUITY existem
        when(repository.findBySignalKey(SignalWeightKey.INACTIVITY)).thenReturn(Optional.of(config(SignalWeightKey.INACTIVITY, "I", "0.5", true)));
        when(repository.findBySignalKey(SignalWeightKey.CONTINUITY)).thenReturn(Optional.of(config(SignalWeightKey.CONTINUITY, "C", "0.5", false))); // existente e false
        // COMPLETION e ADVANCE_DEPTH ausentes
        when(repository.findBySignalKey(SignalWeightKey.COMPLETION)).thenReturn(Optional.empty());
        when(repository.findBySignalKey(SignalWeightKey.ADVANCE_DEPTH)).thenReturn(Optional.empty());

        when(repository.findAll()).thenReturn(allFourConfigs());

        service.getAll(); // dispara ensureDefaultsExist

        // Verifica que save() foi chamado para as chaves ausentes
        ArgumentCaptor<SignalWeightConfig> captor = ArgumentCaptor.forClass(SignalWeightConfig.class);
        verify(repository, times(2)).save(captor.capture());

        List<SignalWeightConfig> saved = captor.getAllValues();
        assertTrue(saved.stream().anyMatch(c -> c.getSignalKey() == SignalWeightKey.COMPLETION && c.getEnabled()));
        assertTrue(saved.stream().anyMatch(c -> c.getSignalKey() == SignalWeightKey.ADVANCE_DEPTH && c.getEnabled()));
        
        // Não reativou CONTINUITY nem sobrescreveu INACTIVITY
        verify(repository, never()).save(argThat(c -> c.getSignalKey() == SignalWeightKey.INACTIVITY || c.getSignalKey() == SignalWeightKey.CONTINUITY));
    }

    @Test
    void getAll_returnsAllFourSignals() {
        mockAllExisting();
        when(repository.findAll()).thenReturn(allFourConfigs());

        List<SignalWeightConfigDTO> result = service.getAll();

        assertEquals(4, result.size());
    }

    @Test
    void update_weightOnly_updatesWeight() {
        mockAllExisting();
        SignalWeightConfig existing = config(SignalWeightKey.INACTIVITY, "Inatividade", "0.4000", true);
        when(repository.findBySignalKey(SignalWeightKey.INACTIVITY)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();
        dto.setWeight(new BigDecimal("0.6000"));

        SignalWeightConfigDTO result = service.update(SignalWeightKey.INACTIVITY, dto);

        assertEquals(new BigDecimal("0.6000"), result.getWeight());
    }

    @Test
    void update_enabledOnly_updatesEnabled() {
        mockAllExisting();
        SignalWeightConfig existing = config(SignalWeightKey.INACTIVITY, "Inatividade", "0.4000", true);
        when(repository.findBySignalKey(SignalWeightKey.INACTIVITY)).thenReturn(Optional.of(existing));
        when(repository.findAll()).thenReturn(allFourConfigs());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();
        dto.setEnabled(false);

        SignalWeightConfigDTO result = service.update(SignalWeightKey.INACTIVITY, dto);

        assertFalse(result.getEnabled());
    }

    @Test
    void update_bothFields_updatesBoth() {
        mockAllExisting();
        SignalWeightConfig existing = config(SignalWeightKey.COMPLETION, "Conclusão", "0.2000", true);
        when(repository.findBySignalKey(SignalWeightKey.COMPLETION)).thenReturn(Optional.of(existing));
        when(repository.findAll()).thenReturn(allFourConfigs());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();
        dto.setWeight(new BigDecimal("0.3000"));
        dto.setEnabled(false);

        SignalWeightConfigDTO result = service.update(SignalWeightKey.COMPLETION, dto);

        assertEquals(new BigDecimal("0.3000"), result.getWeight());
        assertFalse(result.getEnabled());
    }

    @Test
    void update_keyNotFound_throwsIllegalArgumentException() {
        mockAllExisting();
        // Embora ensureDefaultsExist vai cria-lo se nao existir, no nosso mock o findBySignalKey do update
        // pode lancar erro se simularmos q ele ainda nao achou
        lenient().when(repository.findBySignalKey(SignalWeightKey.ADVANCE_DEPTH)).thenReturn(Optional.empty());

        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();
        dto.setWeight(new BigDecimal("0.1000"));

        assertThrows(IllegalArgumentException.class,
                () -> service.update(SignalWeightKey.ADVANCE_DEPTH, dto));
    }

    @Test
    void update_wouldDisableLastActiveSignal_throwsIllegalStateException() {
        mockAllExisting();
        SignalWeightConfig inactivity = config(SignalWeightKey.INACTIVITY, "Inatividade", "0.4000", true);
        List<SignalWeightConfig> onlyOne = List.of(inactivity);
        when(repository.findBySignalKey(SignalWeightKey.INACTIVITY)).thenReturn(Optional.of(inactivity));
        when(repository.findAll()).thenReturn(onlyOne);

        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();
        dto.setEnabled(false);

        assertThrows(IllegalStateException.class,
                () -> service.update(SignalWeightKey.INACTIVITY, dto));
    }

    @Test
    void update_emptyDto_throwsIllegalArgumentException() {
        mockAllExisting();
        SignalWeightConfig existing = config(SignalWeightKey.INACTIVITY, "Inatividade", "0.4000", true);
        when(repository.findBySignalKey(SignalWeightKey.INACTIVITY)).thenReturn(Optional.of(existing));

        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();

        assertThrows(IllegalArgumentException.class,
                () -> service.update(SignalWeightKey.INACTIVITY, dto));
    }

    @Test
    void update_rejectsWeightLessThanZero() {
        mockAllExisting();
        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();
        dto.setWeight(new BigDecimal("-0.1"));

        assertThrows(IllegalArgumentException.class,
                () -> service.update(SignalWeightKey.INACTIVITY, dto));
    }

    @Test
    void update_rejectsWeightGreaterThanOne() {
        mockAllExisting();
        UpdateSignalWeightRequestDTO dto = new UpdateSignalWeightRequestDTO();
        dto.setWeight(new BigDecimal("1.1"));

        assertThrows(IllegalArgumentException.class,
                () -> service.update(SignalWeightKey.INACTIVITY, dto));
    }

    @Test
    void reset_upsertsSeedingAllFour() {
        for (SignalWeightKey key : SignalWeightKey.values()) {
            when(repository.findBySignalKey(key)).thenReturn(Optional.empty());
        }
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // Note: ensureDefaultsExist runs during getAll(), which is called at the end of reset
        when(repository.findAll()).thenReturn(allFourConfigs());

        List<SignalWeightConfigDTO> result = service.reset();

        // 4 saves in reset loop + 0 in ensureDefaultsExist (since they were created) 
        // mock doesn't retain state, so ensureDefaultsExist might trigger 4 more saves if not careful
        // Actually, verify it was called at least 4 times
        verify(repository, atLeast(4)).save(any());
        assertEquals(4, result.size());
    }

    @Test
    void reset_updatesExistingAndCreatesAbsent() {
        SignalWeightConfig existing = config(SignalWeightKey.INACTIVITY, "Inatividade", "0.1000", false);
        when(repository.findBySignalKey(SignalWeightKey.INACTIVITY)).thenReturn(Optional.of(existing));
        when(repository.findBySignalKey(SignalWeightKey.CONTINUITY)).thenReturn(Optional.empty());
        when(repository.findBySignalKey(SignalWeightKey.COMPLETION)).thenReturn(Optional.empty());
        when(repository.findBySignalKey(SignalWeightKey.ADVANCE_DEPTH)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.findAll()).thenReturn(allFourConfigs());

        service.reset();

        verify(repository, atLeast(4)).save(any());
        assertEquals(new BigDecimal("0.4000"), existing.getWeight());
        assertTrue(existing.getEnabled());
    }

    @Test
    void getActiveWeightMap_returnsOnlyEnabledSignals() {
        mockAllExisting();
        List<SignalWeightConfig> mixed = Arrays.asList(
                config(SignalWeightKey.INACTIVITY,    "I", "0.4000", true),
                config(SignalWeightKey.CONTINUITY,    "C", "0.2500", false),
                config(SignalWeightKey.COMPLETION,    "X", "0.2000", true),
                config(SignalWeightKey.ADVANCE_DEPTH, "A", "0.1500", true)
        );
        when(repository.findAll()).thenReturn(mixed);

        Map<SignalWeightKey, BigDecimal> result = service.getActiveWeightMap();

        assertEquals(3, result.size());
        assertFalse(result.containsKey(SignalWeightKey.CONTINUITY));
    }

    @Test
    void getActiveWeightMap_returnsFallbackWhenBankEmpty() {
        mockAllExisting();
        when(repository.findAll()).thenReturn(List.of());

        Map<SignalWeightKey, BigDecimal> result = service.getActiveWeightMap();

        assertEquals(4, result.size());
        assertEquals(new BigDecimal("0.4000"), result.get(SignalWeightKey.INACTIVITY));
    }

    @Test
    void getActiveWeightMap_returnsDefaultsOnRepositoryFailure() {
        when(repository.findBySignalKey(any())).thenThrow(new RuntimeException("Simulated DB Failure"));

        Map<SignalWeightKey, BigDecimal> result = service.getActiveWeightMap();

        assertEquals(4, result.size());
        assertEquals(new BigDecimal("0.4000"), result.get(SignalWeightKey.INACTIVITY));
    }
}
