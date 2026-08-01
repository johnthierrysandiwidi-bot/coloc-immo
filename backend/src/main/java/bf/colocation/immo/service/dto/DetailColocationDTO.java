package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.SexeRecherche;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link bf.colocation.immo.domain.DetailColocation} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DetailColocationDTO implements Serializable {

    private Long id;

    @NotNull
    @Min(value = 1)
    private Integer nombrePlaces;

    @NotNull
    @Min(value = 0)
    private Integer placesRestantes;

    @NotNull
    private SexeRecherche sexeRecherche;

    @Min(value = 16)
    @Max(value = 120)
    private Integer ageMin;

    @Min(value = 16)
    @Max(value = 120)
    private Integer ageMax;

    @NotNull
    @DecimalMin(value = "0")
    private Double loyer;

    @DecimalMin(value = "0")
    private Double caution;

    @DecimalMin(value = "0")
    private Double charges;

    @Size(max = 3000)
    private String reglesDeVie;

    @NotNull
    private AnnonceDTO annonce;

    private Set<EquipementDTO> equipementses = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(Integer nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public Integer getPlacesRestantes() {
        return placesRestantes;
    }

    public void setPlacesRestantes(Integer placesRestantes) {
        this.placesRestantes = placesRestantes;
    }

    public SexeRecherche getSexeRecherche() {
        return sexeRecherche;
    }

    public void setSexeRecherche(SexeRecherche sexeRecherche) {
        this.sexeRecherche = sexeRecherche;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public Double getLoyer() {
        return loyer;
    }

    public void setLoyer(Double loyer) {
        this.loyer = loyer;
    }

    public Double getCaution() {
        return caution;
    }

    public void setCaution(Double caution) {
        this.caution = caution;
    }

    public Double getCharges() {
        return charges;
    }

    public void setCharges(Double charges) {
        this.charges = charges;
    }

    public String getReglesDeVie() {
        return reglesDeVie;
    }

    public void setReglesDeVie(String reglesDeVie) {
        this.reglesDeVie = reglesDeVie;
    }

    public AnnonceDTO getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceDTO annonce) {
        this.annonce = annonce;
    }

    public Set<EquipementDTO> getEquipementses() {
        return equipementses;
    }

    public void setEquipementses(Set<EquipementDTO> equipementses) {
        this.equipementses = equipementses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DetailColocationDTO)) {
            return false;
        }

        DetailColocationDTO detailColocationDTO = (DetailColocationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, detailColocationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DetailColocationDTO{" +
            "id=" + getId() +
            ", nombrePlaces=" + getNombrePlaces() +
            ", placesRestantes=" + getPlacesRestantes() +
            ", sexeRecherche='" + getSexeRecherche() + "'" +
            ", ageMin=" + getAgeMin() +
            ", ageMax=" + getAgeMax() +
            ", loyer=" + getLoyer() +
            ", caution=" + getCaution() +
            ", charges=" + getCharges() +
            ", reglesDeVie='" + getReglesDeVie() + "'" +
            ", annonce=" + getAnnonce() +
            ", equipementses=" + getEquipementses() +
            "}";
    }
}
