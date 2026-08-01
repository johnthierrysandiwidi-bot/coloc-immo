package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.dto.DocumentDTO;
import bf.colocation.immo.service.dto.RendezVousDTO;
import bf.colocation.immo.service.mapper.AnnonceMapper;
import bf.colocation.immo.service.mapper.DocumentMapper;
import bf.colocation.immo.service.mapper.RendezVousMapper;
import bf.colocation.immo.service.metier.DocumentMetierService;
import bf.colocation.immo.service.metier.PublicationAnnonceService;
import bf.colocation.immo.service.metier.ValidationDemarcheurService;
import bf.colocation.immo.service.metier.WorkflowRendezVousService;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints des transitions métier — ce que le CRUD généré ne couvre pas.
 */
@RestController
@RequestMapping("/api")
// La conversion entité -> DTO a lieu ici, dans le contrôleur. Or JHipster désactive
// open-in-view : sans transaction ouverte, MapStruct ne peut plus résoudre les
// relations différées (localite, quartier, typeImmobilier) et lève une
// LazyInitializationException. @Transactional maintient la session le temps du mapping.
@Transactional
public class WorkflowResource {

    private final ValidationDemarcheurService validationDemarcheurService;
    private final PublicationAnnonceService publicationAnnonceService;
    private final DocumentMetierService documentMetierService;
    private final WorkflowRendezVousService workflowRendezVousService;
    private final DocumentMapper documentMapper;
    private final AnnonceMapper annonceMapper;
    private final RendezVousMapper rendezVousMapper;

    public WorkflowResource(
        ValidationDemarcheurService validationDemarcheurService,
        PublicationAnnonceService publicationAnnonceService,
        DocumentMetierService documentMetierService,
        WorkflowRendezVousService workflowRendezVousService,
        DocumentMapper documentMapper,
        AnnonceMapper annonceMapper,
        RendezVousMapper rendezVousMapper
    ) {
        this.validationDemarcheurService = validationDemarcheurService;
        this.publicationAnnonceService = publicationAnnonceService;
        this.documentMetierService = documentMetierService;
        this.workflowRendezVousService = workflowRendezVousService;
        this.documentMapper = documentMapper;
        this.annonceMapper = annonceMapper;
        this.rendezVousMapper = rendezVousMapper;
    }

    // ---------- Documents : seul l'admin décide (EF-02.4) ----------

