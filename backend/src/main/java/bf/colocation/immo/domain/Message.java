package bf.colocation.immo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/** Message échangé au sein d'une conversation. */
@Entity
@Table(name = "message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "contenu", length = 2000, nullable = false)
    private String contenu;

    @Column(name = "date_envoi", nullable = false)
    private Instant dateEnvoi;

    /** Vrai une fois que le destinataire a ouvert la conversation. */
    @Column(name = "lu", nullable = false)
    private boolean lu = false;

    @ManyToOne(optional = false)
    @JsonIgnoreProperties(value = { "annonce", "participant1", "participant2" }, allowSetters = true)
    private Conversation conversation;

    @ManyToOne(optional = false)
    private User expediteur;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Instant getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Instant dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public User getExpediteur() { return expediteur; }
    public void setExpediteur(User expediteur) { this.expediteur = expediteur; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        return getId() != null && getId().equals(((Message) o).getId());
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
