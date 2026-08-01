package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Paiement;
import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.MoyenPaiement;
import bf.colocation.immo.domain.enumeration.StatutPaiement;
import bf.colocation.immo.service.security.AutorisationService;
import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.PaiementRepository;
import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Séquestre des frais de visite — module V2, passerelle SIMULÉE.
 *
 * Aucun encaissement réel : la méthode {@link #simulerReglement} joue le rôle
 * qu'aurait le retour d'une passerelle mobile money. Toute la logique de séquestre
 * (RG21 à RG24), en revanche, est réelle et testable.
 *
 * Machine à états :
 *   EN_ATTENTE --régler--> EN_SEQUESTRE --visite honorée--> LIBERE
 *                                        \--visite annulée--> REMBOURSE
 */
@Service
@Transactional
public class PaiementService {

    /** RG23 — Montant plafonné et affiché à l'avance. */
    public static final double FRAIS_DE_VISITE = 2000d;

    private final PaiementRepository paiementRepository;
    private final RendezVousRepository rendezVousRepository;
    private final UserRepository userRepository;
    private final NotificationMetierService notificationService;
    private final AutorisationService autorisationService;

    public PaiementService(
        PaiementRepository paiementRepository,
        RendezVousRepository rendezVousRepository,
        UserRepository userRepository,
        NotificationMetierService notificationService,
        AutorisationService autorisationService
    ) {
        this.paiementRepository = paiementRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.autorisationService = autorisationService;
    }

    /**
     * Crée (ou retrouve) le paiement associé à un rendez-vous.
     * RG21 — le paiement conditionne la confirmation du rendez-vous.
     */
    public Paiement initier(Long rendezVousId) {
        RendezVous rdv = rendezVousRepository
            .findById(rendezVousId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rendez-vous introuvable"));

        User courant = utilisateurCourant();
        // Seul le demandeur du rendez-vous règle les frais.
        if (rdv.getDemandeur() == null || !rdv.getDemandeur().getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce rendez-vous n'est pas le vôtre");
        }

        return paiementRepository
            .findByRendezVousId(rendezVousId)
            .orElseGet(() -> {
                Paiement p = new Paiement();
                p.setReference(genererReference());
                p.setMontant(FRAIS_DE_VISITE);
                p.setStatut(StatutPaiement.EN_ATTENTE);
                p.setDateCreation(Instant.now());
                p.setRendezVous(rdv);
                p.setPayeur(courant);
                return paiementRepository.save(p);
            });
    }

    /**
     * Simule le retour de la passerelle : le paiement passe en séquestre et le
     * rendez-vous, jusque-là « demandé », est confirmé (« accepté »).
     * Dans un système réel, cette méthode serait le webhook vérifié de la passerelle.
     */
    public Paiement simulerReglement(Long paiementId, MoyenPaiement moyen) {
        Paiement p = charger(paiementId);
        assurerPayeur(p);

        if (p.getStatut() != StatutPaiement.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce paiement a déjà été traité");
        }

        p.setMoyen(moyen != null ? moyen : MoyenPaiement.ORANGE_MONEY);
        p.setStatut(StatutPaiement.EN_SEQUESTRE); // RG22 — fonds conservés jusqu'à la visite
        p.setDateSequestre(Instant.now());

        RendezVous rdv = p.getRendezVous();
        if (rdv != null && rdv.getStatut() == StatutRendezVous.DEMANDE) {
            rdv.setStatut(StatutRendezVous.ACCEPTE);
            rendezVousRepository.save(rdv);
            if (rdv.getDemandeur() != null) {
                notificationService.notifier(
                    rdv.getDemandeur(),
                    TypeNotification.RDV_ACCEPTE,
                    "Frais de visite réglés",
                    "Votre paiement est en séquestre et votre visite est confirmée.",
                    "/rendez-vous"
                );
            }
        }
        return paiementRepository.save(p);
    }

    /** Visite honorée : RG22 — les fonds sont libérés au démarcheur. */
    public Paiement libererApresVisite(Long paiementId) {
        Paiement p = charger(paiementId);
        exigerSequestre(p);
        p.setStatut(StatutPaiement.LIBERE);
        p.setDateDenouement(Instant.now());
        return paiementRepository.save(p);
    }

    /**
     * Libère les fonds d'un rendez-vous, s'il en existe et s'ils sont en séquestre.
     *
     * <p>Appelée à la clôture d'une visite confirmée par le locataire. Contrairement à
     * {@link #libererApresVisite(Long)}, elle ne lève pas d'exception quand il n'y a rien
     * à libérer : une visite peut très bien avoir eu lieu sans frais, ou avoir déjà été
     * dénouée. Le silence est ici le comportement correct.</p>
     *
     * @return le paiement libéré, ou {@code null} s'il n'y avait rien à faire.
     */
    public Paiement libererSiEnSequestre(Long rendezVousId) {
        Paiement p = paiementRepository.findByRendezVousId(rendezVousId).orElse(null);
        if (p == null || p.getStatut() != StatutPaiement.EN_SEQUESTRE) {
            return null;
        }
        p.setStatut(StatutPaiement.LIBERE);
        p.setDateDenouement(Instant.now());
        return paiementRepository.save(p);
    }

    /** Visite annulée par le démarcheur : RG22 — remboursement intégral du payeur. */
    public Paiement rembourser(Long paiementId) {
        Paiement p = charger(paiementId);
        exigerSequestre(p);
        p.setStatut(StatutPaiement.REMBOURSE);
        p.setDateDenouement(Instant.now());
        if (p.getPayeur() != null) {
            notificationService.notifier(
                p.getPayeur(),
                TypeNotification.PAIEMENT_REMBOURSE,
                "Remboursement effectué",
                "La visite n'a pas eu lieu : vos frais de visite vous sont remboursés.",
                "/rendez-vous"
            );
        }
        return paiementRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<Paiement> tous() {
        return paiementRepository.findAllDetaille();
    }

    /**
     * État du règlement d'un rendez-vous.
     *
     * <p>Deux personnes ont un besoin légitime de le connaître : le visiteur qui paie, et
     * l'auteur de l'annonce (démarcheur ou propriétaire) qui doit savoir si les frais sont
     * en séquestre avant d'honorer la visite. Sans ce contrôle, n'importe quel compte
     * connecté pouvait énumérer les identifiants de rendez-vous et lire les paiements
     * d'autrui — montant, référence et identité du payeur.</p>
     */
    @Transactional(readOnly = true)
    public Paiement pourRendezVous(Long rendezVousId) {
        Paiement p = paiementRepository.findByRendezVousId(rendezVousId).orElse(null);
        if (p == null) {
            return null;
        }
        Long payeurId = p.getPayeur() != null ? p.getPayeur().getId() : null;
        Long auteurId = null;
        RendezVous rdv = p.getRendezVous();
        if (rdv != null && rdv.getAnnonce() != null && rdv.getAnnonce().getAuteur() != null) {
            auteurId = rdv.getAnnonce().getAuteur().getId();
        }
        autorisationService.exigerUnDesProprietairesOuAdmin(payeurId, auteurId);
        return p;
    }

    // ---- utilitaires ----

    private void exigerSequestre(Paiement p) {
        if (p.getStatut() != StatutPaiement.EN_SEQUESTRE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un paiement en séquestre peut être dénoué");
        }
    }

    private Paiement charger(Long id) {
        return paiementRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paiement introuvable"));
    }

    private void assurerPayeur(Paiement p) {
        User courant = utilisateurCourant();
        if (p.getPayeur() == null || !p.getPayeur().getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce paiement n'est pas le vôtre");
        }
    }

    private String genererReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    private User utilisateurCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));
    }
}
