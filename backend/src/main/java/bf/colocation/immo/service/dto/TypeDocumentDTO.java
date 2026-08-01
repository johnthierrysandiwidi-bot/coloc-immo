package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.TypeDocument} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TypeDocumentDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String nom;

    @Size(max = 500)
    private String description;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypeDocumentDTO)) {
            return false;
        }

        TypeDocumentDTO typeDocumentDTO = (TypeDocumentDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, typeDocumentDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TypeDocumentDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
