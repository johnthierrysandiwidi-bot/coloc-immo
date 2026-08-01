package bf.colocation.immo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Favori.
 */
@Entity
@Table(name = "favori")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Favori implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "date_ajout", nullable = false)
    private Instant dateAjout;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "immobilier", "auteur", "detailColocation", "vueses", "rendezVouses", "favorises" },
        allowSetters = true
    )
    private Annonce annonce;

    @ManyToOne(optional = false)
    @NotNull
    private User utilisateur;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Favori id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateAjout() {
        return this.dateAjout;
    }

    public Favori dateAjout(Instant dateAjout) {
        this.setDateAjout(dateAjout);
        return this;
    }

    public void setDateAjout(Instant dateAjout) {
        this.dateAjout = dateAjout;
    }

    public Annonce getAnnonce() {
        return this.annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public Favori annonce(Annonce annonce) {
        this.setAnnonce(annonce);
        return this;
    }

    public User getUtilisateur() {
        return this.utilisateur;
    }

    public void setUtilisateur(User user) {
        this.utilisateur = user;
    }

    public Favori utilisateur(User user) {
        this.setUtilisateur(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Favori)) {
            return false;
        }
        return getId() != null && getId().equals(((Favori) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Favori{" +
            "id=" + getId() +
            ", dateAjout='" + getDateAjout() + "'" +
            "}";
    }
}
