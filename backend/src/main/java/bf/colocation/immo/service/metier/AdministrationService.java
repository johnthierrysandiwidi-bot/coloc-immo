package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Authority;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.AuthorityRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Pouvoirs de l'administrateur (EF-01.6) : suspendre un compte, attribuer un rôle,
 * envoyer une notification manuelle.
 */
@Service
@Transactional
public class AdministrationService {

    private static final Logger LOG = LoggerFactory.getLogger(AdministrationService.class);

    /** Rôles métier attribuables par l'admin. */
    private static final Set<String> ROLES_METIER = Set.of(
        AuthoritiesConstants.UTILISATEUR,
        AuthoritiesConstants.PROPRIETAIRE,
        AuthoritiesConstants.DEMARCHEUR,
        AuthoritiesConstants.ADMIN
    );

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final AnnonceRepository annonceRepository;
    private final NotificationMetierService notificationService;

    public AdministrationService(
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        AnnonceRepository annonceRepository,
        NotificationMetierService notificationService
    ) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.annonceRepository = annonceRepository;
        this.notificationService = notificationService;
    }

    /**
     * Suspend un compte : l'utilisateur ne peut plus s'authentifier et
     * ses annonces publiées sont masquées (EF-01.6).
     */
    public User suspendre(Long userId) {
        User user = charger(userId);

        if (user.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.ADMIN.equals(a.getName()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Un administrateur ne peut pas être suspendu");
        }

        user.setActivated(false);
        userRepository.save(user);

        List<Annonce> aMasquer = annonceRepository
            .findAll()
            .stream()
            .filter(a -> a.getAuteur() != null && a.getAuteur().getId().equals(userId))
            .filter(a -> a.getStatut() == StatutAnnonce.PUBLIEE)
            .toList();

        for (Annonce a : aMasquer) {
            a.setStatut(StatutAnnonce.SUSPENDUE);
            annonceRepository.save(a);
        }

        notificationService.notifier(
            user,
            TypeNotification.COMPTE_SUSPENDU,
            "Compte suspendu",
            "Votre compte a été suspendu par un administrateur. " + aMasquer.size() + " annonce(s) ont été masquée(s).",
            "/"
        );

        LOG.info("Compte {} suspendu, {} annonce(s) masquée(s)", user.getLogin(), aMasquer.size());
        return user;
    }

    public User reactiver(Long userId) {
        User user = charger(userId);
        user.setActivated(true);
        userRepository.save(user);
        LOG.info("Compte {} réactivé", user.getLogin());
        return user;
    }

    /** Attribue un rôle métier. Utile quand l'inscription n'a pas posé le bon rôle. */
    public User attribuerRole(Long userId, String role) {
        if (!ROLES_METIER.contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle inconnu : " + role);
        }
        User user = charger(userId);
        Authority autorite = authorityRepository
            .findById(role)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle absent en base : " + role));

        Set<Authority> autorites = new HashSet<>(user.getAuthorities());
        autorites.add(autorite);
        user.setAuthorities(autorites);
        userRepository.save(user);

        LOG.info("Rôle {} attribué à {}", role, user.getLogin());
        return user;
    }

    /** Notification manuelle de l'administrateur (EF-09). */
    public void notifierManuellement(Long userId, String titre, String message) {
        notificationService.notifier(charger(userId), TypeNotification.NOUVELLE_ANNONCE, titre, message, "/notifications");
    }

    private User charger(Long id) {
        return userRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }
}
