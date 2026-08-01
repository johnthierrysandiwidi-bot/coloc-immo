package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Localite;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Localite entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LocaliteRepository extends JpaRepository<Localite, Long> {}
