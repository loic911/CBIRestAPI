package org.cbir.retrieval.repository;

import org.cbir.retrieval.domain.Storach;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Storach entity.
 */
public interface StorachRepository extends JpaRepository<Storach, Long> {

}
