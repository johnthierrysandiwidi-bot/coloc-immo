package bf.colocation.immo.service.security;

import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.security.SecurityUtils;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Service central d'autorisation « au niveau des données » (data-level authorization).
 *
 * <p>La configuration de sécurité HTTP ne fait qu'exiger un jeton valide sur {@code /api/**}
 * ({@code .authenticated()}). Elle ne dit RIEN sur l'appartenance des objets : sans le présent
 * service, tout utilisateur authentifié pouvait lire, modifier ou supprimer les rendez-vous,
 * documents, favoris, alertes, notifications ou paiements d'autrui, simplement en changeant
 * l'identifiant dans l'URL (faille de type IDOR — Insecure Direct Object Reference).</p>
 *
 * <p>Ce service fournit le socle réutilisable pour :</p>
 * <ul>
 *   <li>récupérer l'identifiant de l'utilisateur courant depuis le jeton (jamais depuis un
 *       paramètre fourni par le client) ;</li>
 *   <li>exiger qu'une ressource appartienne à l'utilisateur courant, avec dérogation pour
 *       l'administrateur ({@link #exigerProprietaireOuAdmin}).</li>
 * </ul>
 */
@Service
public class AutorisationService {

    private static final Logger LOG = LoggerFactory.getLogger(AutorisationService.class);

    /**
     * Identifiant de l'utilisateur authentifié, tel qu'inscrit dans le jeton.
     *
     * <p>L'appel échoue en <em>refus</em> (HTTP 403) et non en erreur serveur. La
     * distinction compte : l'identifiant est absent dès que le principal n'est pas
     * un jeton porteur du claim — jeton émis par une version antérieure, session
     * reconstruite autrement, ou contexte de test. Une exception d'authentification
     * y produisait une 500, présentée à l'utilisateur comme une panne alors qu'il
     * lui suffisait de se reconnecter.</p>
     *
     * @throws AccessDeniedException si aucun identifiant n'est disponible.
     */
    public Long idUtilisateurCourant() {
        return SecurityUtils.getCurrentUserId().orElseThrow(() -> {
            LOG.warn("Aucun identifiant utilisateur dans le contexte de sécurité : accès refusé.");
            return new AccessDeniedException("Session incomplète. Reconnectez-vous.");
        });
    }

    /**
     * Identifiant à utiliser pour restreindre une liste.
     *
     * <p>Quand l'identifiant est inconnu, on renvoie une valeur qui ne correspond à
     * aucun enregistrement plutôt que de lever une exception. Le principe est de
     * « fermer » en cas de doute : la liste revient vide, ce qui est sans danger,
     * là où lever une erreur donnerait une panne visible et où ne pas filtrer du
     * tout exposerait les données de tous les utilisateurs.</p>
     */
    public Long idPourFiltrage() {
        return SecurityUtils.getCurrentUserId().orElseGet(() -> {
            LOG.warn("Identifiant utilisateur indisponible : la liste est restreinte à un ensemble vide.");
            return AUCUN_UTILISATEUR;
        });
    }

    /** Identifiant volontairement impossible, aucun enregistrement ne le porte. */
    private static final Long AUCUN_UTILISATEUR = -1L;

    /** @return l'identifiant courant s'il existe, sinon {@link Optional#empty()} (utile pour les endpoints publics). */
    public Optional<Long> idUtilisateurCourantOptionnel() {
        return SecurityUtils.getCurrentUserId();
    }

    /** @return vrai si l'utilisateur courant possède le rôle administrateur. */
    public boolean estAdmin() {
        return SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
    }

    /** @return vrai si l'{@code idProprietaire} correspond à l'utilisateur courant. */
    public boolean estProprietaire(Long idProprietaire) {
        return idProprietaire != null && Objects.equals(idProprietaire, SecurityUtils.getCurrentUserId().orElse(null));
    }

    /**
     * Exige que la ressource appartienne à l'utilisateur courant, l'administrateur étant toujours autorisé.
     *
     * @param idProprietaire identifiant du propriétaire légitime de la ressource (ex. demandeur d'un rendez-vous,
     *                       démarcheur d'un document, titulaire d'une alerte…).
     * @throws AccessDeniedException (traduite en HTTP 403) si l'utilisateur courant n'est ni le propriétaire ni admin.
     */
    public void exigerProprietaireOuAdmin(Long idProprietaire) {
        if (estAdmin()) {
            return;
        }
        Long courant = SecurityUtils.getCurrentUserId().orElse(null);
        if (courant == null || !Objects.equals(courant, idProprietaire)) {
            LOG.warn("Accès refusé : l'utilisateur {} a tenté d'accéder à une ressource appartenant à {}.", courant, idProprietaire);
            throw new AccessDeniedException("Vous n'êtes pas autorisé à accéder à cette ressource.");
        }
    }

    /**
     * Variante pour les ressources à double titulaire légitime — typiquement un rendez-vous, visible à la fois
     * par le locataire qui l'a demandé et par l'auteur de l'annonce (démarcheur/propriétaire).
     *
     * @throws AccessDeniedException si l'utilisateur courant ne figure dans aucun des identifiants autorisés.
     */
    public void exigerUnDesProprietairesOuAdmin(Long... idsAutorises) {
        if (estAdmin()) {
            return;
        }
        Long courant = SecurityUtils.getCurrentUserId().orElse(null);
        if (courant != null) {
            for (Long id : idsAutorises) {
                if (Objects.equals(courant, id)) {
                    return;
                }
            }
        }
        LOG.warn("Accès refusé : l'utilisateur {} n'est titulaire d'aucun des accès légitimes à la ressource.", courant);
        throw new AccessDeniedException("Vous n'êtes pas autorisé à accéder à cette ressource.");
    }
}
