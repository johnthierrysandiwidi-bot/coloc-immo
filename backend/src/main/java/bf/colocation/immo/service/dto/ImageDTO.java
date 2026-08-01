package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Image} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ImageDTO implements Serializable {

    private Long id;

    @Size(max = 150)
    private String nom;

    @NotNull
    @Size(max = 255)
    private String url;

    @Min(value = 0)
    private Integer ordre;

    private Boolean principale;

    @NotNull
    private ImmobilierDTO immobilier;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public Boolean getPrincipale() {
        return principale;
    }

    public void setPrincipale(Boolean principale) {
        this.principale = principale;
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
        if (!(o instanceof ImageDTO)) {
            return false;
        }

        ImageDTO imageDTO = (ImageDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, imageDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ImageDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", url='" + getUrl() + "'" +
            ", ordre=" + getOrdre() +
            ", principale='" + getPrincipale() + "'" +
            ", immobilier=" + getImmobilier() +
            "}";
    }
}
