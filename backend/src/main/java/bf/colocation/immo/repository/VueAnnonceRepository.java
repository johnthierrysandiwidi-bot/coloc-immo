package bf.colocation.immo.repository;

import bf.colocation.immo.domain.VueAnnonce;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the VueAnnonce entity.
 */
@Repository
public interface VueAnnonceRepository extends JpaRepository<VueAnnonce, Long> {
    // ---- Contrôle de propriété (anti-IDOR) : identifiant du titulaire légitime ----

    @Query("select v.utilisateur.id from VueAnnonce v where v.id = :id")
    Optional<Long> trouverUtilisateurId(@Param("id") Long id);

    @Query("select vueAnnonce from VueAnnonce vueAnnonce where vueAnnonce.utilisateur.login = ?#{authentication.name}")
    List<VueAnnonce> findByUtilisateurIsCurrentUser();

    default Optional<VueAnnonce> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<VueAnnonce> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<VueAnnonce> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select vueAnnonce from VueAnnonce vueAnnonce left join fetch vueAnnonce.annonce left join fetch vueAnnonce.utilisateur",
        countQuery = "select count(vueAnnonce) from VueAnnonce vueAnnonce"
    )
    Page<VueAnnonce> findAllWithToOneRelationships(Pageable pageable);

    @Query("select vueAnnonce from VueAnnonce vueAnnonce left join fetch vueAnnonce.annonce left join fetch vueAnnonce.utilisateur")
    List<VueAnnonce> findAllWithToOneRelationships();

    @Query(
        "select vueAnnonce from VueAnnonce vueAnnonce left join fetch vueAnnonce.annonce left join fetch vueAnnonce.utilisateur where vueAnnonce.id =:id"
    )
    Optional<VueAnnonce> findOneWithToOneRelationships(@Param("id") Long id);
}
