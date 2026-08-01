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
 * A VueAnnonce.
 */
@Entity
@Table(name = "vue_annonce")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VueAnnonce implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "date_vue", nullable = false)
    private Instant dateVue;

    @Size(max = 45)
    @Column(name = "adresse_ip", length = 45)
    private String adresseIp;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "immobilier", "auteur", "detailColocation", "vueses", "rendezVouses", "favorises" },
        allowSetters = true
    )
    private Annonce annonce;

    @ManyToOne(fetch = FetchType.LAZY)
    private User utilisateur;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public VueAnnonce id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getDateVue() {
        return this.dateVue;
    }

    public VueAnnonce dateVue(Instant dateVue) {
        this.setDateVue(dateVue);
        return this;
    }

    public void setDateVue(Instant dateVue) {
        this.dateVue = dateVue;
    }

    public String getAdresseIp() {
        return this.adresseIp;
    }

    public VueAnnonce adresseIp(String adresseIp) {
        this.setAdresseIp(adresseIp);
        return this;
    }

    public void setAdresseIp(String adresseIp) {
        this.adresseIp = adresseIp;
    }

    public Annonce getAnnonce() {
        return this.annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public VueAnnonce annonce(Annonce annonce) {
        this.setAnnonce(annonce);
        return this;
    }

    public User getUtilisateur() {
        return this.utilisateur;
    }

    public void setUtilisateur(User user) {
        this.utilisateur = user;
    }

    public VueAnnonce utilisateur(User user) {
        this.setUtilisateur(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VueAnnonce)) {
            return false;
        }
        return getId() != null && getId().equals(((VueAnnonce) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "VueAnnonce{" +
            "id=" + getId() +
            ", dateVue='" + getDateVue() + "'" +
            ", adresseIp='" + getAdresseIp() + "'" +
            "}";
    }
}
