package io.polaris.sebrae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.polaris.sebrae.model.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>{
}