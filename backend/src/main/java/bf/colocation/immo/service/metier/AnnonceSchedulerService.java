package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.AnnonceRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF-04.6 : expiration quotidienne des annonces.
 * EF-05.7 : clôture d'une colocation dont les places restantes tombent à zéro.
 */
@Service
@Transactional
public class AnnonceSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnonceSchedulerService.class);

    private final AnnonceRepository annonceRepository;
    private final NotificationMetierService notificationService;

    public AnnonceSchedulerService(AnnonceRepository annonceRepository, NotificationMetierService notificationService) {
        this.annonceRepository = annonceRepository;
        this.notificationService = notificationService;
    }

    /** Tous les jours à 2h du matin. */
    @Scheduled(cron = "0 0 2 * * *")
    public void expirerAnnonces() {
        Instant maintenant = Instant.now();

        List<Annonce> expirees = annonceRepository
            .findAll()
            .stream()
            .filter(a -> a.getStatut() == StatutAnnonce.PUBLIEE)
            .filter(a -> a.getDateExpiration() != null && a.getDateExpiration().isBefore(maintenant))
            .toList();

        for (Annonce a : expirees) {
            a.setStatut(StatutAnnonce.EXPIREE);
            annonceRepository.save(a);
            if (a.getAuteur() != null) {
                notificationService.notifier(
                    a.getAuteur(),
                    TypeNotification.ANNONCE_EXPIREE,
                    "Annonce expirée",
                    "Votre annonce « " + a.getTitre() + " » a expiré. Republiez-la pour la remettre en ligne.",
                    "/mes-annonces/" + a.getId()
                );
            }
        }
        LOG.info("Expiration quotidienne : {} annonce(s) expirée(s)", expirees.size());
    }

    /** EF-05.7 : plus de place = colocation close. Appelé après toute réservation. */
    public void cloturerSiComplet(Annonce annonce) {
        if (annonce.getType() != TypeAnnonce.COLOCATION || annonce.getDetailColocation() == null) {
            return;
        }
        Integer restantes = annonce.getDetailColocation().getPlacesRestantes();
        if (restantes != null && restantes <= 0 && annonce.getStatut() == StatutAnnonce.PUBLIEE) {
            annonce.setStatut(StatutAnnonce.CLOTUREE);
            annonceRepository.save(annonce);
            LOG.debug("Colocation {} clôturée : plus de place disponible", annonce.getId());
        }
    }
}
