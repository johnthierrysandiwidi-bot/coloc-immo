package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.Periodicite;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Prix} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class PrixDTO implements Serializable {

    private Long id;

    @NotNull
    @DecimalMin(value = "0")
    private Double prix;

    @Size(max = 500)
    private String description;

    @DecimalMin(value = "0")
    private Double charges;

    @NotNull
    private Periodicite periodicite;

    @NotNull
    private LocalDate dateEffet;

    @NotNull
    private ImmobilierDTO immobilier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getCharges() {
        return charges;
    }

    public void setCharges(Double charges) {
        this.charges = charges;
    }

    public Periodicite getPeriodicite() {
        return periodicite;
    }

    public void setPeriodicite(Periodicite periodicite) {
        this.periodicite = periodicite;
    }

    public LocalDate getDateEffet() {
        return dateEffet;
    }

    public void setDateEffet(LocalDate dateEffet) {
        this.dateEffet = dateEffet;
    }

    public ImmobilierDTO getImmobilier() {
        return immobilier;
    }

    public void setImmobilier(ImmobilierDTO immobilier) {
        this.immobilier = immobilier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrixDTO)) {
            return false;
        }

        PrixDTO prixDTO = (PrixDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, prixDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PrixDTO{" +
            "id=" + getId() +
            ", prix=" + getPrix() +
            ", description='" + getDescription() + "'" +
            ", charges=" + getCharges() +
            ", periodicite='" + getPeriodicite() + "'" +
            ", dateEffet='" + getDateEffet() + "'" +
            ", immobilier=" + getImmobilier() +
            "}";
    }
}
