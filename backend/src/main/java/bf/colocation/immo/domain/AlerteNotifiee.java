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
 * A AlerteNotifiee.
 */
@Entity
@Table(name = "alerte_notifiee")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlerteNotifiee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "date_envoi", nullable = false)
    private Instant dateEnvoi;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "titulaire", "localite", "quartier", "typeImmobilier", "notifieeses" }, allowSetters = true)
    private Alerte alerte;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "immobilier", "auteur", "detailColocation", "vueses", "rendezVouses", "favorises" },
        allowSetters = true
    )
    private Annonce annonce;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AlerteNotifiee id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateEnvoi() {
        return this.dateEnvoi;
    }

    public AlerteNotifiee dateEnvoi(Instant dateEnvoi) {
        this.setDateEnvoi(dateEnvoi);
        return this;
    }

    public void setDateEnvoi(Instant dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public Alerte getAlerte() {
        return this.alerte;
    }

    public void setAlerte(Alerte alerte) {
        this.alerte = alerte;
    }

    public AlerteNotifiee alerte(Alerte alerte) {
        this.setAlerte(alerte);
        return this;
    }

    public Annonce getAnnonce() {
        return this.annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public AlerteNotifiee annonce(Annonce annonce) {
        this.setAnnonce(annonce);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlerteNotifiee)) {
            return false;
        }
        return getId() != null && getId().equals(((AlerteNotifiee) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlerteNotifiee{" +
            "id=" + getId() +
            ", dateEnvoi='" + getDateEnvoi() + "'" +
            "}";
    }
}
