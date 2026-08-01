package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Immobilier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Immobilier entity.
 */
@Repository
public interface ImmobilierRepository extends JpaRepository<Immobilier, Long>, JpaSpecificationExecutor<Immobilier> {
    // ---- Contrôle de propriété (anti-IDOR) : identifiant du titulaire légitime ----

    @Query("select i.proprietaire.id from Immobilier i where i.id = :id")
    Optional<Long> trouverProprietaireId(@Param("id") Long id);

    @Query("select i.demarcheur.id from Immobilier i where i.id = :id")
    Optional<Long> trouverDemarcheurId(@Param("id") Long id);

    @Query("select immobilier from Immobilier immobilier where immobilier.proprietaire.login = ?#{authentication.name}")
    List<Immobilier> findByProprietaireIsCurrentUser();

    @Query("select immobilier from Immobilier immobilier where immobilier.demarcheur.login = ?#{authentication.name}")
    List<Immobilier> findByDemarcheurIsCurrentUser();

    default Optional<Immobilier> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Immobilier> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Immobilier> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select immobilier from Immobilier immobilier left join fetch immobilier.proprietaire left join fetch immobilier.demarcheur left join fetch immobilier.localite left join fetch immobilier.quartier left join fetch immobilier.typeImmobilier",
        countQuery = "select count(immobilier) from Immobilier immobilier"
    )
    Page<Immobilier> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select immobilier from Immobilier immobilier left join fetch immobilier.proprietaire left join fetch immobilier.demarcheur left join fetch immobilier.localite left join fetch immobilier.quartier left join fetch immobilier.typeImmobilier"
    )
    List<Immobilier> findAllWithToOneRelationships();

    @Query(
        "select immobilier from Immobilier immobilier left join fetch immobilier.proprietaire left join fetch immobilier.demarcheur left join fetch immobilier.localite left join fetch immobilier.quartier left join fetch immobilier.typeImmobilier where immobilier.id =:id"
    )
    Optional<Immobilier> findOneWithToOneRelationships(@Param("id") Long id);
}
