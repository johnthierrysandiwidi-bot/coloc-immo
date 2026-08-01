package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cycle de vie d'un rendez-vous (EF-06) : demander → accepter | refuser | reporter | annuler.
 * Chaque transition notifie la partie adverse.
 */
@Service
@Transactional
public class WorkflowRendezVousService {

    /** États depuis lesquels le propriétaire peut encore agir. */
    private static final Set<StatutRendezVous> EN_COURS = EnumSet.of(StatutRendezVous.DEMANDE, StatutRendezVous.REPORTE);

    private final RendezVousRepository rendezVousRepository;
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final NotificationMetierService notificationService;
    private final PaiementService paiementService;

    public WorkflowRendezVousService(
        RendezVousRepository rendezVousRepository,
        AnnonceRepository annonceRepository,
        UserRepository userRepository,
        NotificationMetierService notificationService,
        PaiementService paiementService
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.paiementService = paiementService;
    }

    /** EF-06.1 + EF-06.5 : un seul rendez-vous actif par annonce et par utilisateur. */
    public RendezVous demander(Long annonceId, Instant dateSouhaitee, String message) {
        User demandeur = utilisateurCourant();
        Annonce annonce = annonceRepository
            .findById(annonceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Annonce introuvable"));

        if (annonce.getStatut() != StatutAnnonce.PUBLIEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette annonce n'est pas publiée");
        }

        // Cohérence de la date : le client borne déjà la saisie, mais le serveur ne doit
        // jamais s'y fier (un appel direct à l'API contournerait le formulaire).
        if (dateSouhaitee == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date souhaitée est obligatoire");
        }
        if (dateSouhaitee.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date souhaitée doit être dans le futur");
        }
        if (dateSouhaitee.isAfter(Instant.now().plus(365, java.time.temporal.ChronoUnit.DAYS))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date souhaitée ne peut pas dépasser un an");
        }

        // On ne demande pas une visite de son propre bien.
        if (annonce.getAuteur() != null && annonce.getAuteur().getId().equals(demandeur.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous ne pouvez pas demander une visite de votre propre annonce");
        }

        boolean dejaActif = rendezVousRepository
            .findAll()
            .stream()
            .anyMatch(rv ->
                rv.getAnnonce() != null && rv.getAnnonce().getId().equals(annonceId) &&
                rv.getDemandeur() != null && rv.getDemandeur().getId().equals(demandeur.getId()) &&
                (rv.getStatut() == StatutRendezVous.DEMANDE || rv.getStatut() == StatutRendezVous.ACCEPTE)
            );
        if (dejaActif) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez déjà un rendez-vous actif sur cette annonce");
        }

        // Un créneau déjà retenu par un autre locataire n'est plus proposable
        // (rapport §4.4.3, exception E1). Un rendez-vous annulé ou refusé le libère.
        if (rendezVousRepository.creneauDejaPris(annonceId, dateSouhaitee)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ce créneau vient d'être réservé. Choisissez une autre date ou une autre heure."
            );
        }

        RendezVous rv = new RendezVous();
        rv.setAnnonce(annonce);
        rv.setDemandeur(demandeur);
        rv.setDateHeure(dateSouhaitee);
        rv.setContenu(message);
        rv.setStatut(StatutRendezVous.DEMANDE);
        RendezVous saved = rendezVousRepository.save(rv);

        notifier(annonce.getAuteur(), TypeNotification.RDV_DEMANDE, "Nouvelle demande de visite",
            demandeur.getLogin() + " souhaite visiter « " + annonce.getTitre() + " »", saved);
        return saved;
    }

    public RendezVous accepter(Long id) {
        RendezVous rv = chargerPourAuteur(id);
        exigerStatut(rv, EN_COURS);
        rv.setStatut(StatutRendezVous.ACCEPTE);
        rendezVousRepository.save(rv);
        notifier(rv.getDemandeur(), TypeNotification.RDV_ACCEPTE, "Rendez-vous accepté",
            "Votre visite de « " + rv.getAnnonce().getTitre() + " » est confirmée.", rv);
        return rv;
    }

    public RendezVous refuser(Long id, String motif) {
        RendezVous rv = chargerPourAuteur(id);
        exigerStatut(rv, EN_COURS);
        rv.setStatut(StatutRendezVous.REFUSE);
        rv.setMotif(motif);
        rendezVousRepository.save(rv);
        notifier(rv.getDemandeur(), TypeNotification.RDV_REFUSE, "Rendez-vous refusé",
            "Votre demande de visite a été refusée." + (motif != null ? " Motif : " + motif : ""), rv);
        return rv;
    }

    public RendezVous reporter(Long id, Instant nouvelleDate) {
        RendezVous rv = chargerPourAuteur(id);
        exigerStatut(rv, EN_COURS);
        if (nouvelleDate == null || nouvelleDate.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nouvelle date doit être dans le futur");
        }
        rv.setDateReportee(nouvelleDate);
        rv.setStatut(StatutRendezVous.REPORTE);
        rendezVousRepository.save(rv);
        notifier(rv.getDemandeur(), TypeNotification.RDV_REPORTE, "Rendez-vous reporté",
            "Une nouvelle date vous est proposée pour « " + rv.getAnnonce().getTitre() + " ».", rv);
        return rv;
    }

