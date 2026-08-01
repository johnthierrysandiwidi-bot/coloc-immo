package bf.colocation.immo.repository;

import bf.colocation.immo.domain.ProfilDemarcheur;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ProfilDemarcheur entity.
 */
@Repository
public interface ProfilDemarcheurRepository extends JpaRepository<ProfilDemarcheur, Long> {

    /** Le profil métier d'un démarcheur, par l'id de son compte utilisateur. */
    Optional<ProfilDemarcheur> findByUtilisateurId(Long utilisateurId);

    /** Démarcheurs dont l'identité a été vérifiée : les seuls mandatables sur un bien. */
    @Query(
        "select p from ProfilDemarcheur p left join fetch p.utilisateur " +
        "where p.statutValidation = bf.colocation.immo.domain.enumeration.StatutValidation.VALIDE"
    )
    List<ProfilDemarcheur> findDemarcheursValides();

    @Query(
        "select profilDemarcheur from ProfilDemarcheur profilDemarcheur where profilDemarcheur.validePar.login = ?#{authentication.name}"
    )
    List<ProfilDemarcheur> findByValideParIsCurrentUser();

    default Optional<ProfilDemarcheur> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<ProfilDemarcheur> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<ProfilDemarcheur> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select profilDemarcheur from ProfilDemarcheur profilDemarcheur left join fetch profilDemarcheur.utilisateur left join fetch profilDemarcheur.validePar",
        countQuery = "select count(profilDemarcheur) from ProfilDemarcheur profilDemarcheur"
    )
    Page<ProfilDemarcheur> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select profilDemarcheur from ProfilDemarcheur profilDemarcheur left join fetch profilDemarcheur.utilisateur left join fetch profilDemarcheur.validePar"
    )
    List<ProfilDemarcheur> findAllWithToOneRelationships();

    @Query(
        "select profilDemarcheur from ProfilDemarcheur profilDemarcheur left join fetch profilDemarcheur.utilisateur left join fetch profilDemarcheur.validePar where profilDemarcheur.id =:id"
    )
    Optional<ProfilDemarcheur> findOneWithToOneRelationships(@Param("id") Long id);
}
