package bf.colocation.immo.repository;

import bf.colocation.immo.domain.AlerteNotifiee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AlerteNotifiee entity.
 */
@Repository
public interface AlerteNotifieeRepository extends JpaRepository<AlerteNotifiee, Long> {
    // ---- Contrôle de propriété (anti-IDOR) : identifiant du titulaire légitime ----

    @Query("select a.alerte.titulaire.id from AlerteNotifiee a where a.id = :id")
    Optional<Long> trouverTitulaireId(@Param("id") Long id);

    default Optional<AlerteNotifiee> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<AlerteNotifiee> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<AlerteNotifiee> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select alerteNotifiee from AlerteNotifiee alerteNotifiee left join fetch alerteNotifiee.alerte left join fetch alerteNotifiee.annonce",
        countQuery = "select count(alerteNotifiee) from AlerteNotifiee alerteNotifiee"
    )
    Page<AlerteNotifiee> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select alerteNotifiee from AlerteNotifiee alerteNotifiee left join fetch alerteNotifiee.alerte left join fetch alerteNotifiee.annonce"
    )
    List<AlerteNotifiee> findAllWithToOneRelationships();

    @Query(
        "select alerteNotifiee from AlerteNotifiee alerteNotifiee left join fetch alerteNotifiee.alerte left join fetch alerteNotifiee.annonce where alerteNotifiee.id =:id"
    )
    Optional<AlerteNotifiee> findOneWithToOneRelationships(@Param("id") Long id);
}
