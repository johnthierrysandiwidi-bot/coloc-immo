package bf.colocation.immo.web.rest;

import static bf.colocation.immo.security.SecurityUtils.AUTHORITIES_CLAIM;
import static bf.colocation.immo.security.SecurityUtils.JWT_ALGORITHM;
import static bf.colocation.immo.security.SecurityUtils.USER_ID_CLAIM;

import bf.colocation.immo.domain.RefreshToken;
import bf.colocation.immo.security.DomainUserDetailsService.UserWithId;
import bf.colocation.immo.service.security.RefreshTokenService;
import bf.colocation.immo.web.rest.vm.LoginVM;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

/**
 * Authentification : login, rafraîchissement du jeton, déconnexion.
 */
@RestController
@RequestMapping("/api")
public class AuthenticateController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateController.class);

    private final JwtEncoder jwtEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:0}")
    private long tokenValidityInSeconds;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me:0}")
    private long tokenValidityInSecondsForRememberMe;

    public AuthenticateController(
        JwtEncoder jwtEncoder,
        AuthenticationManagerBuilder authenticationManagerBuilder,
        RefreshTokenService refreshTokenService,
        UserDetailsService userDetailsService
    ) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    /** {@code POST /api/authenticate} : connexion. Renvoie l'access token ET le refresh token. */
    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());
        var authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = createToken(authentication, loginVM.isRememberMe());
        RefreshToken refreshToken = refreshTokenService.creerPour(authentication.getName());

        var httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(jwt);
        return new ResponseEntity<>(new JWTToken(jwt, refreshToken.getToken()), httpHeaders, HttpStatus.OK);
    }

    /**
     * {@code POST /api/auth/refresh} : échange un refresh token contre un nouvel access token.
     * Rotation systématique : l'ancien refresh token est révoqué.
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<JWTToken> refresh(@Valid @RequestBody RefreshVM refreshVM) {
        RefreshToken nouveau = refreshTokenService.rafraichir(refreshVM.getRefreshToken());
        String login = nouveau.getUser().getLogin();

        UserDetails userDetails = userDetailsService.loadUserByUsername(login);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String jwt = createToken(authentication, false);
        var httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(jwt);
        return new ResponseEntity<>(new JWTToken(jwt, nouveau.getToken()), httpHeaders, HttpStatus.OK);
    }

    /** {@code POST /api/auth/logout} : révoque le refresh token courant. */
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshVM refreshVM) {
        refreshTokenService.revoquer(refreshVM.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }

    public String createToken(Authentication authentication, boolean rememberMe) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        var now = Instant.now();
        Instant validity = rememberMe
            ? now.plus(this.tokenValidityInSecondsForRememberMe, ChronoUnit.SECONDS)
            : now.plus(this.tokenValidityInSeconds, ChronoUnit.SECONDS);

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(validity)
            .subject(authentication.getName())
            .claim(AUTHORITIES_CLAIM, authorities);

        if (authentication.getPrincipal() instanceof UserWithId user) {
            builder.claim(USER_ID_CLAIM, user.getId());
        }

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, builder.build())).getTokenValue();
    }

    /** Corps de réponse de l'authentification. */
    static class JWTToken {

        private final String idToken;
        private final String refreshToken;

        JWTToken(String idToken, String refreshToken) {
            this.idToken = idToken;
            this.refreshToken = refreshToken;
        }

        @JsonProperty("id_token")
        public String getIdToken() {
            return idToken;
        }

        @JsonProperty("refresh_token")
        public String getRefreshToken() {
            return refreshToken;
        }
    }

    /** Corps de requête pour /auth/refresh et /auth/logout. */
    public static class RefreshVM {

        @NotBlank
        private String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
