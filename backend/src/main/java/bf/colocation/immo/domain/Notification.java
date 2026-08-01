package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.TypeNotification;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Notification.
 */
@Entity
@Table(name = "notification")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeNotification type;

    @NotNull
    @Size(max = 150)
    @Column(name = "titre", length = 150, nullable = false)
    private String titre;

    @NotNull
    @Size(max = 500)
    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Size(max = 255)
    @Column(name = "lien", length = 255)
    private String lien;

    @NotNull
    @Column(name = "lue", nullable = false)
    private Boolean lue;

    @NotNull
    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @ManyToOne(optional = false)
    @NotNull
    private User destinataire;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Notification id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeNotification getType() {
        return this.type;
    }

    public Notification type(TypeNotification type) {
        this.setType(type);
        return this;
    }

    public void setType(TypeNotification type) {
        this.type = type;
    }

    public String getTitre() {
        return this.titre;
    }

    public Notification titre(String titre) {
        this.setTitre(titre);
        return this;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getMessage() {
        return this.message;
    }

    public Notification message(String message) {
        this.setMessage(message);
        return this;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLien() {
        return this.lien;
    }

    public Notification lien(String lien) {
        this.setLien(lien);
        return this;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public Boolean getLue() {
        return this.lue;
    }

    public Notification lue(Boolean lue) {
        this.setLue(lue);
        return this;
    }

    public void setLue(Boolean lue) {
        this.lue = lue;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public Notification dateCreation(Instant dateCreation) {
        this.setDateCreation(dateCreation);
        return this;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public User getDestinataire() {
        return this.destinataire;
    }

    public void setDestinataire(User user) {
        this.destinataire = user;
    }

    public Notification destinataire(User user) {
        this.setDestinataire(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        return getId() != null && getId().equals(((Notification) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Notification{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", titre='" + getTitre() + "'" +
            ", message='" + getMessage() + "'" +
            ", lien='" + getLien() + "'" +
            ", lue='" + getLue() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            "}";
    }
}
