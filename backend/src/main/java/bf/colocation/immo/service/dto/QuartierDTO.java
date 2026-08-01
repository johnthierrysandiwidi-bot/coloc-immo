package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Quartier} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class QuartierDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String nom;

    @Size(max = 500)
    private String description;

    @NotNull
    private LocaliteDTO localite;

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

    public LocaliteDTO getLocalite() {
        return localite;
    }

    public void setLocalite(LocaliteDTO localite) {
        this.localite = localite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuartierDTO)) {
            return false;
        }

        QuartierDTO quartierDTO = (QuartierDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, quartierDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "QuartierDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            ", localite=" + getLocalite() +
            "}";
    }
}
