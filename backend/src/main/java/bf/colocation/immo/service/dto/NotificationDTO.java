package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.TypeNotification;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Notification} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NotificationDTO implements Serializable {

    private Long id;

    @NotNull
    private TypeNotification type;

    @NotNull
    @Size(max = 150)
    private String titre;

    @NotNull
    @Size(max = 500)
    private String message;

    @Size(max = 255)
    private String lien;

    @NotNull
    private Boolean lue;

    @NotNull
    private Instant dateCreation;

    @NotNull
    private UserDTO destinataire;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeNotification getType() {
        return type;
    }

    public void setType(TypeNotification type) {
        this.type = type;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public Boolean getLue() {
        return lue;
    }

    public void setLue(Boolean lue) {
        this.lue = lue;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public UserDTO getDestinataire() {
        return destinataire;
    }

    public void setDestinataire(UserDTO destinataire) {
        this.destinataire = destinataire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationDTO)) {
            return false;
        }

        NotificationDTO notificationDTO = (NotificationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, notificationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificationDTO{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", titre='" + getTitre() + "'" +
            ", message='" + getMessage() + "'" +
            ", lien='" + getLien() + "'" +
            ", lue='" + getLue() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            ", destinataire=" + getDestinataire() +
            "}";
    }
}
