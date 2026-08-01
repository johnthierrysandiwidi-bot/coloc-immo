package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Avis;
import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.AvisRepository;
import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.SecurityUtils;
import bf.colocation.immo.service.dto.AvisDTO;
import bf.colocation.immo.service.dto.ReputationDTO;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Avis et réputation des démarcheurs.
 *
 * <p>La réputation se construit sur des visites réelles. Déposer un avis suppose donc
 * un rendez-vous à l'état TERMINE, dont l'appelant est le locataire : on ne note pas
 * sans avoir rencontré, et on ne note pas à la place d'autrui. Un rendez-vous ne peut
 * donner qu'un seul avis, ce qui empêche de gonfler artificiellement une note.</p>
 *
 * <p>Le démarcheur noté est l'auteur de l'annonce visitée — celui qui a effectivement
 * mené la visite, cohérent avec le reste du workflow de rendez-vous.</p>
 */
@Service
@Transactional
public class AvisService {

    private final AvisRepository avisRepository;
    private final RendezVousRepository rendezVousRepository;
    private final UserRepository userRepository;
    private final NotificationMetierService notificationService;

    public AvisService(
        AvisRepository avisRepository,
        RendezVousRepository rendezVousRepository,
        UserRepository userRepository,
        NotificationMetierService notificationService
    ) {
        this.avisRepository = avisRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /** Dépose un avis à l'issue d'une visite effectuée. */
    public AvisDTO deposer(Long rendezVousId, Integer note, String commentaire) {
        if (note == null || note < 1 || note > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La note doit être comprise entre 1 et 5");
        }

        RendezVous rv = rendezVousRepository
            .findById(rendezVousId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rendez-vous introuvable"));

        User courant = utilisateurCourant();

        // 1. Seul le locataire qui a visité peut noter.
        if (rv.getDemandeur() == null || !rv.getDemandeur().getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le visiteur peut laisser un avis");
        }

        // 2. La visite doit avoir été effectuée.
        if (rv.getStatut() != StatutRendezVous.TERMINE) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Un avis n'est possible qu'après une visite effectuée"
            );
        }

        // 3. Un seul avis par rendez-vous.
        if (avisRepository.findByRendezVousId(rendezVousId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez déjà noté cette visite");
        }

        // Le démarcheur noté est l'auteur de l'annonce visitée.
        Annonce annonce = rv.getAnnonce();
        User demarcheur = annonce != null ? annonce.getAuteur() : null;
        if (demarcheur == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun intermédiaire à noter pour cette visite");
        }
        if (demarcheur.getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "On ne peut pas se noter soi-même");
        }

        Avis avis = new Avis()
            .note(note)
            .commentaire(commentaire)
            .dateCreation(Instant.now())
            .auteur(courant)
            .demarcheur(demarcheur)
            .rendezVous(rv);
        Avis saved = avisRepository.save(avis);

        notificationService.notifier(
            demarcheur,
            TypeNotification.AVIS_RECU,
            "Nouvel avis",
            "Vous avez reçu un avis " + note + "/5 après une visite.",
            "/mon-profil"
        );
        return toDto(saved);
    }

    /** Réputation publique d'un démarcheur : moyenne, nombre d'avis et détail. */
    @Transactional(readOnly = true)
    public ReputationDTO reputation(Long demarcheurId) {
        List<AvisDTO> liste = avisRepository.findByDemarcheur(demarcheurId).stream().map(this::toDto).toList();
        Double moyenne = avisRepository.moyennePour(demarcheurId);

        ReputationDTO dto = new ReputationDTO();
        dto.setDemarcheurId(demarcheurId);
        dto.setMoyenne(moyenne == null ? null : Math.round(moyenne * 10) / 10.0);
        dto.setNombreAvis(avisRepository.countByDemarcheurId(demarcheurId));
        dto.setAvis(liste);
        return dto;
    }

    /** Rendez-vous terminés du locataire courant qu'il n'a pas encore notés. */
    @Transactional(readOnly = true)
    public List<Long> rendezVousNotables() {
        User courant = utilisateurCourant();
        return rendezVousRepository
            .findAll()
            .stream()
            .filter(rv -> rv.getStatut() == StatutRendezVous.TERMINE)
            .filter(rv -> rv.getDemandeur() != null && rv.getDemandeur().getId().equals(courant.getId()))
            .filter(rv -> avisRepository.findByRendezVousId(rv.getId()).isEmpty())
            .map(RendezVous::getId)
            .toList();
    }

    private AvisDTO toDto(Avis a) {
        AvisDTO dto = new AvisDTO();
        dto.setId(a.getId());
        dto.setNote(a.getNote());
        dto.setCommentaire(a.getCommentaire());
        dto.setDateCreation(a.getDateCreation());
        dto.setAuteurLogin(a.getAuteur() != null ? a.getAuteur().getLogin() : null);
        dto.setDemarcheurId(a.getDemarcheur() != null ? a.getDemarcheur().getId() : null);
        dto.setRendezVousId(a.getRendezVous() != null ? a.getRendezVous().getId() : null);
        return dto;
    }

    private User utilisateurCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non identifié"));
    }
}
