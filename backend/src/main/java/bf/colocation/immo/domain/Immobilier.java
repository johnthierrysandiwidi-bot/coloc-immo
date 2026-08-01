package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.StatutBien;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Immobilier.
 */
@Entity
@Table(name = "immobilier")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Immobilier implements Serializable {

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

    @Size(max = 5000)
    @Column(name = "description", length = 5000)
    private String description;

    @Size(max = 255)
    @Column(name = "adresse", length = 255)
    private String adresse;

    @DecimalMin(value = "0")
    @Column(name = "surface")
    private Double surface;

    @Min(value = 0)
    @Column(name = "nombre_pieces")
    private Integer nombrePieces;

    @Min(value = 0)
    @Column(name = "nombre_chambres")
    private Integer nombreChambres;

    @Min(value = 0)
    @Column(name = "nombre_salles_bain")
    private Integer nombreSallesBain;

    @Min(value = 0)
    @Column(name = "nombre_salons")
    private Integer nombreSalons;

    @Column(name = "garage")
    private Boolean garage;

    @Column(name = "piscine")
    private Boolean piscine;

    @Column(name = "meuble")
    private Boolean meuble;

    @Column(name = "disponible_a")
    private LocalDate disponibleA;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutBien statut;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "date_creation")
    private Instant dateCreation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "immobilier")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "immobilier" }, allowSetters = true)
    private Set<Prix> prixes = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "immobilier")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "immobilier" }, allowSetters = true)
    private Set<Image> imageses = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    private User proprietaire;

    @ManyToOne(fetch = FetchType.LAZY)
    private User demarcheur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "quartierses" }, allowSetters = true)
    private Localite localite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "localite" }, allowSetters = true)
    private Quartier quartier;

    @ManyToOne(fetch = FetchType.LAZY)
    private TypeImmobilier typeImmobilier;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "immobilier")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(
        value = { "immobilier", "auteur", "detailColocation", "vueses", "rendezVouses", "favorises" },
        allowSetters = true
    )
    private Set<Annonce> annonceses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Immobilier id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public Immobilier nom(String nom) {
        this.setNom(nom);
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return this.description;
    }

    public Immobilier description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdresse() {
        return this.adresse;
    }

    public Immobilier adresse(String adresse) {
        this.setAdresse(adresse);
        return this;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Double getSurface() {
        return this.surface;
    }

    public Immobilier surface(Double surface) {
        this.setSurface(surface);
        return this;
    }

    public void setSurface(Double surface) {
        this.surface = surface;
    }

    public Integer getNombrePieces() {
        return this.nombrePieces;
    }

    public Immobilier nombrePieces(Integer nombrePieces) {
        this.setNombrePieces(nombrePieces);
        return this;
    }

    public void setNombrePieces(Integer nombrePieces) {
        this.nombrePieces = nombrePieces;
    }

    public Integer getNombreChambres() {
        return this.nombreChambres;
    }

    public Immobilier nombreChambres(Integer nombreChambres) {
        this.setNombreChambres(nombreChambres);
        return this;
    }

    public void setNombreChambres(Integer nombreChambres) {
        this.nombreChambres = nombreChambres;
    }

    public Integer getNombreSallesBain() {
        return this.nombreSallesBain;
    }

    public Immobilier nombreSallesBain(Integer nombreSallesBain) {
        this.setNombreSallesBain(nombreSallesBain);
        return this;
    }

    public void setNombreSallesBain(Integer nombreSallesBain) {
        this.nombreSallesBain = nombreSallesBain;
    }

    public Integer getNombreSalons() {
        return this.nombreSalons;
    }

    public Immobilier nombreSalons(Integer nombreSalons) {
        this.setNombreSalons(nombreSalons);
        return this;
    }

    public void setNombreSalons(Integer nombreSalons) {
        this.nombreSalons = nombreSalons;
    }

    public Boolean getGarage() {
        return this.garage;
    }

    public Immobilier garage(Boolean garage) {
        this.setGarage(garage);
        return this;
    }

    public void setGarage(Boolean garage) {
        this.garage = garage;
    }

    public Boolean getPiscine() {
        return this.piscine;
    }

    public Immobilier piscine(Boolean piscine) {
        this.setPiscine(piscine);
        return this;
    }

    public void setPiscine(Boolean piscine) {
        this.piscine = piscine;
    }

    public Boolean getMeuble() {
        return this.meuble;
    }

    public Immobilier meuble(Boolean meuble) {
        this.setMeuble(meuble);
        return this;
    }

    public void setMeuble(Boolean meuble) {
        this.meuble = meuble;
    }

    public LocalDate getDisponibleA() {
        return this.disponibleA;
    }

    public Immobilier disponibleA(LocalDate disponibleA) {
        this.setDisponibleA(disponibleA);
        return this;
    }

    public void setDisponibleA(LocalDate disponibleA) {
        this.disponibleA = disponibleA;
    }

    public StatutBien getStatut() {
        return this.statut;
    }

    public Immobilier statut(StatutBien statut) {
        this.setStatut(statut);
        return this;
    }

    public void setStatut(StatutBien statut) {
        this.statut = statut;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public Immobilier latitude(Double latitude) {
        this.setLatitude(latitude);
        return this;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public Immobilier longitude(Double longitude) {
        this.setLongitude(longitude);
        return this;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public Immobilier dateCreation(Instant dateCreation) {
        this.setDateCreation(dateCreation);
        return this;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Set<Prix> getPrixes() {
        return this.prixes;
    }

    public void setPrixes(Set<Prix> prixes) {
        if (this.prixes != null) {
            this.prixes.forEach(i -> i.setImmobilier(null));
        }
        if (prixes != null) {
            prixes.forEach(i -> i.setImmobilier(this));
        }
        this.prixes = prixes;
    }

    public Immobilier prixes(Set<Prix> prixes) {
        this.setPrixes(prixes);
        return this;
    }

    public Immobilier addPrix(Prix prix) {
        this.prixes.add(prix);
        prix.setImmobilier(this);
        return this;
    }

    public Immobilier removePrix(Prix prix) {
        this.prixes.remove(prix);
        prix.setImmobilier(null);
        return this;
    }

    public Set<Image> getImageses() {
        return this.imageses;
    }

    public void setImageses(Set<Image> images) {
        if (this.imageses != null) {
            this.imageses.forEach(i -> i.setImmobilier(null));
        }
        if (images != null) {
            images.forEach(i -> i.setImmobilier(this));
        }
        this.imageses = images;
    }

    public Immobilier imageses(Set<Image> images) {
        this.setImageses(images);
        return this;
    }

    public Immobilier addImages(Image image) {
        this.imageses.add(image);
        image.setImmobilier(this);
        return this;
    }

    public Immobilier removeImages(Image image) {
        this.imageses.remove(image);
        image.setImmobilier(null);
        return this;
    }

    public User getProprietaire() {
        return this.proprietaire;
    }

    public void setProprietaire(User user) {
        this.proprietaire = user;
    }

    public Immobilier proprietaire(User user) {
        this.setProprietaire(user);
        return this;
    }

    public User getDemarcheur() {
        return this.demarcheur;
    }

    public void setDemarcheur(User user) {
        this.demarcheur = user;
    }

    public Immobilier demarcheur(User user) {
        this.setDemarcheur(user);
        return this;
    }

    public Localite getLocalite() {
        return this.localite;
    }

    public void setLocalite(Localite localite) {
        this.localite = localite;
    }

    public Immobilier localite(Localite localite) {
        this.setLocalite(localite);
        return this;
    }

    public Quartier getQuartier() {
        return this.quartier;
    }

    public void setQuartier(Quartier quartier) {
        this.quartier = quartier;
    }

    public Immobilier quartier(Quartier quartier) {
        this.setQuartier(quartier);
        return this;
    }

    public TypeImmobilier getTypeImmobilier() {
        return this.typeImmobilier;
    }

    public void setTypeImmobilier(TypeImmobilier typeImmobilier) {
        this.typeImmobilier = typeImmobilier;
    }

    public Immobilier typeImmobilier(TypeImmobilier typeImmobilier) {
        this.setTypeImmobilier(typeImmobilier);
        return this;
    }

    public Set<Annonce> getAnnonceses() {
        return this.annonceses;
    }

    public void setAnnonceses(Set<Annonce> annonces) {
        if (this.annonceses != null) {
            this.annonceses.forEach(i -> i.setImmobilier(null));
        }
        if (annonces != null) {
            annonces.forEach(i -> i.setImmobilier(this));
        }
        this.annonceses = annonces;
    }

    public Immobilier annonceses(Set<Annonce> annonces) {
        this.setAnnonceses(annonces);
        return this;
    }

    public Immobilier addAnnonces(Annonce annonce) {
        this.annonceses.add(annonce);
        annonce.setImmobilier(this);
        return this;
    }

    public Immobilier removeAnnonces(Annonce annonce) {
        this.annonceses.remove(annonce);
        annonce.setImmobilier(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Immobilier)) {
            return false;
        }
        return getId() != null && getId().equals(((Immobilier) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Immobilier{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            ", adresse='" + getAdresse() + "'" +
            ", surface=" + getSurface() +
            ", nombrePieces=" + getNombrePieces() +
            ", nombreChambres=" + getNombreChambres() +
            ", nombreSallesBain=" + getNombreSallesBain() +
            ", nombreSalons=" + getNombreSalons() +
            ", garage='" + getGarage() + "'" +
            ", piscine='" + getPiscine() + "'" +
            ", meuble='" + getMeuble() + "'" +
            ", disponibleA='" + getDisponibleA() + "'" +
            ", statut='" + getStatut() + "'" +
            ", latitude=" + getLatitude() +
            ", longitude=" + getLongitude() +
            ", dateCreation='" + getDateCreation() + "'" +
            "}";
    }
}
