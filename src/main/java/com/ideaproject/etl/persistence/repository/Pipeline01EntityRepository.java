package com.ideaproject.etl.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ideaproject.etl.domain.entities.entity1.Pipeline01Entity;

import java.util.List;
import java.util.Optional;

/**
 * Repository for pipeline 01 base entities.
 */
public interface Pipeline01EntityRepository extends JpaRepository<Pipeline01Entity, Long> {

    /**
     * Returns a pipeline 01 base entity by source id.
     * @param pipeline01EntityId source entity id
     * @return matching entity if present
     */
    Optional<Pipeline01Entity> getPipeline01EntityById(long pipeline01EntityId);

    /**
     * Gets a list of pipeline 01 base entities.
     * @return the list of pipeline 01 base entities
     */
    List<Pipeline01Entity> findAll();

    /**
     * Gets a list of pipeline 01 base entities in paged form.
     * @param pageable pagination for which we want to return the list of entities
     * @return the page containing the list of entities
     */
    Page<Pipeline01Entity> findAll(Pageable pageable);

    long count();
}

