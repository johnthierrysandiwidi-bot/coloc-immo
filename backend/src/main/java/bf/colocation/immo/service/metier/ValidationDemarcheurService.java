package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Document;
import bf.colocation.immo.domain.ProfilDemarcheur;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutValidation;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.DocumentRepository;
import bf.colocation.immo.repository.ProfilDemarcheurRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Workflow de vérification documentaire (EF-02).
 *
 * Règles :
 *  - seul l'administrateur décide (garanti par @PreAuthorize sur le Resource) ;
 *  - un refus exige un motif ;
 *  - dès qu'un document est validé, le démarcheur passe à VALIDE et gagne le droit de publier ;
 *  - toute décision notifie le démarcheur.
 */
@Service
@Transactional
public class ValidationDemarcheurService {

    private final DocumentRepository documentRepository;
    private final ProfilDemarcheurRepository profilDemarcheurRepository;
    private final UserRepository userRepository;
    private final NotificationMetierService notificationService;

    public ValidationDemarcheurService(
        DocumentRepository documentRepository,
        ProfilDemarcheurRepository profilDemarcheurRepository,
        UserRepository userRepository,
        NotificationMetierService notificationService
    ) {
        this.documentRepository = documentRepository;
        this.profilDemarcheurRepository = profilDemarcheurRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Document valider(Long documentId) {
        Document doc = charger(documentId);
        User admin = adminCourant();

        doc.setStatut(StatutValidation.VALIDE);
        doc.setMotifRefus(null);
        doc.setTraitePar(admin);
        doc.setDateTraitement(Instant.now());
        documentRepository.save(doc);

        // Un document validé suffit à débloquer le démarcheur (EF-02.5)
        profilDemarcheurRepository
            .findByUtilisateurId(doc.getDemarcheur().getId())
            .ifPresent(profil -> {
                profil.setStatutValidation(StatutValidation.VALIDE);
                profil.setDateValidation(Instant.now());
                profil.setValidePar(admin);
                profilDemarcheurRepository.save(profil);
            });

        notificationService.notifier(
            doc.getDemarcheur(),
            TypeNotification.DOCUMENT_VALIDE,
            "Document validé",
            "Votre document « " + doc.getNom() + " » a été validé. Vous pouvez désormais publier des annonces.",
            "/mes-documents"
        );
        return doc;
    }

    public Document refuser(Long documentId, String motif) {
        if (motif == null || motif.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un refus doit être motivé (EF-02.3)");
        }
        Document doc = charger(documentId);

        doc.setStatut(StatutValidation.REFUSE);
        doc.setMotifRefus(motif);
        doc.setTraitePar(adminCourant());
        doc.setDateTraitement(Instant.now());
        documentRepository.save(doc);

        notificationService.notifier(
            doc.getDemarcheur(),
            TypeNotification.DOCUMENT_REFUSE,
            "Document refusé",
            "Votre document « " + doc.getNom() + " » a été refusé. Motif : " + motif,
            "/mes-documents"
        );
        return doc;
    }

    /** Le verrou du projet : un démarcheur non validé ne publie pas (EF-02.1). */
    @Transactional(readOnly = true)
    public boolean peutPublier(User utilisateur) {
        return profilDemarcheurRepository
            .findByUtilisateurId(utilisateur.getId())
            .map(ProfilDemarcheur::getStatutValidation)
            .map(StatutValidation.VALIDE::equals)
            .orElse(false);
    }

    private Document charger(Long id) {
        return documentRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    private User adminCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrateur non identifié"));
    }
}
