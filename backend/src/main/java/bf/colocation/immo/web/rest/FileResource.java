package bf.colocation.immo.web.rest;

import bf.colocation.immo.repository.DocumentRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.service.security.AutorisationService;
import bf.colocation.immo.service.storage.FileStorageService;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Upload et service des images et documents. */
@RestController
@RequestMapping("/api/files")
public class FileResource {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final AutorisationService autorisationService;

    public FileResource(
        FileStorageService fileStorageService,
        DocumentRepository documentRepository,
        AutorisationService autorisationService
    ) {
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
        this.autorisationService = autorisationService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(
        "hasAnyAuthority(\"" +
        AuthoritiesConstants.PROPRIETAIRE +
        "\", \"" +
        AuthoritiesConstants.DEMARCHEUR +
        "\", \"" +
        AuthoritiesConstants.ADMIN +
        "\")"
    )
    public ResponseEntity<Map<String, String>> uploaderImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.stockerImage(file)));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploaderDocument(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.stockerDocument(file)));
    }

    /**
     * Sert un fichier stocké.
     *
     * <p>Les images illustrent des annonces publiques : elles restent librement accessibles.
     * Les pièces justificatives (CNIB, attestations…) sont des données personnelles sensibles :
     * l'URL, même difficile à deviner, ne constitue pas une protection — elle fuit par
     * l'historique du navigateur, les journaux serveur ou un simple partage de lien, et donne
     * alors un accès définitif. On exige donc que l'appelant soit le démarcheur propriétaire
     * du document ou un administrateur.</p>
     */
    @GetMapping("/{dossier}/{nom:.+}")
    public ResponseEntity<Resource> telecharger(@PathVariable String dossier, @PathVariable String nom) {
        if ("documents".equals(dossier)) {
            String url = "/api/files/documents/" + nom;
            Long proprietaire = documentRepository
                .findFirstByUrl(url)
                .map(d -> d.getDemarcheur() != null ? d.getDemarcheur().getId() : null)
                .orElse(null);
            autorisationService.exigerProprietaireOuAdmin(proprietaire);
        }
        Resource resource = fileStorageService.charger(dossier, nom);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
}
