package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.VueAnnonce} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VueAnnonceDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant dateVue;

    @Size(max = 45)
    private String adresseIp;

    @NotNull
    private AnnonceDTO annonce;

    private UserDTO utilisateur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateVue() {
        return dateVue;
    }

    public void setDateVue(Instant dateVue) {
        this.dateVue = dateVue;
    }

    public String getAdresseIp() {
        return adresseIp;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
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
        if (!(o instanceof VueAnnonceDTO)) {
            return false;
        }

        VueAnnonceDTO vueAnnonceDTO = (VueAnnonceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, vueAnnonceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VueAnnonceDTO{" +
            "id=" + getId() +
            ", dateVue='" + getDateVue() + "'" +
            ", adresseIp='" + getAdresseIp() + "'" +
            ", annonce=" + getAnnonce() +
            ", utilisateur=" + getUtilisateur() +
            "}";
    }
}
