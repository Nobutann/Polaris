package io.polaris.sebrae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.polaris.sebrae.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>{
}