    /** EF-06.3 : les deux parties peuvent annuler tant que la date n'est pas passée. */
    public RendezVous annuler(Long id, String motif) {
        RendezVous rv = charger(id);
        User courant = utilisateurCourant();

        boolean estDemandeur = rv.getDemandeur() != null && rv.getDemandeur().getId().equals(courant.getId());
        boolean estAuteur = rv.getAnnonce().getAuteur() != null && rv.getAnnonce().getAuteur().getId().equals(courant.getId());
        boolean estAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        if (!estDemandeur && !estAuteur && !estAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas partie à ce rendez-vous");
        }

        Instant effective = rv.getDateReportee() != null ? rv.getDateReportee() : rv.getDateHeure();
        if (effective != null && effective.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce rendez-vous est déjà passé");
        }

        rv.setStatut(StatutRendezVous.ANNULE);
        rv.setMotif(motif);
        rendezVousRepository.save(rv);

        User destinataire = estDemandeur ? rv.getAnnonce().getAuteur() : rv.getDemandeur();
        notifier(destinataire, TypeNotification.RDV_REFUSE, "Rendez-vous annulé",
            "Le rendez-vous sur « " + rv.getAnnonce().getTitre() + " » a été annulé.", rv);
        return rv;
    }

    // ---------- utilitaires ----------

    private RendezVous charger(Long id) {
        return rendezVousRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rendez-vous introuvable"));
    }

    /** Seul l'auteur de l'annonce (propriétaire/démarcheur) — ou l'admin — traite la demande. */
    /**
     * Clôture d'une visite : la rencontre a bien eu lieu (RG22).
     *
     * <p>Le statut TERMINE existait au modèle sans qu'aucune action ne le pose : les
     * fonds étaient donc libérés sur simple appréciation, sans trace de la visite.
     * Cette méthode ferme la boucle.</p>
     *
     * <p>Les deux parties peuvent déclarer la visite effectuée, mais la conséquence
     * diffère selon qui parle. Quand c'est le <em>locataire</em>, la confirmation
     * vient de la partie que le séquestre protège : il n'y a plus de litige possible,
     * les fonds sont libérés immédiatement. Quand c'est l'<em>auteur de l'annonce</em>,
     * il s'agit d'une déclaration intéressée : le rendez-vous est clôturé, mais la
     * libération reste soumise à l'arbitrage d'un administrateur, qui en est averti.</p>
     */
    public RendezVous terminer(Long id) {
        RendezVous rv = chargerPourAuteurOuDemandeur(id);
        exigerStatut(rv, EnumSet.of(StatutRendezVous.ACCEPTE));
        rv.setStatut(StatutRendezVous.TERMINE);
        rendezVousRepository.save(rv);

        User courant = utilisateurCourant();
        boolean parLeLocataire = rv.getDemandeur() != null && rv.getDemandeur().getId().equals(courant.getId());
        String titreBien = rv.getAnnonce() != null ? rv.getAnnonce().getTitre() : "le bien";

        if (parLeLocataire) {
            // La partie protégée confirme : plus de raison de retenir les fonds.
            paiementService.libererSiEnSequestre(rv.getId());
            notifier(rv.getAnnonce().getAuteur(), TypeNotification.RDV_TERMINE, "Visite confirmée",
                "Le locataire a confirmé la visite de « " + titreBien + " ». Les frais vous sont acquis.", rv);
        } else {
            notifier(rv.getDemandeur(), TypeNotification.RDV_TERMINE, "Visite déclarée effectuée",
                "La visite de « " + titreBien + "  » a été déclarée effectuée. Signalez toute erreur à l'administration.", rv);
            alerterAdministrateurs(
                "Visite à arbitrer",
                "La visite de « " + titreBien + " » est déclarée effectuée par l'annonceur. " +
                "Les frais restent en séquestre jusqu'à votre décision."
            );
        }
        return rv;
    }

    /** Le titulaire de l'annonce, le locataire concerné, ou un administrateur. */
    private RendezVous chargerPourAuteurOuDemandeur(Long id) {
        RendezVous rv = charger(id);
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return rv;
        }
        User courant = utilisateurCourant();
        boolean estAuteur = rv.getAnnonce() != null &&
            rv.getAnnonce().getAuteur() != null &&
            rv.getAnnonce().getAuteur().getId().equals(courant.getId());
        boolean estDemandeur = rv.getDemandeur() != null &&
            rv.getDemandeur().getId().equals(courant.getId());
        if (!estAuteur && !estDemandeur) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce rendez-vous ne vous concerne pas");
        }
        return rv;
    }

    /** Prévient tous les administrateurs actifs qu'un arbitrage est attendu. */
    private void alerterAdministrateurs(String titre, String message) {
        userRepository
            .findActifsParAutorite(AuthoritiesConstants.ADMIN)
            .forEach(admin -> notificationService.notifier(admin, TypeNotification.RDV_TERMINE, titre, message, "/admin/paiements"));
    }

    private RendezVous chargerPourAuteur(Long id) {
        RendezVous rv = charger(id);
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return rv;
        }
        User courant = utilisateurCourant();
        if (rv.getAnnonce().getAuteur() == null || !rv.getAnnonce().getAuteur().getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette annonce ne vous appartient pas");
        }
        return rv;
    }

    private void exigerStatut(RendezVous rv, Set<StatutRendezVous> autorises) {
        if (!autorises.contains(rv.getStatut())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transition impossible depuis le statut " + rv.getStatut());
        }
    }

    private void notifier(User destinataire, TypeNotification type, String titre, String message, RendezVous rv) {
        if (destinataire != null) {
            notificationService.notifier(destinataire, type, titre, message, "/rendez-vous/" + rv.getId());
        }
    }

    private User utilisateurCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non identifié"));
    }
}
