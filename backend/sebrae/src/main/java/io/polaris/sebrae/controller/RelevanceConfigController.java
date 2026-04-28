package io.polaris.sebrae.controller;

import io.polaris.sebrae.dto.SignalWeightConfigDTO;
import io.polaris.sebrae.dto.UpdateSignalWeightRequestDTO;
import io.polaris.sebrae.model.enums.SignalWeightKey;
import io.polaris.sebrae.service.AuditLogger;
import io.polaris.sebrae.service.SignalWeightConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relevance-config/weights")
public class RelevanceConfigController {

    private final SignalWeightConfigService configService;
    private final AuditLogger auditLogger;

    public RelevanceConfigController(SignalWeightConfigService configService, AuditLogger auditLogger) {
        this.configService = configService;
        this.auditLogger = auditLogger;
    }

    /** GET /api/relevance-config/weights — lista todos os sinais e pesos atuais. */
    @GetMapping
    public ResponseEntity<List<SignalWeightConfigDTO>> getAll() {
        return ResponseEntity.ok(configService.getAll());
    }

    /**
     * PUT /api/relevance-config/weights/{signalKey} — atualiza parcialmente peso e/ou enabled.
     * Retorna 400 se body estiver vazio (sem weight nem enabled).
     */
    @PutMapping("/{signalKey}")
    public ResponseEntity<SignalWeightConfigDTO> update(
            @PathVariable SignalWeightKey signalKey,
            @Valid @RequestBody UpdateSignalWeightRequestDTO dto) {

        if (!dto.hasAtLeastOneField()) {
            return ResponseEntity.badRequest().build();
        }

        auditLogger.logAdminAction(
                "UPDATE_SIGNAL_WEIGHT",
                "/api/relevance-config/weights/" + signalKey,
                Map.of("signalKey", signalKey.name(),
                       "weight",   String.valueOf(dto.getWeight()),
                       "enabled",  String.valueOf(dto.getEnabled()))
        );

        return ResponseEntity.ok(configService.update(signalKey, dto));
    }

    /**
     * POST /api/relevance-config/weights/reset — restaura/recria todos os 4 padrões (upsert).
     */
    @PostMapping("/reset")
    public ResponseEntity<List<SignalWeightConfigDTO>> reset() {
        auditLogger.logAdminAction(
                "RESET_SIGNAL_WEIGHTS",
                "/api/relevance-config/weights/reset",
                Map.of("action", "reset_all_to_defaults")
        );
        return ResponseEntity.ok(configService.reset());
    }
}
