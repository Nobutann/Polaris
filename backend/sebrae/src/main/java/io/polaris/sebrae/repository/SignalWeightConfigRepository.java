package io.polaris.sebrae.repository;

import io.polaris.sebrae.model.SignalWeightConfig;
import io.polaris.sebrae.model.enums.SignalWeightKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignalWeightConfigRepository extends JpaRepository<SignalWeightConfig, Long> {
    Optional<SignalWeightConfig> findBySignalKey(SignalWeightKey signalKey);
}
