package bf.colocation.immo.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.AlerteNotifiee} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlerteNotifieeDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant dateEnvoi;

    @NotNull
    private AlerteDTO alerte;

    @NotNull
    private AnnonceDTO annonce;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(Instant dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public AlerteDTO getAlerte() {
        return alerte;
    }

    public void setAlerte(AlerteDTO alerte) {
        this.alerte = alerte;
    }

    public AnnonceDTO getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceDTO annonce) {
        this.annonce = annonce;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlerteNotifieeDTO)) {
            return false;
        }

        AlerteNotifieeDTO alerteNotifieeDTO = (AlerteNotifieeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, alerteNotifieeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlerteNotifieeDTO{" +
            "id=" + getId() +
            ", dateEnvoi='" + getDateEnvoi() + "'" +
            ", alerte=" + getAlerte() +
            ", annonce=" + getAnnonce() +
            "}";
    }
}
