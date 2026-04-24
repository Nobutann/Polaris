package io.polaris.sebrae.controller;

import io.polaris.sebrae.dto.SignalRequestDTO;
import io.polaris.sebrae.dto.SignalResponseDTO;
import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;
import io.polaris.sebrae.service.SignalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import io.polaris.sebrae.service.ExternalSignalIngestionService;
import io.polaris.sebrae.service.collector.YouTubeSignalCollector;

@RestController
@RequestMapping("/api/signals")
public class SignalController {

    private final SignalService signalService;
    private final ExternalSignalIngestionService externalSignalIngestionService;
    private final YouTubeSignalCollector youTubeSignalCollector;

    public SignalController(SignalService signalService, 
                            ExternalSignalIngestionService externalSignalIngestionService, 
                            YouTubeSignalCollector youTubeSignalCollector) {
        this.signalService = signalService;
        this.externalSignalIngestionService = externalSignalIngestionService;
        this.youTubeSignalCollector = youTubeSignalCollector;
    }

    @PostMapping
    public ResponseEntity<io.polaris.sebrae.dto.SignalAckDTO> receive(@Valid @RequestBody SignalRequestDTO dto) {
        Signal saved = signalService.saveFromRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new io.polaris.sebrae.dto.SignalAckDTO(saved));
    }

    @GetMapping
    public ResponseEntity<Page<io.polaris.sebrae.dto.SignalSummaryDTO>> getAll(
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Signal> signals;
        if (source != null) {
            signals = signalService.findBySource(SignalSource.valueOf(source), pageable);
        } else {
            signals = signalService.findAll(pageable);
        }

        Page<io.polaris.sebrae.dto.SignalSummaryDTO> dtos = signals.map(io.polaris.sebrae.dto.SignalSummaryDTO::new);

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/ingest/youtube")
    public ResponseEntity<io.polaris.sebrae.dto.YouTubeIngestionResponseDTO> ingestYouTube() {
        int count = externalSignalIngestionService.ingest(youTubeSignalCollector);
        return ResponseEntity.ok(new io.polaris.sebrae.dto.YouTubeIngestionResponseDTO(count));
    }
}
