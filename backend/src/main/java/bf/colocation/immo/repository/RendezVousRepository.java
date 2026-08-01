package bf.colocation.immo.repository;

import bf.colocation.immo.domain.RendezVous;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the RendezVous entity.
 */
@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long>, JpaSpecificationExecutor<RendezVous> {
    @Query("select rendezVous from RendezVous rendezVous where rendezVous.demandeur.login = ?#{authentication.name}")
    List<RendezVous> findByDemandeurIsCurrentUser();

    /**
     * Rendez-vous visibles par un utilisateur : ceux qu'il a demandés (locataire) ET ceux qui portent
     * sur une annonce dont il est l'auteur (démarcheur / propriétaire). Utilisé pour scoper la liste
     * côté serveur et empêcher un utilisateur de voir les rendez-vous d'autrui.
     */
    /**
     * Un rendez-vous actif occupe-t-il déjà ce créneau sur cette annonce ?
     *
     * Le rapport (§4.4.3, exception E1) prévoit qu'un créneau réservé devienne
     * indisponible pour les autres locataires. Seuls les statuts encore vivants
     * comptent : un rendez-vous annulé ou refusé libère le créneau.
     */
    @Query(
        "select count(rv) > 0 from RendezVous rv " +
        "where rv.annonce.id = :annonceId " +
        "and rv.dateHeure = :dateHeure " +
        "and rv.statut in (bf.colocation.immo.domain.enumeration.StatutRendezVous.DEMANDE, " +
        "bf.colocation.immo.domain.enumeration.StatutRendezVous.ACCEPTE, " +
        "bf.colocation.immo.domain.enumeration.StatutRendezVous.REPORTE)"
    )
    boolean creneauDejaPris(@Param("annonceId") Long annonceId, @Param("dateHeure") java.time.Instant dateHeure);

    @Query(
        value = "select distinct rv from RendezVous rv left join fetch rv.annonce a left join fetch rv.demandeur " +
        "where rv.demandeur.id = :uid or a.auteur.id = :uid",
        countQuery = "select count(distinct rv) from RendezVous rv where rv.demandeur.id = :uid or rv.annonce.auteur.id = :uid"
    )
    Page<RendezVous> findVisiblesParUtilisateur(@Param("uid") Long uid, Pageable pageable);

    default Optional<RendezVous> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<RendezVous> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<RendezVous> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select rendezVous from RendezVous rendezVous left join fetch rendezVous.annonce left join fetch rendezVous.demandeur",
        countQuery = "select count(rendezVous) from RendezVous rendezVous"
    )
    Page<RendezVous> findAllWithToOneRelationships(Pageable pageable);

    @Query("select rendezVous from RendezVous rendezVous left join fetch rendezVous.annonce left join fetch rendezVous.demandeur")
    List<RendezVous> findAllWithToOneRelationships();

    @Query(
        "select rendezVous from RendezVous rendezVous left join fetch rendezVous.annonce left join fetch rendezVous.demandeur where rendezVous.id =:id"
    )
    Optional<RendezVous> findOneWithToOneRelationships(@Param("id") Long id);
}
