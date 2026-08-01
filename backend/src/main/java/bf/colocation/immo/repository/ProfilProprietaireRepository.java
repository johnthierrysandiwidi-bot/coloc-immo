package bf.colocation.immo.repository;

import bf.colocation.immo.domain.ProfilProprietaire;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ProfilProprietaire entity.
 */
@Repository
public interface ProfilProprietaireRepository extends JpaRepository<ProfilProprietaire, Long> {
    default Optional<ProfilProprietaire> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ProfilProprietaire> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ProfilProprietaire> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select profilProprietaire from ProfilProprietaire profilProprietaire left join fetch profilProprietaire.utilisateur",
        countQuery = "select count(profilProprietaire) from ProfilProprietaire profilProprietaire"
    )
    Page<ProfilProprietaire> findAllWithToOneRelationships(Pageable pageable);

    @Query("select profilProprietaire from ProfilProprietaire profilProprietaire left join fetch profilProprietaire.utilisateur")
    List<ProfilProprietaire> findAllWithToOneRelationships();

    @Query(
        "select profilProprietaire from ProfilProprietaire profilProprietaire left join fetch profilProprietaire.utilisateur where profilProprietaire.id =:id"
    )
    Optional<ProfilProprietaire> findOneWithToOneRelationships(@Param("id") Long id);
}
