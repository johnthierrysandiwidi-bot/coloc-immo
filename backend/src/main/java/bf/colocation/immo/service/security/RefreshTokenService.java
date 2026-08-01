package bf.colocation.immo.service.security;

import bf.colocation.immo.domain.RefreshToken;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.repository.RefreshTokenRepository;
import bf.colocation.immo.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Émission, rotation et révocation des refresh tokens.
 * Rotation systématique : un refresh token ne sert qu'une fois.
 */
@Service
@Transactional
public class RefreshTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${application.security.refresh-token-validity-in-days:7}")
    private long validiteEnJours;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken creerPour(String login) {
        User user = userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));

        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);

        RefreshToken rt = new RefreshToken();
        rt.setToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        rt.setUser(user);
        rt.setDateCreation(Instant.now());
        rt.setDateExpiration(Instant.now().plus(validiteEnJours, ChronoUnit.DAYS));
        return refreshTokenRepository.save(rt);
    }

    /**
     * Consomme un refresh token et en émet un nouveau (rotation).
     * Rejette tout jeton révoqué, expiré ou inconnu.
     */
    public RefreshToken rafraichir(String token) {
        RefreshToken ancien = refreshTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inconnu"));

        if (!ancien.estValide()) {
            // Réutilisation d'un jeton déjà consommé : on révoque toute la session par précaution.
            refreshTokenRepository.revoquerTousPourUtilisateur(ancien.getUser());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expiré ou révoqué");
        }

        ancien.setRevoque(true);
        refreshTokenRepository.save(ancien);
        return creerPour(ancien.getUser().getLogin());
    }

    public void revoquer(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoque(true);
            refreshTokenRepository.save(rt);
        });
    }

    /** Purge quotidienne des jetons expirés (3h du matin). */
    @Scheduled(cron = "0 0 3 * * *")
    public void purger() {
        LOG.debug("Purge des refresh tokens expirés");
        refreshTokenRepository.purgerExpires(Instant.now());
    }
}
