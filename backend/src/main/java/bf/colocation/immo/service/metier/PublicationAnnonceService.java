package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.*;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

/**
 * Publication d'une annonce (EF-04).
 *
 * C'est ici que se joue le verrou du projet : un démarcheur non validé ne publie pas.
 */
@Service
@Transactional
public class PublicationAnnonceService {

    private HistoriquePrixService historiquePrixService;

    @org.springframework.beans.factory.annotation.Autowired
    public void setHistoriquePrixService(HistoriquePrixService s) {
        this.historiquePrixService = s;
    }

    private static final long DUREE_VALIDITE_JOURS = 60;

    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final ValidationDemarcheurService validationDemarcheurService;
    private final ApplicationEventPublisher eventPublisher;

    public PublicationAnnonceService(
        AnnonceRepository annonceRepository,
        UserRepository userRepository,
        ValidationDemarcheurService validationDemarcheurService,
        ApplicationEventPublisher eventPublisher
    ) {
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
        this.validationDemarcheurService = validationDemarcheurService;
        this.eventPublisher = eventPublisher;
    }

    public Annonce publier(Long annonceId) {
        Annonce annonce = annonceRepository
            .findById(annonceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Annonce introuvable"));

        User courant = utilisateurCourant();
        verifierDroitDePublier(courant, annonce);
        verifierCoherenceColocation(annonce);

        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setDatePublication(Instant.now());
        if (annonce.getDateExpiration() == null) {
            annonce.setDateExpiration(Instant.now().plus(DUREE_VALIDITE_JOURS, ChronoUnit.DAYS));
        }
        if (annonce.getNombreVues() == null) {
            annonce.setNombreVues(0);
        }
        Annonce publiee = annonceRepository.save(annonce);

        // Transparence des prix : on historise le prix affiché du bien à la publication.
        if (publiee.getImmobilier() != null && publiee.getPrix() != null) {
            historiquePrixService.enregistrer(publiee.getImmobilier(), publiee.getPrix(), null, null);
        }

        // Le moteur d'alertes tourne APRÈS le commit, de façon asynchrone.
        eventPublisher.publishEvent(new AnnoncePublieeEvent(publiee.getId()));
        return publiee;
    }

    /** Retire l'annonce de la vitrine sans la détruire : elle repasse en brouillon. */
    public Annonce depublier(Long annonceId) {
        Annonce annonce = charger(annonceId);
        assurerProprietaire(annonceId);
        if (annonce.getStatut() == StatutAnnonce.CLOTUREE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une annonce archivée ne peut pas être dépubliée");
        }
        annonce.setStatut(StatutAnnonce.BROUILLON);
        return annonceRepository.save(annonce);
    }

    /** Archive définitivement : l'annonce quitte la vitrine et n'est plus modifiable. */
    public Annonce archiver(Long annonceId) {
        Annonce annonce = charger(annonceId);
        assurerProprietaire(annonceId);
        annonce.setStatut(StatutAnnonce.CLOTUREE);
        return annonceRepository.save(annonce);
    }

    /**
     * Renouvelle une annonce expirée : sans cela le démarcheur est dans une impasse,
     * puisque AnnonceSchedulerService fait expirer les annonces automatiquement.
     * Le verrou du démarcheur s'applique de nouveau : un compte suspendu ne renouvelle pas.
     */
    public Annonce renouveler(Long annonceId) {
        Annonce annonce = charger(annonceId);
        User courant = utilisateurCourant();
        verifierDroitDePublier(courant, annonce);

        if (annonce.getStatut() != StatutAnnonce.EXPIREE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une annonce expirée peut être renouvelée");
        }
        annonce.setStatut(StatutAnnonce.PUBLIEE);
        annonce.setDatePublication(Instant.now());
        annonce.setDateExpiration(Instant.now().plus(DUREE_VALIDITE_JOURS, ChronoUnit.DAYS));
        return annonceRepository.save(annonce);
    }

    private Annonce charger(Long annonceId) {
        return annonceRepository
            .findById(annonceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Annonce introuvable"));
    }

    /**
     * Garde anti-IDOR pour la modification et la suppression.
     * Le contrôleur généré par JHipster n'effectue aucun contrôle de propriété :
     * sans cet appel, n'importe quel compte authentifié peut supprimer l'annonce d'autrui.
     */
    @Transactional(readOnly = true)
    public void assurerProprietaire(Long annonceId) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return;
        }
        Annonce annonce = annonceRepository
            .findById(annonceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Annonce introuvable"));

        User courant = utilisateurCourant();
        if (annonce.getAuteur() == null || !annonce.getAuteur().getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette annonce ne vous appartient pas");
        }
    }

    /** EF-02.1 : le verrou du démarcheur. */
    private void verifierDroitDePublier(User courant, Annonce annonce) {
        boolean estAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        if (estAdmin) {
            return;
        }

        if (annonce.getAuteur() == null || !annonce.getAuteur().getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette annonce ne vous appartient pas");
        }

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.DEMARCHEUR) &&
            !validationDemarcheurService.peutPublier(courant)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Vos documents ne sont pas encore validés : la publication est bloquée (EF-02.1)"
            );
        }
    }

    /** EF-05 : une annonce de colocation doit porter son détail. */
    private void verifierCoherenceColocation(Annonce annonce) {
        if (annonce.getType() == TypeAnnonce.COLOCATION && annonce.getDetailColocation() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Une annonce de colocation doit renseigner son détail (places, loyer, caution...)"
            );
        }
    }

    private User utilisateurCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non identifié"));
    }

    /** Événement publié à chaque mise en ligne : déclenche le moteur d'alertes. */
    public record AnnoncePublieeEvent(Long annonceId) {}
}
