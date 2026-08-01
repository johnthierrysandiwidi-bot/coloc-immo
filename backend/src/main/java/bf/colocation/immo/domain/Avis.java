package bf.colocation.immo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Avis laissé par un locataire sur un démarcheur, après une visite effectuée.
 *
 * <p>Prolonge le cycle du rendez-vous : un avis n'est possible qu'une fois la visite
 * réellement terminée (statut TERMINE), ce qui garantit qu'il repose sur une rencontre
 * réelle et non sur une simple demande. La note alimente la réputation du démarcheur,
 * renforçant l'axe de confiance de la plateforme au-delà de la seule vérification
 * d'identité.</p>
 *
 * <p>Un rendez-vous ne peut donner lieu qu'à un seul avis : la contrainte d'unicité sur
 * {@code rendez_vous_id} l'empêche techniquement.</p>
 */
@Entity
@Table(name = "avis", uniqueConstraints = @UniqueConstraint(columnNames = "rendez_vous_id"))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Avis implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Note de 1 à 5 étoiles. */
    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "note", nullable = false)
    private Integer note;

    @Size(max = 1000)
    @Column(name = "commentaire", length = 1000)
    private String commentaire;

    @NotNull
    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    /** La visite qui fonde cet avis. Unique : un rendez-vous, un avis. */
    @ManyToOne(optional = false)
    @JsonIgnoreProperties(value = { "annonce", "demandeur" }, allowSetters = true)
    private RendezVous rendezVous;

    /** L'auteur de l'avis : le locataire qui a visité. */
    @ManyToOne(optional = false)
    private User auteur;

    /** La cible de l'avis : le démarcheur (auteur de l'annonce). */
    @ManyToOne(optional = false)
    private User demarcheur;

    public Long getId() {
        return this.id;
    }

    public Avis id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNote() {
        return this.note;
    }

    public Avis note(Integer note) {
        this.setNote(note);
        return this;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return this.commentaire;
    }

    public Avis commentaire(String commentaire) {
        this.setCommentaire(commentaire);
        return this;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public Avis dateCreation(Instant dateCreation) {
        this.setDateCreation(dateCreation);
        return this;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public RendezVous getRendezVous() {
        return this.rendezVous;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }

    public Avis rendezVous(RendezVous rendezVous) {
        this.setRendezVous(rendezVous);
        return this;
    }

    public User getAuteur() {
        return this.auteur;
    }

    public void setAuteur(User auteur) {
        this.auteur = auteur;
    }

    public Avis auteur(User auteur) {
        this.setAuteur(auteur);
        return this;
    }

    public User getDemarcheur() {
        return this.demarcheur;
    }

    public void setDemarcheur(User demarcheur) {
        this.demarcheur = demarcheur;
    }

    public Avis demarcheur(User demarcheur) {
        this.setDemarcheur(demarcheur);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Avis)) {
            return false;
        }
        return getId() != null && getId().equals(((Avis) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Avis{" + "id=" + getId() + ", note=" + getNote() + ", dateCreation='" + getDateCreation() + "'}";
    }
}
