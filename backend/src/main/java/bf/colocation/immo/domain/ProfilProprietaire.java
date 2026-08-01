package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.TypeProprietaire;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ProfilProprietaire.
 */
@Entity
@Table(name = "profil_proprietaire")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProfilProprietaire implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeProprietaire type;

    @Size(max = 150)
    @Column(name = "raison_sociale", length = 150)
    private String raisonSociale;

    @Column(name = "date_creation")
    private Instant dateCreation;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private User utilisateur;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ProfilProprietaire id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeProprietaire getType() {
        return this.type;
    }

    public ProfilProprietaire type(TypeProprietaire type) {
        this.setType(type);
        return this;
    }

    public void setType(TypeProprietaire type) {
        this.type = type;
    }

    public String getRaisonSociale() {
        return this.raisonSociale;
    }

    public ProfilProprietaire raisonSociale(String raisonSociale) {
        this.setRaisonSociale(raisonSociale);
        return this;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public ProfilProprietaire dateCreation(Instant dateCreation) {
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

    public ProfilProprietaire utilisateur(User user) {
        this.setUtilisateur(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfilProprietaire)) {
            return false;
        }
        return getId() != null && getId().equals(((ProfilProprietaire) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProfilProprietaire{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", raisonSociale='" + getRaisonSociale() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            "}";
    }
}
