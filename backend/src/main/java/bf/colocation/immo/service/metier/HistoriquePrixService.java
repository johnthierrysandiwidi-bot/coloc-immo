package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.domain.Prix;
import bf.colocation.immo.domain.enumeration.Periodicite;
import bf.colocation.immo.repository.PrixRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Historique de prix — transparence des prix (pilier de la problématique).
 *
 * Enregistre chaque prix effectif d'un bien avec sa date d'effet, ce qui permet
 * d'afficher l'évolution et de détecter les hausses ou les baisses.
 */
@Service
@Transactional
public class HistoriquePrixService {

    private final PrixRepository prixRepository;

    public HistoriquePrixService(PrixRepository prixRepository) {
        this.prixRepository = prixRepository;
    }

    /**
     * Enregistre un nouveau prix pour un bien, seulement s'il diffère du dernier connu.
     * On évite ainsi de polluer l'historique avec des valeurs identiques.
     */
    public void enregistrer(Immobilier bien, Double montant, Periodicite periodicite, Double charges) {
        if (bien == null || montant == null) {
            return;
        }
        List<Prix> historique = prixRepository.findByImmobilierIdOrderByDateEffetDesc(bien.getId());
        if (!historique.isEmpty()) {
            Prix dernier = historique.get(0);
            boolean memePrix = dernier.getPrix() != null && dernier.getPrix().equals(montant);
            boolean memesCharges = java.util.Objects.equals(dernier.getCharges(), charges);
            if (memePrix && memesCharges) {
                return; // rien de neuf à historiser
            }
        }
        Prix p = new Prix();
        p.setPrix(montant);
        p.setCharges(charges);
        p.setPeriodicite(periodicite != null ? periodicite : Periodicite.MENSUEL);
        p.setDateEffet(LocalDate.now());
        p.setImmobilier(bien);
        p.setDescription(historique.isEmpty() ? "Prix initial" : "Mise à jour du prix");
        prixRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<Prix> historique(Long immobilierId) {
        return prixRepository.findByImmobilierIdOrderByDateEffetDesc(immobilierId);
    }
}
