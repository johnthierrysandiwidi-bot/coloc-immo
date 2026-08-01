package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Favori;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Favori entity.
 */
@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long>, JpaSpecificationExecutor<Favori> {
    @Query("select favori from Favori favori where favori.utilisateur.login = ?#{authentication.name}")
    List<Favori> findByUtilisateurIsCurrentUser();

    default Optional<Favori> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Favori> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Favori> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select favori from Favori favori left join fetch favori.annonce left join fetch favori.utilisateur",
        countQuery = "select count(favori) from Favori favori"
    )
    Page<Favori> findAllWithToOneRelationships(Pageable pageable);

    @Query("select favori from Favori favori left join fetch favori.annonce left join fetch favori.utilisateur")
    List<Favori> findAllWithToOneRelationships();

    @Query("select favori from Favori favori left join fetch favori.annonce left join fetch favori.utilisateur where favori.id =:id")
    Optional<Favori> findOneWithToOneRelationships(@Param("id") Long id);
}
