package bf.colocation.immo.web.rest;

import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.security.SecurityUtils;
import bf.colocation.immo.service.metier.NotificationMetierService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Point de contact vers l'administration.
 *
 * Permet à toute personne — visiteur ou utilisateur connecté — d'adresser un
 * message aux administrateurs. Chaque administrateur actif reçoit une notification
 * interne ; l'expéditeur, s'il est connecté, est identifié automatiquement.
 *
 * L'accès est volontairement public : un visiteur bloqué à l'inscription ou à la
 * connexion doit pouvoir joindre l'équipe, précisément dans les cas où il n'a pas
 * encore de compte.
 */
@RestController
@RequestMapping("/api")
public class ContactResource {

    private static final Logger LOG = LoggerFactory.getLogger(ContactResource.class);

    private final UserRepository userRepository;
    private final NotificationMetierService notificationService;

    public ContactResource(UserRepository userRepository, NotificationMetierService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @PostMapping("/contact")
    public ResponseEntity<Void> envoyer(@Valid @RequestBody MessageContactVM vm) {
        String expediteur = SecurityUtils.getCurrentUserLogin().orElse(vm.getEmail());
        String titre = "Message de contact — " + vm.getSujet();
        String corps = "De : " + (expediteur != null ? expediteur : "visiteur") +
            (vm.getEmail() != null ? " (" + vm.getEmail() + ")" : "") + "\n\n" + vm.getMessage();

        List<User> admins = userRepository.findActifsParAutorite(AuthoritiesConstants.ADMIN);
        for (User admin : admins) {
            notificationService.notifier(admin, TypeNotification.MESSAGE_CONTACT, titre, corps, "/admin/notifications");
        }
        LOG.info("Message de contact transmis à {} administrateur(s).", admins.size());
        return ResponseEntity.accepted().build();
    }

    /** Corps du message de contact. */
    public static class MessageContactVM {

        @NotBlank
        @Size(max = 120)
        private String sujet;

        @Size(max = 120)
        private String email;

        @NotBlank
        @Size(max = 2000)
        private String message;

        public String getSujet() {
            return sujet;
        }

        public void setSujet(String sujet) {
            this.sujet = sujet;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
