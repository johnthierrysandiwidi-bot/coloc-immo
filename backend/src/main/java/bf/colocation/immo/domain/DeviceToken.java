package bf.colocation.immo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A DeviceToken.
 */
@Entity
@Table(name = "device_token")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeviceToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "token", length = 255, nullable = false)
    private String token;

    @NotNull
    @Size(max = 20)
    @Column(name = "plateforme", length = 20, nullable = false)
    private String plateforme;

    @Column(name = "date_creation")
    private Instant dateCreation;

    @ManyToOne(optional = false)
    @NotNull
    private User utilisateur;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DeviceToken id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return this.token;
    }

    public DeviceToken token(String token) {
        this.setToken(token);
        return this;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlateforme() {
        return this.plateforme;
    }

    public DeviceToken plateforme(String plateforme) {
        this.setPlateforme(plateforme);
        return this;
    }

    public void setPlateforme(String plateforme) {
        this.plateforme = plateforme;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public DeviceToken dateCreation(Instant dateCreation) {
        this.setDateCreation(dateCreation);
        return this;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public User getUtilisateur() {
        return this.utilisateur;
    }

    public void setUtilisateur(User user) {
        this.utilisateur = user;
    }

    public DeviceToken utilisateur(User user) {
        this.setUtilisateur(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceToken)) {
            return false;
        }
        return getId() != null && getId().equals(((DeviceToken) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeviceToken{" +
            "id=" + getId() +
            ", token='" + getToken() + "'" +
            ", plateforme='" + getPlateforme() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            "}";
    }
}
