package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A RendezVous.
 */
@Entity
@Table(name = "rendez_vous")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RendezVous implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "date_heure", nullable = false)
    private Instant dateHeure;

    @Column(name = "date_reportee")
    private Instant dateReportee;

    @Size(max = 255)
    @Column(name = "lieu", length = 255)
    private String lieu;

    @Size(max = 2000)
    @Column(name = "contenu", length = 2000)
    private String contenu;

    @Size(max = 255)
    @Column(name = "motif", length = 255)
    private String motif;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutRendezVous statut;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "immobilier", "auteur", "detailColocation", "vueses", "rendezVouses", "favorises" },
        allowSetters = true
    )
    private Annonce annonce;

    @ManyToOne(optional = false)
    @NotNull
    private User demandeur;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RendezVous id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateHeure() {
        return this.dateHeure;
    }

    public RendezVous dateHeure(Instant dateHeure) {
        this.setDateHeure(dateHeure);
        return this;
    }

    public void setDateHeure(Instant dateHeure) {
        this.dateHeure = dateHeure;
    }

    public Instant getDateReportee() {
        return this.dateReportee;
    }

    public RendezVous dateReportee(Instant dateReportee) {
        this.setDateReportee(dateReportee);
        return this;
    }

    public void setDateReportee(Instant dateReportee) {
        this.dateReportee = dateReportee;
    }

    public String getLieu() {
        return this.lieu;
    }

    public RendezVous lieu(String lieu) {
        this.setLieu(lieu);
        return this;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getContenu() {
        return this.contenu;
    }

    public RendezVous contenu(String contenu) {
        this.setContenu(contenu);
        return this;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getMotif() {
        return this.motif;
    }

    public RendezVous motif(String motif) {
        this.setMotif(motif);
        return this;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutRendezVous getStatut() {
        return this.statut;
    }

    public RendezVous statut(StatutRendezVous statut) {
        this.setStatut(statut);
        return this;
    }

    public void setStatut(StatutRendezVous statut) {
        this.statut = statut;
    }

    public Annonce getAnnonce() {
        return this.annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public RendezVous annonce(Annonce annonce) {
        this.setAnnonce(annonce);
        return this;
    }

    public User getDemandeur() {
        return this.demandeur;
    }

    public void setDemandeur(User user) {
        this.demandeur = user;
    }

    public RendezVous demandeur(User user) {
        this.setDemandeur(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RendezVous)) {
            return false;
        }
        return getId() != null && getId().equals(((RendezVous) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RendezVous{" +
            "id=" + getId() +
            ", dateHeure='" + getDateHeure() + "'" +
            ", dateReportee='" + getDateReportee() + "'" +
            ", lieu='" + getLieu() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", motif='" + getMotif() + "'" +
            ", statut='" + getStatut() + "'" +
            "}";
    }
}
