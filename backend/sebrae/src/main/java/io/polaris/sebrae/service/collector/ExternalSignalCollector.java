package io.polaris.sebrae.service.collector;

import io.polaris.sebrae.model.Signal;
import java.util.List;

public interface ExternalSignalCollector {
    List<Signal> collect();
}
