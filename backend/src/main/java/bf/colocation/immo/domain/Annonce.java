package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Annonce.
 */
@Entity
@Table(name = "annonce")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Annonce implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 150)
    @Column(name = "titre", length = 150, nullable = false)
    private String titre;

    @Size(max = 5000)
    @Column(name = "contenu", length = 5000)
    private String contenu;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeAnnonce type;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "prix", nullable = false)
    private Double prix;

    @Min(value = 0)
    @Column(name = "nombre_vues")
    private Integer nombreVues;

    @Column(name = "date_publication")
    private Instant datePublication;

    @Column(name = "date_expiration")
    private Instant dateExpiration;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutAnnonce statut;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "prixes", "imageses", "proprietaire", "demarcheur", "localite", "quartier", "typeImmobilier", "annonceses" },
        allowSetters = true
    )
    private Immobilier immobilier;

    @ManyToOne(optional = false)
    @NotNull
    private User auteur;

    @JsonIgnoreProperties(value = { "annonce", "equipementses" }, allowSetters = true)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "annonce")
    private DetailColocation detailColocation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "annonce")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "annonce", "utilisateur" }, allowSetters = true)
    private Set<VueAnnonce> vueses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "annonce")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "annonce", "demandeur" }, allowSetters = true)
    private Set<RendezVous> rendezVouses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "annonce")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "annonce", "utilisateur" }, allowSetters = true)
    private Set<Favori> favorises = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Annonce id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return this.titre;
    }

    public Annonce titre(String titre) {
        this.setTitre(titre);
        return this;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return this.contenu;
    }

    public Annonce contenu(String contenu) {
        this.setContenu(contenu);
        return this;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public TypeAnnonce getType() {
        return this.type;
    }

    public Annonce type(TypeAnnonce type) {
        this.setType(type);
        return this;
    }

    public void setType(TypeAnnonce type) {
        this.type = type;
    }

    public Double getPrix() {
        return this.prix;
    }

    public Annonce prix(Double prix) {
        this.setPrix(prix);
        return this;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Integer getNombreVues() {
        return this.nombreVues;
    }

    public Annonce nombreVues(Integer nombreVues) {
        this.setNombreVues(nombreVues);
        return this;
    }

    public void setNombreVues(Integer nombreVues) {
        this.nombreVues = nombreVues;
    }

    public Instant getDatePublication() {
        return this.datePublication;
    }

    public Annonce datePublication(Instant datePublication) {
        this.setDatePublication(datePublication);
        return this;
    }

    public void setDatePublication(Instant datePublication) {
        this.datePublication = datePublication;
    }

    public Instant getDateExpiration() {
        return this.dateExpiration;
    }

    public Annonce dateExpiration(Instant dateExpiration) {
        this.setDateExpiration(dateExpiration);
        return this;
    }

    public void setDateExpiration(Instant dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public StatutAnnonce getStatut() {
        return this.statut;
    }

    public Annonce statut(StatutAnnonce statut) {
        this.setStatut(statut);
        return this;
    }

    public void setStatut(StatutAnnonce statut) {
        this.statut = statut;
    }

    public Immobilier getImmobilier() {
        return this.immobilier;
    }

    public void setImmobilier(Immobilier immobilier) {
        this.immobilier = immobilier;
    }

    public Annonce immobilier(Immobilier immobilier) {
        this.setImmobilier(immobilier);
        return this;
    }

    public User getAuteur() {
        return this.auteur;
    }

    public void setAuteur(User user) {
        this.auteur = user;
    }

    public Annonce auteur(User user) {
        this.setAuteur(user);
        return this;
    }

    public DetailColocation getDetailColocation() {
        return this.detailColocation;
    }

    public void setDetailColocation(DetailColocation detailColocation) {
        if (this.detailColocation != null) {
            this.detailColocation.setAnnonce(null);
        }
        if (detailColocation != null) {
            detailColocation.setAnnonce(this);
        }
        this.detailColocation = detailColocation;
    }

    public Annonce detailColocation(DetailColocation detailColocation) {
        this.setDetailColocation(detailColocation);
        return this;
    }

    public Set<VueAnnonce> getVueses() {
        return this.vueses;
    }

    public void setVueses(Set<VueAnnonce> vueAnnonces) {
        if (this.vueses != null) {
            this.vueses.forEach(i -> i.setAnnonce(null));
        }
        if (vueAnnonces != null) {
            vueAnnonces.forEach(i -> i.setAnnonce(this));
        }
        this.vueses = vueAnnonces;
    }

    public Annonce vueses(Set<VueAnnonce> vueAnnonces) {
        this.setVueses(vueAnnonces);
        return this;
    }

    public Annonce addVues(VueAnnonce vueAnnonce) {
        this.vueses.add(vueAnnonce);
        vueAnnonce.setAnnonce(this);
        return this;
    }

    public Annonce removeVues(VueAnnonce vueAnnonce) {
        this.vueses.remove(vueAnnonce);
        vueAnnonce.setAnnonce(null);
        return this;
    }

    public Set<RendezVous> getRendezVouses() {
        return this.rendezVouses;
    }

    public void setRendezVouses(Set<RendezVous> rendezVouses) {
        if (this.rendezVouses != null) {
            this.rendezVouses.forEach(i -> i.setAnnonce(null));
        }
        if (rendezVouses != null) {
            rendezVouses.forEach(i -> i.setAnnonce(this));
        }
        this.rendezVouses = rendezVouses;
    }

    public Annonce rendezVouses(Set<RendezVous> rendezVouses) {
        this.setRendezVouses(rendezVouses);
        return this;
    }

    public Annonce addRendezVous(RendezVous rendezVous) {
        this.rendezVouses.add(rendezVous);
        rendezVous.setAnnonce(this);
        return this;
    }

    public Annonce removeRendezVous(RendezVous rendezVous) {
        this.rendezVouses.remove(rendezVous);
        rendezVous.setAnnonce(null);
        return this;
    }

    public Set<Favori> getFavorises() {
        return this.favorises;
    }

    public void setFavorises(Set<Favori> favoris) {
        if (this.favorises != null) {
            this.favorises.forEach(i -> i.setAnnonce(null));
        }
        if (favoris != null) {
            favoris.forEach(i -> i.setAnnonce(this));
        }
        this.favorises = favoris;
    }

    public Annonce favorises(Set<Favori> favoris) {
        this.setFavorises(favoris);
        return this;
    }

    public Annonce addFavoris(Favori favori) {
        this.favorises.add(favori);
        favori.setAnnonce(this);
        return this;
    }

    public Annonce removeFavoris(Favori favori) {
        this.favorises.remove(favori);
        favori.setAnnonce(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Annonce)) {
            return false;
        }
        return getId() != null && getId().equals(((Annonce) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Annonce{" +
            "id=" + getId() +
            ", titre='" + getTitre() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", type='" + getType() + "'" +
            ", prix=" + getPrix() +
            ", nombreVues=" + getNombreVues() +
            ", datePublication='" + getDatePublication() + "'" +
            ", dateExpiration='" + getDateExpiration() + "'" +
            ", statut='" + getStatut() + "'" +
            "}";
    }
}
