package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Notification;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.NotificationRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Point d'entrée unique pour créer une notification (EF-09).
 * Le push FCM se branche ici (envoyerPush), en un seul endroit.
 */
@Service
@Transactional
public class NotificationMetierService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationMetierService.class);

    private final NotificationRepository notificationRepository;

    public NotificationMetierService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification notifier(User destinataire, TypeNotification type, String titre, String message, String lien) {
        Notification n = new Notification();
        n.setDestinataire(destinataire);
        n.setType(type);
        n.setTitre(titre);
        n.setMessage(message);
        n.setLien(lien);
        n.setLue(false);
        n.setDateCreation(Instant.now());
        Notification saved = notificationRepository.save(n);
        envoyerPush(saved);
        return saved;
    }

    /** TODO : brancher Firebase Cloud Messaging via DeviceToken. */
    private void envoyerPush(Notification n) {
        LOG.debug("Push à envoyer à {} : {}", n.getDestinataire().getLogin(), n.getTitre());
    }
}
