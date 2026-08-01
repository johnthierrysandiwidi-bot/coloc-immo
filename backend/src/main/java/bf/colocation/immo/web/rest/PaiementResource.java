package bf.colocation.immo.web.rest;

import bf.colocation.immo.domain.enumeration.MoyenPaiement;
import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.service.dto.PaiementDTO;
import bf.colocation.immo.service.metier.PaiementService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Frais de visite et séquestre (module V2, passerelle simulée).
 *
 * @Transactional : la conversion en DTO lit des relations différées
 * (rendez-vous → annonce, payeur) après la couche service ; sans transaction
 * ouverte au niveau du contrôleur, on retomberait sur une LazyInitializationException.
 */
@RestController
@RequestMapping("/api/paiements")
@Transactional
public class PaiementResource {

    private final PaiementService paiementService;

    public PaiementResource(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    /** Le montant des frais, pour affichage à l'avance (RG23). */
    @GetMapping("/frais-de-visite")
    public ResponseEntity<Double> fraisDeVisite() {
        return ResponseEntity.ok(PaiementService.FRAIS_DE_VISITE);
    }

    /** Initie le paiement d'un rendez-vous (le crée ou retourne l'existant). */
    @PostMapping("/rendez-vous/{rendezVousId}/initier")
    public ResponseEntity<PaiementDTO> initier(@PathVariable Long rendezVousId) {
        return ResponseEntity.ok(PaiementDTO.de(paiementService.initier(rendezVousId)));
    }

    /** Simule le règlement via la passerelle (bouton « payer » du front). */
    @PostMapping("/{id}/simuler-reglement")
    public ResponseEntity<PaiementDTO> simuler(@PathVariable Long id, @RequestParam(required = false) MoyenPaiement moyen) {
        return ResponseEntity.ok(PaiementDTO.de(paiementService.simulerReglement(id, moyen)));
    }

    /** Le paiement associé à un rendez-vous, ou 204 s'il n'existe pas. */
    @GetMapping("/rendez-vous/{rendezVousId}")
    public ResponseEntity<PaiementDTO> pourRendezVous(@PathVariable Long rendezVousId) {
        var p = paiementService.pourRendezVous(rendezVousId);
        return p == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(PaiementDTO.de(p));
    }

    // ---- Réservé à l'administrateur ----

    @GetMapping("")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<List<PaiementDTO>> tous() {
        return ResponseEntity.ok(paiementService.tous().stream().map(PaiementDTO::de).toList());
    }

    @PostMapping("/{id}/liberer")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<PaiementDTO> liberer(@PathVariable Long id) {
        return ResponseEntity.ok(PaiementDTO.de(paiementService.libererApresVisite(id)));
    }

    @PostMapping("/{id}/rembourser")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<PaiementDTO> rembourser(@PathVariable Long id) {
        return ResponseEntity.ok(PaiementDTO.de(paiementService.rembourser(id)));
    }
}
