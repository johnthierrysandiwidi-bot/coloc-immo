package bf.colocation.immo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Fil de discussion entre deux utilisateurs, à propos d'une annonce.
 *
 * <p>Le couple (annonce, participant1, participant2) est unique : on ne crée jamais
 * deux fils pour la même paire sur la même annonce. {@code dernierMessageLe} est
 * dénormalisé pour trier par activité sans parcourir les messages.</p>
 */
@Entity
@Table(
    name = "conversation",
    uniqueConstraints = @UniqueConstraint(columnNames = { "annonce_id", "participant1_id", "participant2_id" })
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Conversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @Column(name = "dernier_message_le")
    private Instant dernierMessageLe;

    @ManyToOne(optional = false)
    @JsonIgnoreProperties(value = { "prixes", "imageses", "proprietaire", "demarcheur" }, allowSetters = true)
    private Annonce annonce;

    @ManyToOne(optional = false)
    private User participant1;

    @ManyToOne(optional = false)
    private User participant2;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getDateCreation() { return dateCreation; }
    public void setDateCreation(Instant dateCreation) { this.dateCreation = dateCreation; }
    public Instant getDernierMessageLe() { return dernierMessageLe; }
    public void setDernierMessageLe(Instant dernierMessageLe) { this.dernierMessageLe = dernierMessageLe; }
    public Annonce getAnnonce() { return annonce; }
    public void setAnnonce(Annonce annonce) { this.annonce = annonce; }
    public User getParticipant1() { return participant1; }
    public void setParticipant1(User participant1) { this.participant1 = participant1; }
    public User getParticipant2() { return participant2; }
    public void setParticipant2(User participant2) { this.participant2 = participant2; }

    /** L'autre participant, vu depuis un utilisateur donné. */
    public User autre(Long utilisateurId) {
        if (participant1 != null && participant1.getId().equals(utilisateurId)) {
            return participant2;
        }
        return participant1;
    }

    /** Cet utilisateur fait-il partie de la conversation ? */
    public boolean concerne(Long utilisateurId) {
        return (participant1 != null && participant1.getId().equals(utilisateurId)) ||
            (participant2 != null && participant2.getId().equals(utilisateurId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation)) return false;
        return getId() != null && getId().equals(((Conversation) o).getId());
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
