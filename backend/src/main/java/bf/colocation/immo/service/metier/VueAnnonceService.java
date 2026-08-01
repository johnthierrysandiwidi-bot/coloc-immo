package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.VueAnnonce;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.repository.VueAnnonceRepository;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * EF-04.4 : une vue par utilisateur (ou par IP) et par tranche de 24 h.
 * Sans cette règle, rafraîchir la page gonflerait le compteur indéfiniment.
 */
@Service
@Transactional
public class VueAnnonceService {

    private final VueAnnonceRepository vueAnnonceRepository;
    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;

    public VueAnnonceService(
        VueAnnonceRepository vueAnnonceRepository,
        AnnonceRepository annonceRepository,
        UserRepository userRepository
    ) {
        this.vueAnnonceRepository = vueAnnonceRepository;
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
    }

    public void enregistrer(Long annonceId, String adresseIp) {
        Annonce annonce = annonceRepository.findById(annonceId).orElse(null);
        if (annonce == null) {
            return;
        }

        User courant = SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).orElse(null);
        Instant limite = Instant.now().minus(24, ChronoUnit.HOURS);

        boolean dejaVue = vueAnnonceRepository
            .findAll()
            .stream()
            .filter(v -> v.getAnnonce() != null && Objects.equals(v.getAnnonce().getId(), annonceId))
            .filter(v -> v.getDateVue() != null && v.getDateVue().isAfter(limite))
            .anyMatch(v ->
                courant != null
                    ? (v.getUtilisateur() != null && Objects.equals(v.getUtilisateur().getId(), courant.getId()))
                    : (v.getUtilisateur() == null && Objects.equals(v.getAdresseIp(), adresseIp))
            );

        if (dejaVue) {
            return;
        }

        VueAnnonce vue = new VueAnnonce();
        vue.setAnnonce(annonce);
        vue.setUtilisateur(courant);
        vue.setAdresseIp(adresseIp);
        vue.setDateVue(Instant.now());
        vueAnnonceRepository.save(vue);

        annonce.setNombreVues((annonce.getNombreVues() == null ? 0 : annonce.getNombreVues()) + 1);
        annonceRepository.save(annonce);
    }
}
