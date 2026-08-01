package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.StatutValidation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ProfilDemarcheur.
 */
@Entity
@Table(name = "profil_demarcheur")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProfilDemarcheur implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false)
    private StatutValidation statutValidation;

    @Column(name = "date_validation")
    private Instant dateValidation;

    @Column(name = "date_creation")
    private Instant dateCreation;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    private User validePar;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ProfilDemarcheur id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatutValidation getStatutValidation() {
        return this.statutValidation;
    }

    public ProfilDemarcheur statutValidation(StatutValidation statutValidation) {
        this.setStatutValidation(statutValidation);
        return this;
    }

    public void setStatutValidation(StatutValidation statutValidation) {
        this.statutValidation = statutValidation;
    }

    public Instant getDateValidation() {
        return this.dateValidation;
    }

    public ProfilDemarcheur dateValidation(Instant dateValidation) {
        this.setDateValidation(dateValidation);
        return this;
    }

    public void setDateValidation(Instant dateValidation) {
        this.dateValidation = dateValidation;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public ProfilDemarcheur dateCreation(Instant dateCreation) {
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

    public ProfilDemarcheur utilisateur(User user) {
        this.setUtilisateur(user);
        return this;
    }

    public User getValidePar() {
        return this.validePar;
    }

    public void setValidePar(User user) {
        this.validePar = user;
    }

    public ProfilDemarcheur validePar(User user) {
        this.setValidePar(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfilDemarcheur)) {
            return false;
        }
        return getId() != null && getId().equals(((ProfilDemarcheur) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProfilDemarcheur{" +
            "id=" + getId() +
            ", statutValidation='" + getStatutValidation() + "'" +
            ", dateValidation='" + getDateValidation() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            "}";
    }
}
