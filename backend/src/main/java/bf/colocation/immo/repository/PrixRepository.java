package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Prix;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Prix entity.
 */
@Repository
public interface PrixRepository extends JpaRepository<Prix, Long> {
    // ---- Contrôle de propriété (anti-IDOR) : identifiant du titulaire légitime ----

    @Query("select p.immobilier.proprietaire.id from Prix p where p.id = :id")
    Optional<Long> trouverProprietaireId(@Param("id") Long id);

    @Query("select p.immobilier.demarcheur.id from Prix p where p.id = :id")
    Optional<Long> trouverDemarcheurId(@Param("id") Long id);

    default Optional<Prix> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Prix> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Prix> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select prix from Prix prix left join fetch prix.immobilier", countQuery = "select count(prix) from Prix prix")
    Page<Prix> findAllWithToOneRelationships(Pageable pageable);

    @Query("select prix from Prix prix left join fetch prix.immobilier")
    List<Prix> findAllWithToOneRelationships();

    @Query("select prix from Prix prix left join fetch prix.immobilier where prix.id =:id")
    Optional<Prix> findOneWithToOneRelationships(@Param("id") Long id);

    /** Historique des prix d'un bien, du plus récent au plus ancien. */
    List<Prix> findByImmobilierIdOrderByDateEffetDesc(Long immobilierId);
}
