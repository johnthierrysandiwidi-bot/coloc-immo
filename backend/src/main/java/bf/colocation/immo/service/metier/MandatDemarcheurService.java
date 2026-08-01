package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.domain.ProfilDemarcheur;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutValidation;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.ImmobilierRepository;
import bf.colocation.immo.repository.ProfilDemarcheurRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import bf.colocation.immo.security.SecurityUtils;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Mandat de commercialisation : le propriétaire confie son bien à un démarcheur.
 *
 * <p>Le modèle prévoyait ce lien depuis le début — un bien porte un champ
 * {@code demarcheur} — mais aucune opération ne permettait de l'établir : le mandat
 * restait théorique. Ce service le rend effectif, en encadrant qui peut désigner qui.</p>
 *
 * <p>Deux garde-fous. D'abord, seul le propriétaire du bien (ou un administrateur)
 * peut mandater : un démarcheur ne se désigne pas lui-même. Ensuite, seul un
 * démarcheur dont l'identité a été <em>vérifiée</em> peut être mandaté — c'est la
 * promesse centrale de la plateforme, elle ne doit pas être contournée par ce biais.</p>
 */
@Service
@Transactional
public class MandatDemarcheurService {

    private final ImmobilierRepository immobilierRepository;
    private final ProfilDemarcheurRepository profilDemarcheurRepository;
    private final UserRepository userRepository;
    private final NotificationMetierService notificationService;

    public MandatDemarcheurService(
        ImmobilierRepository immobilierRepository,
        ProfilDemarcheurRepository profilDemarcheurRepository,
        UserRepository userRepository,
        NotificationMetierService notificationService
    ) {
        this.immobilierRepository = immobilierRepository;
        this.profilDemarcheurRepository = profilDemarcheurRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /** Les démarcheurs vérifiés, seuls candidats possibles à un mandat. */
    @Transactional(readOnly = true)
    public List<User> demarcheursDisponibles() {
        return profilDemarcheurRepository.findDemarcheursValides().stream().map(ProfilDemarcheur::getUtilisateur).toList();
    }

    /** Confie le bien à un démarcheur vérifié. */
    public Immobilier mandater(Long bienId, Long demarcheurId) {
        Immobilier bien = chargerBienDuProprietaire(bienId);
        User demarcheur = userRepository
            .findById(demarcheurId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Démarcheur introuvable"));

        // Un démarcheur non vérifié ne peut pas être mandaté : c'est le socle de confiance.
        ProfilDemarcheur profil = profilDemarcheurRepository
            .findByUtilisateurId(demarcheurId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet utilisateur n'est pas démarcheur"));
        if (profil.getStatutValidation() != StatutValidation.VALIDE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce démarcheur n'est pas encore vérifié");
        }

        bien.setDemarcheur(demarcheur);
        Immobilier saved = immobilierRepository.save(bien);

        notificationService.notifier(
            demarcheur,
            TypeNotification.MANDAT_CONFIE,
            "Nouveau mandat",
            "Un propriétaire vous a confié la commercialisation de « " + bien.getNom() + " ».",
            "/mes-biens"
        );
        return saved;
    }

    /** Retire le mandat en cours. */
    public Immobilier retirerMandat(Long bienId) {
        Immobilier bien = chargerBienDuProprietaire(bienId);
        User ancien = bien.getDemarcheur();
        bien.setDemarcheur(null);
        Immobilier saved = immobilierRepository.save(bien);

        if (ancien != null) {
            notificationService.notifier(
                ancien,
                TypeNotification.MANDAT_RETIRE,
                "Mandat retiré",
                "Le mandat sur « " + bien.getNom() + " » vous a été retiré.",
                "/mes-biens"
            );
        }
        return saved;
    }

    /**
     * Charge le bien en exigeant que l'appelant en soit le propriétaire.
     *
     * <p>Volontairement plus strict que l'écriture ordinaire du bien : mandater engage
     * la responsabilité du propriétaire, un démarcheur déjà mandaté ne doit pas pouvoir
     * s'en substituer un autre.</p>
     */
    private Immobilier chargerBienDuProprietaire(Long bienId) {
        Immobilier bien = immobilierRepository
            .findById(bienId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bien introuvable"));
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return bien;
        }
        Long courant = SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .map(User::getId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non identifié"));
        if (bien.getProprietaire() == null || !bien.getProprietaire().getId().equals(courant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le propriétaire du bien peut gérer son mandat");
        }
        return bien;
    }
}
