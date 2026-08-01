package bf.colocation.immo.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Image.
 */
@Entity
@Table(name = "image")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Image implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Size(max = 150)
    @Column(name = "nom", length = 150)
    private String nom;

    @NotNull
    @Size(max = 255)
    @Column(name = "url", length = 255, nullable = false)
    private String url;

    @Min(value = 0)
    @Column(name = "ordre")
    private Integer ordre;

    @Column(name = "principale")
    private Boolean principale;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "prixes", "imageses", "proprietaire", "demarcheur", "localite", "quartier", "typeImmobilier", "annonceses" },
        allowSetters = true
    )
    private Immobilier immobilier;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Image id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public Image nom(String nom) {
        this.setNom(nom);
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getUrl() {
        return this.url;
    }

    public Image url(String url) {
        this.setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrdre() {
        return this.ordre;
    }

    public Image ordre(Integer ordre) {
        this.setOrdre(ordre);
        return this;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public Boolean getPrincipale() {
        return this.principale;
    }

    public Image principale(Boolean principale) {
        this.setPrincipale(principale);
        return this;
    }

    public void setPrincipale(Boolean principale) {
        this.principale = principale;
    }

    public Immobilier getImmobilier() {
        return this.immobilier;
    }

    public void setImmobilier(Immobilier immobilier) {
        this.immobilier = immobilier;
    }

    public Image immobilier(Immobilier immobilier) {
        this.setImmobilier(immobilier);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Image)) {
            return false;
        }
        return getId() != null && getId().equals(((Image) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Image{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", url='" + getUrl() + "'" +
            ", ordre=" + getOrdre() +
            ", principale='" + getPrincipale() + "'" +
            "}";
    }
}
