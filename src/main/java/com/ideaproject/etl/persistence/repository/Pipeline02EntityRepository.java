package com.ideaproject.etl.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ideaproject.etl.domain.entities.Entity2.Pipeline02Entity;

import java.util.List;
import java.util.Optional;


/**
 * Repository for pipeline 02 base entities.
 */
public interface Pipeline02EntityRepository extends JpaRepository<Pipeline02Entity, Long> {

    /**
     * Returns a pipeline 02 base entity by source id.
     * @param pipeline02EntityId source entity id
     * @return matching entity if present
     */
    Optional<Pipeline02Entity> getPipeline02EntityById(long pipeline02EntityId);

    /**
     * Gets a list of Pruefauftraege.
     * @return A list of Pruefauftraege
     */
    List<Pipeline02Entity> findAll();

    long count();
}

