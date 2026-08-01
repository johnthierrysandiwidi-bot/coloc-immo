package bf.colocation.immo.repository;

import bf.colocation.immo.domain.TypeImmobilier;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TypeImmobilier entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypeImmobilierRepository extends JpaRepository<TypeImmobilier, Long> {}
