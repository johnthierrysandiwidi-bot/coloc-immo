package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.StatutBien;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Immobilier} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ImmobilierDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 150)
    private String nom;

    @Size(max = 5000)
    private String description;

    @Size(max = 255)
    private String adresse;

    @DecimalMin(value = "0")
    private Double surface;

    @Min(value = 0)
    private Integer nombrePieces;

    @Min(value = 0)
    private Integer nombreChambres;

    @Min(value = 0)
    private Integer nombreSallesBain;

    @Min(value = 0)
    private Integer nombreSalons;

    private Boolean garage;

    private Boolean piscine;

    private Boolean meuble;

    private LocalDate disponibleA;

    @NotNull
    private StatutBien statut;

    private Double latitude;

    private Double longitude;

    private Instant dateCreation;

    @NotNull
    private UserDTO proprietaire;

    private UserDTO demarcheur;

    private LocaliteDTO localite;

    private QuartierDTO quartier;

    private TypeImmobilierDTO typeImmobilier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Double getSurface() {
        return surface;
    }

    public void setSurface(Double surface) {
        this.surface = surface;
    }

    public Integer getNombrePieces() {
        return nombrePieces;
    }

    public void setNombrePieces(Integer nombrePieces) {
        this.nombrePieces = nombrePieces;
    }

    public Integer getNombreChambres() {
        return nombreChambres;
    }

    public void setNombreChambres(Integer nombreChambres) {
        this.nombreChambres = nombreChambres;
    }

    public Integer getNombreSallesBain() {
        return nombreSallesBain;
    }

    public void setNombreSallesBain(Integer nombreSallesBain) {
        this.nombreSallesBain = nombreSallesBain;
    }

    public Integer getNombreSalons() {
        return nombreSalons;
    }

    public void setNombreSalons(Integer nombreSalons) {
        this.nombreSalons = nombreSalons;
    }

    public Boolean getGarage() {
        return garage;
    }

    public void setGarage(Boolean garage) {
        this.garage = garage;
    }

    public Boolean getPiscine() {
        return piscine;
    }

    public void setPiscine(Boolean piscine) {
        this.piscine = piscine;
    }

    public Boolean getMeuble() {
        return meuble;
    }

    public void setMeuble(Boolean meuble) {
        this.meuble = meuble;
    }

    public LocalDate getDisponibleA() {
        return disponibleA;
    }

    public void setDisponibleA(LocalDate disponibleA) {
        this.disponibleA = disponibleA;
    }

    public StatutBien getStatut() {
        return statut;
    }

    public void setStatut(StatutBien statut) {
        this.statut = statut;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public UserDTO getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(UserDTO proprietaire) {
        this.proprietaire = proprietaire;
    }

    public UserDTO getDemarcheur() {
        return demarcheur;
    }

    public void setDemarcheur(UserDTO demarcheur) {
        this.demarcheur = demarcheur;
    }

    public LocaliteDTO getLocalite() {
        return localite;
    }

    public void setLocalite(LocaliteDTO localite) {
        this.localite = localite;
    }

    public QuartierDTO getQuartier() {
        return quartier;
    }

    public void setQuartier(QuartierDTO quartier) {
        this.quartier = quartier;
    }

    public TypeImmobilierDTO getTypeImmobilier() {
        return typeImmobilier;
    }

    public void setTypeImmobilier(TypeImmobilierDTO typeImmobilier) {
        this.typeImmobilier = typeImmobilier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmobilierDTO)) {
            return false;
        }

        ImmobilierDTO immobilierDTO = (ImmobilierDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, immobilierDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ImmobilierDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            ", adresse='" + getAdresse() + "'" +
            ", surface=" + getSurface() +
            ", nombrePieces=" + getNombrePieces() +
            ", nombreChambres=" + getNombreChambres() +
            ", nombreSallesBain=" + getNombreSallesBain() +
            ", nombreSalons=" + getNombreSalons() +
            ", garage='" + getGarage() + "'" +
            ", piscine='" + getPiscine() + "'" +
            ", meuble='" + getMeuble() + "'" +
            ", disponibleA='" + getDisponibleA() + "'" +
            ", statut='" + getStatut() + "'" +
            ", latitude=" + getLatitude() +
            ", longitude=" + getLongitude() +
            ", dateCreation='" + getDateCreation() + "'" +
            ", proprietaire=" + getProprietaire() +
            ", demarcheur=" + getDemarcheur() +
            ", localite=" + getLocalite() +
            ", quartier=" + getQuartier() +
            ", typeImmobilier=" + getTypeImmobilier() +
            "}";
    }
}
