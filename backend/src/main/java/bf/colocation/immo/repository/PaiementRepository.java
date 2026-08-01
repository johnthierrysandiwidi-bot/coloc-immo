package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Paiement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    Optional<Paiement> findByRendezVousId(Long rendezVousId);

    @Query("select p from Paiement p left join fetch p.rendezVous rv left join fetch rv.annonce left join fetch p.payeur order by p.dateCreation desc")
    List<Paiement> findAllDetaille();

    boolean existsByRendezVousId(Long rendezVousId);
}
