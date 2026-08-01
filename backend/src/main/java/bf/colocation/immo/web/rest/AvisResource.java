package bf.colocation.immo.web.rest;

import bf.colocation.immo.service.dto.AvisDTO;
import bf.colocation.immo.service.dto.ReputationDTO;
import bf.colocation.immo.service.metier.AvisService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Avis et réputation des démarcheurs.
 *
 * La réputation d'un démarcheur est publique — elle éclaire le choix d'un locataire.
 * Déposer un avis, en revanche, exige d'être connecté et d'avoir réellement visité.
 */
@RestController
@RequestMapping("/api")
public class AvisResource {

    private final AvisService avisService;

    public AvisResource(AvisService avisService) {
        this.avisService = avisService;
    }

    /** Réputation publique d'un démarcheur. */
    @GetMapping("/demarcheurs/{id}/reputation")
    public ResponseEntity<ReputationDTO> reputation(@PathVariable Long id) {
        return ResponseEntity.ok(avisService.reputation(id));
    }

    /** Dépose un avis à l'issue d'une visite effectuée. */
    @PostMapping("/rendez-vous/{id}/avis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AvisDTO> deposer(@PathVariable Long id, @RequestBody AvisVM vm) {
        return ResponseEntity.ok(avisService.deposer(id, vm.getNote(), vm.getCommentaire()));
    }

    /** Rendez-vous terminés que le locataire courant peut encore noter. */
    @GetMapping("/mes-visites-a-noter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Long>> aNoter() {
        return ResponseEntity.ok(avisService.rendezVousNotables());
    }

    /** Corps d'un dépôt d'avis. */
    public static class AvisVM {

        @Min(1)
        @Max(5)
        private Integer note;

        @Size(max = 1000)
        private String commentaire;

        public Integer getNote() {
            return note;
        }

        public void setNote(Integer note) {
            this.note = note;
        }

        public String getCommentaire() {
            return commentaire;
        }

        public void setCommentaire(String commentaire) {
            this.commentaire = commentaire;
        }
    }
}
