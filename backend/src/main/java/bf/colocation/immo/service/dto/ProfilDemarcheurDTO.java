package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.StatutValidation;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.ProfilDemarcheur} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProfilDemarcheurDTO implements Serializable {

    private Long id;

    @NotNull
    private StatutValidation statutValidation;

    private Instant dateValidation;

    private Instant dateCreation;

    @NotNull
    private UserDTO utilisateur;

    private UserDTO validePar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatutValidation getStatutValidation() {
        return statutValidation;
    }

    public void setStatutValidation(StatutValidation statutValidation) {
        this.statutValidation = statutValidation;
    }

    public Instant getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(Instant dateValidation) {
        this.dateValidation = dateValidation;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public UserDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UserDTO utilisateur) {
        this.utilisateur = utilisateur;
    }

    public UserDTO getValidePar() {
        return validePar;
    }

    public void setValidePar(UserDTO validePar) {
        this.validePar = validePar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfilDemarcheurDTO)) {
            return false;
        }

        ProfilDemarcheurDTO profilDemarcheurDTO = (ProfilDemarcheurDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, profilDemarcheurDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProfilDemarcheurDTO{" +
            "id=" + getId() +
            ", statutValidation='" + getStatutValidation() + "'" +
            ", dateValidation='" + getDateValidation() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            ", utilisateur=" + getUtilisateur() +
            ", validePar=" + getValidePar() +
            "}";
    }
}
