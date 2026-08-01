package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Image;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Image entity.
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    // ---- Contrôle de propriété (anti-IDOR) : identifiant du titulaire légitime ----

    @Query("select i.immobilier.proprietaire.id from Image i where i.id = :id")
    Optional<Long> trouverProprietaireId(@Param("id") Long id);

    @Query("select i.immobilier.demarcheur.id from Image i where i.id = :id")
    Optional<Long> trouverDemarcheurId(@Param("id") Long id);

    /** Charge en une seule requête les photos de plusieurs biens (évite le N+1). */
    java.util.List<Image> findByImmobilierIdInOrderByOrdreAsc(java.util.Collection<Long> immobilierIds);

    default Optional<Image> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Image> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Image> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select image from Image image left join fetch image.immobilier", countQuery = "select count(image) from Image image")
    Page<Image> findAllWithToOneRelationships(Pageable pageable);

    @Query("select image from Image image left join fetch image.immobilier")
    List<Image> findAllWithToOneRelationships();

    @Query("select image from Image image left join fetch image.immobilier where image.id =:id")
    Optional<Image> findOneWithToOneRelationships(@Param("id") Long id);
}
