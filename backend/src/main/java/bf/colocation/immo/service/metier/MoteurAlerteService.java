package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.*;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.*;
import bf.colocation.immo.service.metier.PublicationAnnonceService.AnnoncePublieeEvent;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Moteur d'alertes (EF-08).
 *
 * À chaque annonce publiée, on confronte l'annonce aux alertes actives.
 * Anti-doublon : AlerteNotifiee garantit qu'une alerte ne notifie qu'une fois par annonce.
 */
@Service
public class MoteurAlerteService {

    private static final Logger LOG = LoggerFactory.getLogger(MoteurAlerteService.class);

    private final AlerteRepository alerteRepository;
    private final AnnonceRepository annonceRepository;
    private final AlerteNotifieeRepository alerteNotifieeRepository;
    private final NotificationMetierService notificationService;

    public MoteurAlerteService(
        AlerteRepository alerteRepository,
        AnnonceRepository annonceRepository,
        AlerteNotifieeRepository alerteNotifieeRepository,
        NotificationMetierService notificationService
    ) {
        this.alerteRepository = alerteRepository;
        this.annonceRepository = annonceRepository;
        this.alerteNotifieeRepository = alerteNotifieeRepository;
        this.notificationService = notificationService;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void surAnnoncePubliee(AnnoncePublieeEvent event) {
        annonceRepository.findById(event.annonceId()).ifPresent(this::evaluer);
    }

    @Transactional
    public void evaluer(Annonce annonce) {
        List<Alerte> alertes = alerteRepository.findAll().stream().filter(a -> Boolean.TRUE.equals(a.getActive())).toList();

        for (Alerte alerte : alertes) {
            if (!correspond(alerte, annonce)) continue;
            if (dejaNotifiee(alerte, annonce)) continue;

            notificationService.notifier(
                alerte.getTitulaire(),
                TypeNotification.NOUVELLE_ANNONCE,
                "Nouvelle annonce : " + alerte.getTitre(),
                "Une annonce correspond à votre alerte : " + annonce.getTitre(),
                "/annonces/" + annonce.getId()
            );

            AlerteNotifiee trace = new AlerteNotifiee();
            trace.setAlerte(alerte);
            trace.setAnnonce(annonce);
            trace.setDateEnvoi(Instant.now());
            alerteNotifieeRepository.save(trace);
        }
    }

    /** Chaque critère renseigné doit être satisfait ; un critère nul ne filtre pas. */
    boolean correspond(Alerte alerte, Annonce annonce) {
        Immobilier bien = annonce.getImmobilier();
        if (bien == null) return false;

        if (alerte.getTypeAnnonce() != null && alerte.getTypeAnnonce() != annonce.getType()) return false;
        if (alerte.getPrixMin() != null && (annonce.getPrix() == null || annonce.getPrix() < alerte.getPrixMin())) return false;
        if (alerte.getPrixMax() != null && (annonce.getPrix() == null || annonce.getPrix() > alerte.getPrixMax())) return false;

        if (alerte.getLocalite() != null && !idEgal(alerte.getLocalite().getId(), bien.getLocalite() == null ? null : bien.getLocalite().getId())) return false;
        if (alerte.getQuartier() != null && !idEgal(alerte.getQuartier().getId(), bien.getQuartier() == null ? null : bien.getQuartier().getId())) return false;
        if (alerte.getTypeImmobilier() != null && !idEgal(alerte.getTypeImmobilier().getId(), bien.getTypeImmobilier() == null ? null : bien.getTypeImmobilier().getId())) return false;

        if (alerte.getSurfaceMin() != null && (bien.getSurface() == null || bien.getSurface() < alerte.getSurfaceMin())) return false;
        if (alerte.getNombreChambresMin() != null && (bien.getNombreChambres() == null || bien.getNombreChambres() < alerte.getNombreChambresMin())) return false;
        if (Boolean.TRUE.equals(alerte.getMeubleUniquement()) && !Boolean.TRUE.equals(bien.getMeuble())) return false;

        return true;
    }

    private boolean dejaNotifiee(Alerte alerte, Annonce annonce) {
        return alerteNotifieeRepository
            .findAll()
            .stream()
            .anyMatch(an ->
                an.getAlerte() != null && an.getAnnonce() != null &&
                idEgal(an.getAlerte().getId(), alerte.getId()) &&
                idEgal(an.getAnnonce().getId(), annonce.getId())
            );
    }

    private boolean idEgal(Long a, Long b) {
        return Objects.equals(a, b);
    }
}
