package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.Periodicite;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Prix.
 */
@Entity
@Table(name = "prix")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Prix implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "prix", nullable = false)
    private Double prix;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @DecimalMin(value = "0")
    @Column(name = "charges")
    private Double charges;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite", nullable = false)
    private Periodicite periodicite;

    @NotNull
    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "prixes", "imageses", "proprietaire", "demarcheur", "localite", "quartier", "typeImmobilier", "annonceses" },
        allowSetters = true
    )
    private Immobilier immobilier;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Prix id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrix() {
        return this.prix;
    }

    public Prix prix(Double prix) {
        this.setPrix(prix);
        return this;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return this.description;
    }

    public Prix description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCharges() {
        return this.charges;
    }

    public Prix charges(Double charges) {
        this.setCharges(charges);
        return this;
    }

    public void setCharges(Double charges) {
        this.charges = charges;
    }

    public Periodicite getPeriodicite() {
        return this.periodicite;
    }

    public Prix periodicite(Periodicite periodicite) {
        this.setPeriodicite(periodicite);
        return this;
    }

    public void setPeriodicite(Periodicite periodicite) {
        this.periodicite = periodicite;
    }

    public LocalDate getDateEffet() {
        return this.dateEffet;
    }

    public Prix dateEffet(LocalDate dateEffet) {
        this.setDateEffet(dateEffet);
        return this;
    }

    public void setDateEffet(LocalDate dateEffet) {
        this.dateEffet = dateEffet;
    }

    public Immobilier getImmobilier() {
        return this.immobilier;
    }

    public void setImmobilier(Immobilier immobilier) {
        this.immobilier = immobilier;
    }

    public Prix immobilier(Immobilier immobilier) {
        this.setImmobilier(immobilier);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Prix)) {
            return false;
        }
        return getId() != null && getId().equals(((Prix) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Prix{" +
            "id=" + getId() +
            ", prix=" + getPrix() +
            ", description='" + getDescription() + "'" +
            ", charges=" + getCharges() +
            ", periodicite='" + getPeriodicite() + "'" +
            ", dateEffet='" + getDateEffet() + "'" +
            "}";
    }
}
