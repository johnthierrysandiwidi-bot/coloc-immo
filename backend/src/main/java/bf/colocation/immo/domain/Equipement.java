package bf.colocation.immo.domain;

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
 * A Equipement.
 */
@Entity
@Table(name = "equipement")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Equipement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 80)
    @Column(name = "nom", length = 80, nullable = false)
    private String nom;

    @Size(max = 80)
    @Column(name = "icone", length = 80)
    private String icone;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "equipementses")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "annonce", "equipementses" }, allowSetters = true)
    private Set<DetailColocation> colocationses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Equipement id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public Equipement nom(String nom) {
        this.setNom(nom);
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getIcone() {
        return this.icone;
    }

    public Equipement icone(String icone) {
        this.setIcone(icone);
        return this;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public Set<DetailColocation> getColocationses() {
        return this.colocationses;
    }

    public void setColocationses(Set<DetailColocation> detailColocations) {
        if (this.colocationses != null) {
            this.colocationses.forEach(i -> i.removeEquipements(this));
        }
        if (detailColocations != null) {
            detailColocations.forEach(i -> i.addEquipements(this));
        }
        this.colocationses = detailColocations;
    }

    public Equipement colocationses(Set<DetailColocation> detailColocations) {
        this.setColocationses(detailColocations);
        return this;
    }

    public Equipement addColocations(DetailColocation detailColocation) {
        this.colocationses.add(detailColocation);
        detailColocation.getEquipementses().add(this);
        return this;
    }

    public Equipement removeColocations(DetailColocation detailColocation) {
        this.colocationses.remove(detailColocation);
        detailColocation.getEquipementses().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Equipement)) {
            return false;
        }
        return getId() != null && getId().equals(((Equipement) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Equipement{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", icone='" + getIcone() + "'" +
            "}";
    }
}