    @PatchMapping("/documents/{id}/valider")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<DocumentDTO> validerDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentMapper.toDto(validationDemarcheurService.valider(id)));
    }

    @PatchMapping("/documents/{id}/refuser")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<DocumentDTO> refuserDocument(@PathVariable Long id, @RequestBody MotifVM motif) {
        return ResponseEntity.ok(documentMapper.toDto(validationDemarcheurService.refuser(id, motif.getMotif())));
    }

    // ---------- Annonces ----------

    @PatchMapping("/annonces/{id}/publier")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnnonceDTO> publierAnnonce(@PathVariable Long id) {
        return ResponseEntity.ok(annonceMapper.toDto(publicationAnnonceService.publier(id)));
    }

    // ---------- Rendez-vous (EF-06) ----------

    @PostMapping("/rendez-vous/demander")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RendezVousDTO> demanderVisite(@RequestBody DemandeVisiteVM vm) {
        var rv = workflowRendezVousService.demander(vm.getAnnonceId(), vm.getDateSouhaitee(), vm.getMessage());
        return ResponseEntity.ok(rendezVousMapper.toDto(rv));
    }

    @PatchMapping("/rendez-vous/{id}/accepter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RendezVousDTO> accepter(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousMapper.toDto(workflowRendezVousService.accepter(id)));
    }

    @PatchMapping("/rendez-vous/{id}/refuser")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RendezVousDTO> refuser(@PathVariable Long id, @RequestBody MotifVM motif) {
        return ResponseEntity.ok(rendezVousMapper.toDto(workflowRendezVousService.refuser(id, motif.getMotif())));
    }

    @PatchMapping("/rendez-vous/{id}/reporter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RendezVousDTO> reporter(@PathVariable Long id, @RequestBody ReportVM vm) {
        return ResponseEntity.ok(rendezVousMapper.toDto(workflowRendezVousService.reporter(id, vm.getNouvelleDate())));
    }

    /**
     * Clôture d'une visite effectivement réalisée.
     *
     * Ouvert au titulaire de l'annonce comme au locataire : le service distingue
     * ensuite qui a parlé, car la conséquence sur les fonds séquestrés en dépend.
     */
    @PatchMapping("/rendez-vous/{id}/terminer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RendezVousDTO> terminer(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousMapper.toDto(workflowRendezVousService.terminer(id)));
    }

    @PatchMapping("/rendez-vous/{id}/annuler")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RendezVousDTO> annuler(@PathVariable Long id, @RequestBody(required = false) MotifVM motif) {
        String m = motif == null ? null : motif.getMotif();
        return ResponseEntity.ok(rendezVousMapper.toDto(workflowRendezVousService.annuler(id, m)));
    }

    // ---------- View models ----------

    // --- Cycle de vie de l'annonce (dépublier / archiver / renouveler) ---

    @PatchMapping("/annonces/{id}/depublier")
    public ResponseEntity<AnnonceDTO> depublier(@PathVariable Long id) {
        return ResponseEntity.ok(annonceMapper.toDto(publicationAnnonceService.depublier(id)));
    }

    @PatchMapping("/annonces/{id}/archiver")
    public ResponseEntity<AnnonceDTO> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(annonceMapper.toDto(publicationAnnonceService.archiver(id)));
    }

    @PatchMapping("/annonces/{id}/renouveler")
    public ResponseEntity<AnnonceDTO> renouveler(@PathVariable Long id) {
        return ResponseEntity.ok(annonceMapper.toDto(publicationAnnonceService.renouveler(id)));
    }

    // --- Pièces justificatives du démarcheur ---

    @PutMapping("/documents/{id}/remplacer")
    public ResponseEntity<DocumentDTO> remplacerDocument(@PathVariable Long id, @RequestBody RemplacementVM vm) {
        return ResponseEntity.ok(documentMapper.toDto(documentMetierService.remplacer(id, vm.getUrl(), vm.getNom())));
    }

    @DeleteMapping("/documents/{id}/retirer")
    public ResponseEntity<Void> retirerDocument(@PathVariable Long id) {
        documentMetierService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/documents/{id}/telecharger")
    public ResponseEntity<String> telechargerDocument(@PathVariable Long id) {
        return ResponseEntity.ok(documentMetierService.urlDeTelechargement(id));
    }

    public static class RemplacementVM {

        @NotNull
        private String url;

        private String nom;

        public String getUrl() { return url; }

        public void setUrl(String url) { this.url = url; }

        public String getNom() { return nom; }

        public void setNom(String nom) { this.nom = nom; }
    }

    public static class MotifVM {

        private String motif;

        public String getMotif() { return motif; }
        public void setMotif(String motif) { this.motif = motif; }
    }

    public static class ReportVM {

        @NotNull
        private Instant nouvelleDate;

        public Instant getNouvelleDate() { return nouvelleDate; }
        public void setNouvelleDate(Instant nouvelleDate) { this.nouvelleDate = nouvelleDate; }
    }

    public static class DemandeVisiteVM {

        @NotNull
        private Long annonceId;

        @NotNull
        private Instant dateSouhaitee;

        private String message;

        public Long getAnnonceId() { return annonceId; }
        public void setAnnonceId(Long annonceId) { this.annonceId = annonceId; }
        public Instant getDateSouhaitee() { return dateSouhaitee; }
        public void setDateSouhaitee(Instant dateSouhaitee) { this.dateSouhaitee = dateSouhaitee; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
