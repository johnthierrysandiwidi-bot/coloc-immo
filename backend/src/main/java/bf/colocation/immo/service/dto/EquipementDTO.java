package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Equipement} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EquipementDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 80)
    private String nom;

    @Size(max = 80)
    private String icone;

    private Set<DetailColocationDTO> colocationses = new HashSet<>();

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

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public Set<DetailColocationDTO> getColocationses() {
        return colocationses;
    }

    public void setColocationses(Set<DetailColocationDTO> colocationses) {
        this.colocationses = colocationses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EquipementDTO)) {
            return false;
        }

        EquipementDTO equipementDTO = (EquipementDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, equipementDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EquipementDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", icone='" + getIcone() + "'" +
            ", colocationses=" + getColocationses() +
            "}";
    }
}
