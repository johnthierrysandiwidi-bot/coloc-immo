package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.RendezVous} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RendezVousDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant dateHeure;

    private Instant dateReportee;

    @Size(max = 255)
    private String lieu;

    @Size(max = 2000)
    private String contenu;

    @Size(max = 255)
    private String motif;

    @NotNull
    private StatutRendezVous statut;

    @NotNull
    private AnnonceDTO annonce;

    @NotNull
    private UserDTO demandeur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(Instant dateHeure) {
        this.dateHeure = dateHeure;
    }

    public Instant getDateReportee() {
        return dateReportee;
    }

    public void setDateReportee(Instant dateReportee) {
        this.dateReportee = dateReportee;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutRendezVous getStatut() {
        return statut;
    }

    public void setStatut(StatutRendezVous statut) {
        this.statut = statut;
    }

    public AnnonceDTO getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceDTO annonce) {
        this.annonce = annonce;
    }

    public UserDTO getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(UserDTO demandeur) {
        this.demandeur = demandeur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RendezVousDTO)) {
            return false;
        }

        RendezVousDTO rendezVousDTO = (RendezVousDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, rendezVousDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RendezVousDTO{" +
            "id=" + getId() +
            ", dateHeure='" + getDateHeure() + "'" +
            ", dateReportee='" + getDateReportee() + "'" +
            ", lieu='" + getLieu() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", motif='" + getMotif() + "'" +
            ", statut='" + getStatut() + "'" +
            ", annonce=" + getAnnonce() +
            ", demandeur=" + getDemandeur() +
            "}";
    }
}
