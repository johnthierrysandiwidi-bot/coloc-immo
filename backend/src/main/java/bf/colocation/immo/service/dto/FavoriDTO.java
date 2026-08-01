package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Favori} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FavoriDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant dateAjout;

    @NotNull
    private AnnonceDTO annonce;

    @NotNull
    private UserDTO utilisateur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Instant dateAjout) {
        this.dateAjout = dateAjout;
    }

    public AnnonceDTO getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceDTO annonce) {
        this.annonce = annonce;
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
        if (!(o instanceof FavoriDTO)) {
            return false;
        }

        FavoriDTO favoriDTO = (FavoriDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, favoriDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FavoriDTO{" +
            "id=" + getId() +
            ", dateAjout='" + getDateAjout() + "'" +
            ", annonce=" + getAnnonce() +
            ", utilisateur=" + getUtilisateur() +
            "}";
    }
}
