package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Annonce;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Annonce entity.
 */
@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {
    // ---- Contrôle de propriété (anti-IDOR) : identifiant du titulaire légitime ----

    @Query("select a.auteur.id from Annonce a where a.id = :id")
    Optional<Long> trouverAuteurId(@Param("id") Long id);

    @Query("select annonce from Annonce annonce where annonce.auteur.login = ?#{authentication.name}")
    List<Annonce> findByAuteurIsCurrentUser();

    default Optional<Annonce> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Annonce> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Annonce> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select annonce from Annonce annonce left join fetch annonce.immobilier left join fetch annonce.auteur",
        countQuery = "select count(annonce) from Annonce annonce"
    )
    Page<Annonce> findAllWithToOneRelationships(Pageable pageable);

    @Query("select annonce from Annonce annonce left join fetch annonce.immobilier left join fetch annonce.auteur")
    List<Annonce> findAllWithToOneRelationships();

    @Query("select annonce from Annonce annonce left join fetch annonce.immobilier left join fetch annonce.auteur where annonce.id =:id")
    Optional<Annonce> findOneWithToOneRelationships(@Param("id") Long id);
}
