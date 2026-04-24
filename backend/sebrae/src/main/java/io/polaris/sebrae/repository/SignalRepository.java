package io.polaris.sebrae.repository;

import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalRepository extends JpaRepository<Signal, Long> {
    Page<Signal> findBySource(SignalSource source, Pageable pageable);
    List<Signal> findByCourseId(Long courseId);
    boolean existsBySourceAndExternalId(SignalSource source, String externalId);
}

