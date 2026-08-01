package bf.colocation.immo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Jeton de rafraîchissement. Absent de JHipster par défaut, exigé par le cahier des charges (ENF-01).
 */
@Entity
@Table(name = "refresh_token")
public class RefreshToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "token", length = 255, nullable = false, unique = true)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "date_expiration", nullable = false)
    private Instant dateExpiration;

    @Column(name = "revoque", nullable = false)
    private boolean revoque = false;

    @NotNull
    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation = Instant.now();

    public boolean estValide() {
        return !revoque && dateExpiration.isAfter(Instant.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Instant getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Instant dateExpiration) { this.dateExpiration = dateExpiration; }
    public boolean isRevoque() { return revoque; }
    public void setRevoque(boolean revoque) { this.revoque = revoque; }
    public Instant getDateCreation() { return dateCreation; }
    public void setDateCreation(Instant dateCreation) { this.dateCreation = dateCreation; }
}
