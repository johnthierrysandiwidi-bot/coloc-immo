package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Avis;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Accès aux avis et calcul de réputation.
 */
@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {
    /** Les avis reçus par un démarcheur, du plus récent au plus ancien. */
    @Query("select a from Avis a left join fetch a.auteur where a.demarcheur.id = :demarcheurId order by a.dateCreation desc")
    List<Avis> findByDemarcheur(@Param("demarcheurId") Long demarcheurId);

    /** Empêche un second avis sur le même rendez-vous. */
    Optional<Avis> findByRendezVousId(Long rendezVousId);

    /** Note moyenne d'un démarcheur, ou null s'il n'a pas encore d'avis. */
    @Query("select avg(a.note) from Avis a where a.demarcheur.id = :demarcheurId")
    Double moyennePour(@Param("demarcheurId") Long demarcheurId);

    /** Nombre d'avis reçus par un démarcheur. */
    long countByDemarcheurId(Long demarcheurId);
}
