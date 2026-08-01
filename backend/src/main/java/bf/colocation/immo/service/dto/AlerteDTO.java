package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.FrequenceAlerte;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Alerte} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlerteDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 150)
    private String titre;

    @Size(max = 2000)
    private String contenu;

    private TypeAnnonce typeAnnonce;

    @DecimalMin(value = "0")
    private Double prixMin;

    @DecimalMin(value = "0")
    private Double prixMax;

    @DecimalMin(value = "0")
    private Double surfaceMin;

    @Min(value = 0)
    private Integer nombreChambresMin;

    private Boolean meubleUniquement;

    @NotNull
    private Boolean active;

    @NotNull
    private FrequenceAlerte frequence;

    private Instant derniereExecution;

    @NotNull
    private UserDTO titulaire;

    private LocaliteDTO localite;

    private QuartierDTO quartier;

    private TypeImmobilierDTO typeImmobilier;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public TypeAnnonce getTypeAnnonce() {
        return typeAnnonce;
    }

    public void setTypeAnnonce(TypeAnnonce typeAnnonce) {
        this.typeAnnonce = typeAnnonce;
    }

    public Double getPrixMin() {
        return prixMin;
    }

    public void setPrixMin(Double prixMin) {
        this.prixMin = prixMin;
    }

    public Double getPrixMax() {
        return prixMax;
    }

    public void setPrixMax(Double prixMax) {
        this.prixMax = prixMax;
    }

    public Double getSurfaceMin() {
        return surfaceMin;
    }

    public void setSurfaceMin(Double surfaceMin) {
        this.surfaceMin = surfaceMin;
    }

    public Integer getNombreChambresMin() {
        return nombreChambresMin;
    }

    public void setNombreChambresMin(Integer nombreChambresMin) {
        this.nombreChambresMin = nombreChambresMin;
    }

    public Boolean getMeubleUniquement() {
        return meubleUniquement;
    }

    public void setMeubleUniquement(Boolean meubleUniquement) {
        this.meubleUniquement = meubleUniquement;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public FrequenceAlerte getFrequence() {
        return frequence;
    }

    public void setFrequence(FrequenceAlerte frequence) {
        this.frequence = frequence;
    }

    public Instant getDerniereExecution() {
        return derniereExecution;
    }

    public void setDerniereExecution(Instant derniereExecution) {
        this.derniereExecution = derniereExecution;
    }

    public UserDTO getTitulaire() {
        return titulaire;
    }

    public void setTitulaire(UserDTO titulaire) {
        this.titulaire = titulaire;
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
        if (!(o instanceof AlerteDTO)) {
            return false;
        }

        AlerteDTO alerteDTO = (AlerteDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alerteDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlerteDTO{" +
            "id=" + getId() +
            ", titre='" + getTitre() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", typeAnnonce='" + getTypeAnnonce() + "'" +
            ", prixMin=" + getPrixMin() +
            ", prixMax=" + getPrixMax() +
            ", surfaceMin=" + getSurfaceMin() +
            ", nombreChambresMin=" + getNombreChambresMin() +
            ", meubleUniquement='" + getMeubleUniquement() + "'" +
            ", active='" + getActive() + "'" +
            ", frequence='" + getFrequence() + "'" +
            ", derniereExecution='" + getDerniereExecution() + "'" +
            ", titulaire=" + getTitulaire() +
            ", localite=" + getLocalite() +
            ", quartier=" + getQuartier() +
            ", typeImmobilier=" + getTypeImmobilier() +
            "}";
    }
}
