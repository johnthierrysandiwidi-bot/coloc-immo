package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.StatutValidation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Document.
 */
@Entity
@Table(name = "document")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Document implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 150)
    @Column(name = "nom", length = 150, nullable = false)
    private String nom;

    @NotNull
    @Size(max = 255)
    @Column(name = "url", length = 255, nullable = false)
    private String url;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutValidation statut;

    @Size(max = 500)
    @Column(name = "motif_refus", length = 500)
    private String motifRefus;

    @Column(name = "date_ajout")
    private Instant dateAjout;

    @Column(name = "date_traitement")
    private Instant dateTraitement;

    @ManyToOne(optional = false)
    @NotNull
    private TypeDocument typeDocument;

    @ManyToOne(optional = false)
    @NotNull
    private User demarcheur;

    @ManyToOne(fetch = FetchType.LAZY)
    private User traitePar;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Document id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public Document nom(String nom) {
        this.setNom(nom);
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getUrl() {
        return this.url;
    }

    public Document url(String url) {
        this.setUrl(url);
        return this;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public StatutValidation getStatut() {
        return this.statut;
    }

    public Document statut(StatutValidation statut) {
        this.setStatut(statut);
        return this;
    }

    public void setStatut(StatutValidation statut) {
        this.statut = statut;
    }

    public String getMotifRefus() {
        return this.motifRefus;
    }

    public Document motifRefus(String motifRefus) {
        this.setMotifRefus(motifRefus);
        return this;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public Instant getDateAjout() {
        return this.dateAjout;
    }

    public Document dateAjout(Instant dateAjout) {
        this.setDateAjout(dateAjout);
        return this;
    }

    public void setDateAjout(Instant dateAjout) {
        this.dateAjout = dateAjout;
    }

    public Instant getDateTraitement() {
        return this.dateTraitement;
    }

    public Document dateTraitement(Instant dateTraitement) {
        this.setDateTraitement(dateTraitement);
        return this;
    }

    public void setDateTraitement(Instant dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public TypeDocument getTypeDocument() {
        return this.typeDocument;
    }

    public void setTypeDocument(TypeDocument typeDocument) {
        this.typeDocument = typeDocument;
    }

    public Document typeDocument(TypeDocument typeDocument) {
        this.setTypeDocument(typeDocument);
        return this;
    }

    public User getDemarcheur() {
        return this.demarcheur;
    }

    public void setDemarcheur(User user) {
        this.demarcheur = user;
    }

    public Document demarcheur(User user) {
        this.setDemarcheur(user);
        return this;
    }

    public User getTraitePar() {
        return this.traitePar;
    }

    public void setTraitePar(User user) {
        this.traitePar = user;
    }

    public Document traitePar(User user) {
        this.setTraitePar(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Document)) {
            return false;
        }
        return getId() != null && getId().equals(((Document) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Document{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", url='" + getUrl() + "'" +
            ", statut='" + getStatut() + "'" +
            ", motifRefus='" + getMotifRefus() + "'" +
            ", dateAjout='" + getDateAjout() + "'" +
            ", dateTraitement='" + getDateTraitement() + "'" +
            "}";
    }
}
