package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.TypeProprietaire;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.ProfilProprietaire} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProfilProprietaireDTO implements Serializable {

    private Long id;

    @NotNull
    private TypeProprietaire type;

    @Size(max = 150)
    private String raisonSociale;

    private Instant dateCreation;

    @NotNull
    private UserDTO utilisateur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeProprietaire getType() {
        return type;
    }

    public void setType(TypeProprietaire type) {
        this.type = type;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProfilProprietaireDTO)) {
            return false;
        }

        ProfilProprietaireDTO profilProprietaireDTO = (ProfilProprietaireDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, profilProprietaireDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProfilProprietaireDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", raisonSociale='" + getRaisonSociale() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            ", utilisateur=" + getUtilisateur() +
            "}";
    }
}
