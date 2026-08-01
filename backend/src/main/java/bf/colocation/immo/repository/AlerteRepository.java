package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Alerte;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Alerte entity.
 */
@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long>, JpaSpecificationExecutor<Alerte> {
    @Query("select alerte from Alerte alerte where alerte.titulaire.login = ?#{authentication.name}")
    List<Alerte> findByTitulaireIsCurrentUser();

    default Optional<Alerte> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Alerte> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Alerte> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select alerte from Alerte alerte left join fetch alerte.titulaire left join fetch alerte.localite left join fetch alerte.quartier left join fetch alerte.typeImmobilier",
        countQuery = "select count(alerte) from Alerte alerte"
    )
    Page<Alerte> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select alerte from Alerte alerte left join fetch alerte.titulaire left join fetch alerte.localite left join fetch alerte.quartier left join fetch alerte.typeImmobilier"
    )
    List<Alerte> findAllWithToOneRelationships();

    @Query(
        "select alerte from Alerte alerte left join fetch alerte.titulaire left join fetch alerte.localite left join fetch alerte.quartier left join fetch alerte.typeImmobilier where alerte.id =:id"
    )
    Optional<Alerte> findOneWithToOneRelationships(@Param("id") Long id);
}
