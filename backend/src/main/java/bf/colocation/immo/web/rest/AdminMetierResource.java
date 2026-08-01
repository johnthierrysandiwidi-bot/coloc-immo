package bf.colocation.immo.web.rest;

import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.service.dto.AdminUserDTO;
import bf.colocation.immo.service.metier.AdministrationService;
import bf.colocation.immo.service.metier.StatistiqueService;
import bf.colocation.immo.service.metier.VueAnnonceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints d'administration, de statistiques et de comptage de vues.
 */
@RestController
@RequestMapping("/api")
public class AdminMetierResource {

    private final AdministrationService administrationService;
    private final StatistiqueService statistiqueService;
    private final VueAnnonceService vueAnnonceService;

    public AdminMetierResource(
        AdministrationService administrationService,
        StatistiqueService statistiqueService,
        VueAnnonceService vueAnnonceService
    ) {
        this.administrationService = administrationService;
        this.statistiqueService = statistiqueService;
        this.vueAnnonceService = vueAnnonceService;
    }

    // ---------- Administration (EF-01.6) ----------

    @PatchMapping("/admin/utilisateurs/{id}/suspendre")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> suspendre(@PathVariable Long id) {
        return ResponseEntity.ok(new AdminUserDTO(administrationService.suspendre(id)));
    }

    @PatchMapping("/admin/utilisateurs/{id}/reactiver")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> reactiver(@PathVariable Long id) {
        return ResponseEntity.ok(new AdminUserDTO(administrationService.reactiver(id)));
    }

    @PatchMapping("/admin/utilisateurs/{id}/role")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> attribuerRole(@PathVariable Long id, @RequestBody RoleVM vm) {
        return ResponseEntity.ok(new AdminUserDTO(administrationService.attribuerRole(id, vm.getRole())));
    }

    @PostMapping("/admin/utilisateurs/{id}/notifier")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> notifier(@PathVariable Long id, @RequestBody NotificationVM vm) {
        administrationService.notifierManuellement(id, vm.getTitre(), vm.getMessage());
        return ResponseEntity.accepted().build();
    }

    // ---------- Statistiques (EF-11) ----------

    @GetMapping("/statistiques/administrateur")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Map<String, Object>> statsAdmin() {
        return ResponseEntity.ok(statistiqueService.pourAdministrateur());
    }

    @GetMapping("/statistiques/bailleur")
    @PreAuthorize(
        "hasAnyAuthority(\"" + AuthoritiesConstants.PROPRIETAIRE + "\", \"" + AuthoritiesConstants.DEMARCHEUR + "\", \"" +
        AuthoritiesConstants.ADMIN + "\")"
    )
    public ResponseEntity<Map<String, Object>> statsBailleur() {
        return ResponseEntity.ok(statistiqueService.pourBailleur());
    }

    @GetMapping("/statistiques/utilisateur")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> statsUtilisateur() {
        return ResponseEntity.ok(statistiqueService.pourUtilisateur());
    }

    // ---------- Vue d'une annonce (EF-04.4) ----------

    @PostMapping("/annonces/{id}/vue")
    public ResponseEntity<Void> enregistrerVue(@PathVariable Long id, HttpServletRequest request) {
        vueAnnonceService.enregistrer(id, request.getRemoteAddr());
        return ResponseEntity.accepted().build();
    }

    // ---------- View models ----------

    public static class RoleVM {

        @NotBlank
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class NotificationVM {

        @NotBlank
        private String titre;

        @NotBlank
        private String message;

        public String getTitre() {
            return titre;
        }

        public void setTitre(String titre) {
            this.titre = titre;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
