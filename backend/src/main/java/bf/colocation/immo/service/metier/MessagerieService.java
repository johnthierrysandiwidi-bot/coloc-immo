package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Conversation;
import bf.colocation.immo.domain.Message;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.ConversationRepository;
import bf.colocation.immo.repository.MessageRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Messagerie interne : conversations et messages entre utilisateurs.
 *
 * <p>Toute la logique d'accès est concentrée ici. Deux principes : le cloisonnement
 * (un utilisateur n'atteint que les conversations dont il est participant, vérifié à
 * chaque opération) et l'unicité (on retrouve le fil existant pour une annonce et une
 * paire, ou on le crée).</p>
 */
@Service
@Transactional
public class MessagerieService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final NotificationMetierService notificationService;

    public MessagerieService(
        ConversationRepository conversationRepository,
        MessageRepository messageRepository,
        AnnonceRepository annonceRepository,
        UserRepository userRepository,
        NotificationMetierService notificationService
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User utilisateurCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non identifié"));
    }

    @Transactional(readOnly = true)
    public List<Conversation> mesConversations() {
        return conversationRepository.findForUser(utilisateurCourant().getId());
    }

    /**
     * Ouvre la conversation à propos d'une annonce, en la créant au besoin.
     * L'interlocuteur est l'auteur de l'annonce ; on refuse qu'un utilisateur se contacte lui-même.
     */
    public Conversation ouvrirPourAnnonce(Long annonceId) {
        User moi = utilisateurCourant();
        Annonce annonce = annonceRepository
            .findById(annonceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Annonce introuvable"));
        User auteur = annonce.getAuteur();
        if (auteur == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette annonce n'a pas d'auteur à contacter");
        }
        if (auteur.getId().equals(moi.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas contacter votre propre annonce");
        }
        return conversationRepository
            .findExistante(annonceId, moi.getId(), auteur.getId())
            .orElseGet(() -> {
                Conversation c = new Conversation();
                c.setAnnonce(annonce);
                c.setParticipant1(moi);
                c.setParticipant2(auteur);
                c.setDateCreation(Instant.now());
                return conversationRepository.save(c);
            });
    }

    @Transactional
    public List<Message> messages(Long conversationId) {
        Conversation c = chargerConversationParticipant(conversationId);
        messageRepository.marquerLus(conversationId, utilisateurCourant().getId());
        return messageRepository.findByConversationIdOrderByDateEnvoiAsc(c.getId());
    }

    public Message envoyer(Long conversationId, String contenu) {
        if (contenu == null || contenu.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le message est vide");
        }
        User moi = utilisateurCourant();
        Conversation c = chargerConversationParticipant(conversationId);

        Message m = new Message();
        m.setConversation(c);
        m.setExpediteur(moi);
        m.setContenu(contenu.trim());
        m.setDateEnvoi(Instant.now());
        m.setLu(false);
        Message saved = messageRepository.save(m);

        c.setDernierMessageLe(saved.getDateEnvoi());
        conversationRepository.save(c);

        User destinataire = c.autre(moi.getId());
        if (destinataire != null) {
            String apercu = contenu.length() > 60 ? contenu.substring(0, 57) + "..." : contenu;
            notificationService.notifier(
                destinataire,
                TypeNotification.NOUVEAU_MESSAGE,
                "Nouveau message de " + moi.getLogin(),
                apercu,
                "/messages/" + c.getId()
            );
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public long nombreNonLus() {
        return messageRepository.compterNonLus(utilisateurCourant().getId());
    }

    private Conversation chargerConversationParticipant(Long conversationId) {
        Conversation c = conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));
        if (!c.concerne(utilisateurCourant().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette conversation ne vous concerne pas");
        }
        return c;
    }
}
