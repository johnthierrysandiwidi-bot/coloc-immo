package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.DeviceToken} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeviceTokenDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 255)
    private String token;

    @NotNull
    @Size(max = 20)
    private String plateforme;

    private Instant dateCreation;

    @NotNull
    private UserDTO utilisateur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlateforme() {
        return plateforme;
    }

    public void setPlateforme(String plateforme) {
        this.plateforme = plateforme;
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
        if (!(o instanceof DeviceTokenDTO)) {
            return false;
        }

        DeviceTokenDTO deviceTokenDTO = (DeviceTokenDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, deviceTokenDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeviceTokenDTO{" +
            "id=" + getId() +
            ", token='" + getToken() + "'" +
            ", plateforme='" + getPlateforme() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            ", utilisateur=" + getUtilisateur() +
            "}";
    }
}
