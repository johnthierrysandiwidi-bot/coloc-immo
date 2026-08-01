package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.FrequenceAlerte;
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
 * A Alerte.
 */
@Entity
@Table(name = "alerte")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Alerte implements Serializable {

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

    @Size(max = 2000)
    @Column(name = "contenu", length = 2000)
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_annonce")
    private TypeAnnonce typeAnnonce;

    @DecimalMin(value = "0")
    @Column(name = "prix_min")
    private Double prixMin;

    @DecimalMin(value = "0")
    @Column(name = "prix_max")
    private Double prixMax;

    @DecimalMin(value = "0")
    @Column(name = "surface_min")
    private Double surfaceMin;

    @Min(value = 0)
    @Column(name = "nombre_chambres_min")
    private Integer nombreChambresMin;

    @Column(name = "meuble_uniquement")
    private Boolean meubleUniquement;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "frequence", nullable = false)
    private FrequenceAlerte frequence;

    @Column(name = "derniere_execution")
    private Instant derniereExecution;

    @ManyToOne(optional = false)
    @NotNull
    private User titulaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "quartierses" }, allowSetters = true)
    private Localite localite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "localite" }, allowSetters = true)
    private Quartier quartier;

    @ManyToOne(fetch = FetchType.LAZY)
    private TypeImmobilier typeImmobilier;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "alerte")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "alerte", "annonce" }, allowSetters = true)
    private Set<AlerteNotifiee> notifieeses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Alerte id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return this.titre;
    }

    public Alerte titre(String titre) {
        this.setTitre(titre);
        return this;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return this.contenu;
    }

    public Alerte contenu(String contenu) {
        this.setContenu(contenu);
        return this;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public TypeAnnonce getTypeAnnonce() {
        return this.typeAnnonce;
    }

    public Alerte typeAnnonce(TypeAnnonce typeAnnonce) {
        this.setTypeAnnonce(typeAnnonce);
        return this;
    }

    public void setTypeAnnonce(TypeAnnonce typeAnnonce) {
        this.typeAnnonce = typeAnnonce;
    }

    public Double getPrixMin() {
        return this.prixMin;
    }

    public Alerte prixMin(Double prixMin) {
        this.setPrixMin(prixMin);
        return this;
    }

    public void setPrixMin(Double prixMin) {
        this.prixMin = prixMin;
    }

    public Double getPrixMax() {
        return this.prixMax;
    }

    public Alerte prixMax(Double prixMax) {
        this.setPrixMax(prixMax);
        return this;
    }

    public void setPrixMax(Double prixMax) {
        this.prixMax = prixMax;
    }

    public Double getSurfaceMin() {
        return this.surfaceMin;
    }

    public Alerte surfaceMin(Double surfaceMin) {
        this.setSurfaceMin(surfaceMin);
        return this;
    }

    public void setSurfaceMin(Double surfaceMin) {
        this.surfaceMin = surfaceMin;
    }

    public Integer getNombreChambresMin() {
        return this.nombreChambresMin;
    }

    public Alerte nombreChambresMin(Integer nombreChambresMin) {
        this.setNombreChambresMin(nombreChambresMin);
        return this;
    }

    public void setNombreChambresMin(Integer nombreChambresMin) {
        this.nombreChambresMin = nombreChambresMin;
    }

    public Boolean getMeubleUniquement() {
        return this.meubleUniquement;
    }

    public Alerte meubleUniquement(Boolean meubleUniquement) {
        this.setMeubleUniquement(meubleUniquement);
        return this;
    }

    public void setMeubleUniquement(Boolean meubleUniquement) {
        this.meubleUniquement = meubleUniquement;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Alerte active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public FrequenceAlerte getFrequence() {
        return this.frequence;
    }

    public Alerte frequence(FrequenceAlerte frequence) {
        this.setFrequence(frequence);
        return this;
    }

    public void setFrequence(FrequenceAlerte frequence) {
        this.frequence = frequence;
    }

    public Instant getDerniereExecution() {
        return this.derniereExecution;
    }

    public Alerte derniereExecution(Instant derniereExecution) {
        this.setDerniereExecution(derniereExecution);
        return this;
    }

    public void setDerniereExecution(Instant derniereExecution) {
        this.derniereExecution = derniereExecution;
    }

    public User getTitulaire() {
        return this.titulaire;
    }

    public void setTitulaire(User user) {
        this.titulaire = user;
    }

    public Alerte titulaire(User user) {
        this.setTitulaire(user);
        return this;
    }

    public Localite getLocalite() {
        return this.localite;
    }

    public void setLocalite(Localite localite) {
        this.localite = localite;
    }

    public Alerte localite(Localite localite) {
        this.setLocalite(localite);
        return this;
    }

    public Quartier getQuartier() {
        return this.quartier;
    }

    public void setQuartier(Quartier quartier) {
        this.quartier = quartier;
    }

    public Alerte quartier(Quartier quartier) {
        this.setQuartier(quartier);
        return this;
    }

    public TypeImmobilier getTypeImmobilier() {
        return this.typeImmobilier;
    }

    public void setTypeImmobilier(TypeImmobilier typeImmobilier) {
        this.typeImmobilier = typeImmobilier;
    }

    public Alerte typeImmobilier(TypeImmobilier typeImmobilier) {
        this.setTypeImmobilier(typeImmobilier);
        return this;
    }

    public Set<AlerteNotifiee> getNotifieeses() {
        return this.notifieeses;
    }

    public void setNotifieeses(Set<AlerteNotifiee> alerteNotifiees) {
        if (this.notifieeses != null) {
            this.notifieeses.forEach(i -> i.setAlerte(null));
        }
        if (alerteNotifiees != null) {
            alerteNotifiees.forEach(i -> i.setAlerte(this));
        }
        this.notifieeses = alerteNotifiees;
    }

    public Alerte notifieeses(Set<AlerteNotifiee> alerteNotifiees) {
        this.setNotifieeses(alerteNotifiees);
        return this;
    }

    public Alerte addNotifiees(AlerteNotifiee alerteNotifiee) {
        this.notifieeses.add(alerteNotifiee);
        alerteNotifiee.setAlerte(this);
        return this;
    }

    public Alerte removeNotifiees(AlerteNotifiee alerteNotifiee) {
        this.notifieeses.remove(alerteNotifiee);
        alerteNotifiee.setAlerte(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Alerte)) {
            return false;
        }
        return getId() != null && getId().equals(((Alerte) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Alerte{" +
            "id=" + getId() +
            ", titre='" + getTitre() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", typeAnnonce='" + getTypeAnnonce() + "'" +
            ", prixMin=" + getPrixMin() +
            ", prixMax=" + getPrixMax() +
            ", surfaceMin=" + getSurfaceMin() +
            ", nombreChambresMin=" + getNombreChambresMin() +
            ", meubleUniquement='" + getMeubleUniquement() + "'" +
            ", active='" + getActive() + "'" +
            ", frequence='" + getFrequence() + "'" +
            ", derniereExecution='" + getDerniereExecution() + "'" +
            "}";
    }
}
