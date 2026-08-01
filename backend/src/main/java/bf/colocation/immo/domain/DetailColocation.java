package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.SexeRecherche;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A DetailColocation.
 */
@Entity
@Table(name = "detail_colocation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DetailColocation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Min(value = 1)
    @Column(name = "nombre_places", nullable = false)
    private Integer nombrePlaces;

    @NotNull
    @Min(value = 0)
    @Column(name = "places_restantes", nullable = false)
    private Integer placesRestantes;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sexe_recherche", nullable = false)
    private SexeRecherche sexeRecherche;

    @Min(value = 16)
    @Max(value = 120)
    @Column(name = "age_min")
    private Integer ageMin;

    @Min(value = 16)
    @Max(value = 120)
    @Column(name = "age_max")
    private Integer ageMax;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "loyer", nullable = false)
    private Double loyer;

    @DecimalMin(value = "0")
    @Column(name = "caution")
    private Double caution;

    @DecimalMin(value = "0")
    @Column(name = "charges")
    private Double charges;

    @Size(max = 3000)
    @Column(name = "regles_de_vie", length = 3000)
    private String reglesDeVie;

    @JsonIgnoreProperties(
        value = { "immobilier", "auteur", "detailColocation", "vueses", "rendezVouses", "favorises" },
        allowSetters = true
    )
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JoinColumn(unique = true)
    private Annonce annonce;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_detail_colocation__equipements",
        joinColumns = @JoinColumn(name = "detail_colocation_id"),
        inverseJoinColumns = @JoinColumn(name = "equipements_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "colocationses" }, allowSetters = true)
    private Set<Equipement> equipementses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DetailColocation id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNombrePlaces() {
        return this.nombrePlaces;
    }

    public DetailColocation nombrePlaces(Integer nombrePlaces) {
        this.setNombrePlaces(nombrePlaces);
        return this;
    }

    public void setNombrePlaces(Integer nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public Integer getPlacesRestantes() {
        return this.placesRestantes;
    }

    public DetailColocation placesRestantes(Integer placesRestantes) {
        this.setPlacesRestantes(placesRestantes);
        return this;
    }

    public void setPlacesRestantes(Integer placesRestantes) {
        this.placesRestantes = placesRestantes;
    }

    public SexeRecherche getSexeRecherche() {
        return this.sexeRecherche;
    }

    public DetailColocation sexeRecherche(SexeRecherche sexeRecherche) {
        this.setSexeRecherche(sexeRecherche);
        return this;
    }

    public void setSexeRecherche(SexeRecherche sexeRecherche) {
        this.sexeRecherche = sexeRecherche;
    }

    public Integer getAgeMin() {
        return this.ageMin;
    }

    public DetailColocation ageMin(Integer ageMin) {
        this.setAgeMin(ageMin);
        return this;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return this.ageMax;
    }

    public DetailColocation ageMax(Integer ageMax) {
        this.setAgeMax(ageMax);
        return this;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public Double getLoyer() {
        return this.loyer;
    }

    public DetailColocation loyer(Double loyer) {
        this.setLoyer(loyer);
        return this;
    }

    public void setLoyer(Double loyer) {
        this.loyer = loyer;
    }

    public Double getCaution() {
        return this.caution;
    }

    public DetailColocation caution(Double caution) {
        this.setCaution(caution);
        return this;
    }

    public void setCaution(Double caution) {
        this.caution = caution;
    }

    public Double getCharges() {
        return this.charges;
    }

    public DetailColocation charges(Double charges) {
        this.setCharges(charges);
        return this;
    }

    public void setCharges(Double charges) {
        this.charges = charges;
    }

    public String getReglesDeVie() {
        return this.reglesDeVie;
    }

    public DetailColocation reglesDeVie(String reglesDeVie) {
        this.setReglesDeVie(reglesDeVie);
        return this;
    }

    public void setReglesDeVie(String reglesDeVie) {
        this.reglesDeVie = reglesDeVie;
    }

    public Annonce getAnnonce() {
        return this.annonce;
    }

    public void setAnnonce(Annonce annonce) {
        this.annonce = annonce;
    }

    public DetailColocation annonce(Annonce annonce) {
        this.setAnnonce(annonce);
        return this;
    }

    public Set<Equipement> getEquipementses() {
        return this.equipementses;
    }

    public void setEquipementses(Set<Equipement> equipements) {
        this.equipementses = equipements;
    }

    public DetailColocation equipementses(Set<Equipement> equipements) {
        this.setEquipementses(equipements);
        return this;
    }

    public DetailColocation addEquipements(Equipement equipement) {
        this.equipementses.add(equipement);
        return this;
    }

    public DetailColocation removeEquipements(Equipement equipement) {
        this.equipementses.remove(equipement);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DetailColocation)) {
            return false;
        }
        return getId() != null && getId().equals(((DetailColocation) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DetailColocation{" +
            "id=" + getId() +
            ", nombrePlaces=" + getNombrePlaces() +
            ", placesRestantes=" + getPlacesRestantes() +
            ", sexeRecherche='" + getSexeRecherche() + "'" +
            ", ageMin=" + getAgeMin() +
            ", ageMax=" + getAgeMax() +
            ", loyer=" + getLoyer() +
            ", caution=" + getCaution() +
            ", charges=" + getCharges() +
            ", reglesDeVie='" + getReglesDeVie() + "'" +
            "}";
    }
}
