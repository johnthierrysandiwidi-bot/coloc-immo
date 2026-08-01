package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Document;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutValidation;
import bf.colocation.immo.repository.DocumentRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cycle de vie des pièces justificatives du démarcheur (EF-02).
 *
 * Règle centrale : un document déjà VALIDE est figé. Sans cela, un démarcheur
 * pourrait faire valider une pièce authentique puis la remplacer par une autre.
 */
@Service
@Transactional
public class DocumentMetierService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentMetierService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Document> mesDocuments() {
        return documentRepository.findByDemarcheurIsCurrentUser();
    }

    /** Remplace le fichier d'un document refusé (ou encore en attente). */
    public Document remplacer(Long documentId, String nouvelleUrl, String nouveauNom) {
        Document document = charger(documentId);
        assurerProprietaire(document);

        if (document.getStatut() == StatutValidation.VALIDE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un document déjà validé ne peut pas être remplacé");
        }

        document.setUrl(nouvelleUrl);
        if (nouveauNom != null && !nouveauNom.isBlank()) {
            document.setNom(nouveauNom);
        }
        // Le remplacement remet le compteur à zéro : l'admin doit réexaminer.
        document.setStatut(StatutValidation.EN_ATTENTE);
        document.setMotifRefus(null);
        document.setTraitePar(null);
        document.setDateTraitement(null);
        document.setDateAjout(Instant.now());
        return documentRepository.save(document);
    }

    /** Suppression possible tant que l'administrateur n'a pas validé la pièce. */
    public void supprimer(Long documentId) {
        Document document = charger(documentId);
        assurerProprietaire(document);

        if (document.getStatut() == StatutValidation.VALIDE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un document validé ne peut plus être supprimé");
        }
        documentRepository.delete(document);
    }

    /** Renvoie l'URL du fichier, après contrôle que le demandeur a le droit de le lire. */
    @Transactional(readOnly = true)
    public String urlDeTelechargement(Long documentId) {
        Document document = charger(documentId);
        boolean estAdmin = SecurityUtils.hasCurrentUserThisAuthority(bf.colocation.immo.security.AuthoritiesConstants.ADMIN);
        if (!estAdmin) {
            assurerProprietaire(document);
        }
        return document.getUrl();
    }

    private Document charger(Long documentId) {
        return documentRepository
            .findById(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    private void assurerProprietaire(Document document) {
        User courant = utilisateurCourant();
        if (document.getDemarcheur() == null || !document.getDemarcheur().getId().equals(courant.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce document ne vous appartient pas");
        }
    }

    private User utilisateurCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));
    }
}
