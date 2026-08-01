package bf.colocation.immo.domain;

import bf.colocation.immo.domain.enumeration.MoyenPaiement;
import bf.colocation.immo.domain.enumeration.StatutPaiement;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Paiement des frais de visite, avec séquestre (module V2, passerelle simulée).
 */
@Entity
@Table(name = "paiement")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Paiement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    // MySQL ne gère pas les séquences : la stratégie SEQUENCE amenait Hibernate à les
    // émuler via une table « sequence_generator » que Liquibase ne crée jamais, d'où
    // l'échec de tout paiement (« Table 'sequence_generator' doesn't exist »).
    // La colonne id est déclarée autoIncrement dans le changelog, et les 22 autres
    // entités utilisent IDENTITY : on s'aligne, sans aucune migration de base.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "reference", length = 40, nullable = false, unique = true)
    private String reference;

    @NotNull
    @Column(name = "montant", nullable = false)
    private Double montant;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutPaiement statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "moyen", length = 20)
    private MoyenPaiement moyen;

    @NotNull
    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @Column(name = "date_sequestre")
    private Instant dateSequestre;

    @Column(name = "date_denouement")
    private Instant dateDenouement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rendez_vous_id", unique = true)
    @JsonIgnoreProperties(value = { "annonce", "demandeur" }, allowSetters = true)
    private RendezVous rendezVous;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payeur_id")
    private User payeur;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public MoyenPaiement getMoyen() {
        return moyen;
    }

    public void setMoyen(MoyenPaiement moyen) {
        this.moyen = moyen;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Instant getDateSequestre() {
        return dateSequestre;
    }

    public void setDateSequestre(Instant dateSequestre) {
        this.dateSequestre = dateSequestre;
    }

    public Instant getDateDenouement() {
        return dateDenouement;
    }

    public void setDateDenouement(Instant dateDenouement) {
        this.dateDenouement = dateDenouement;
    }

    public RendezVous getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(RendezVous rendezVous) {
        this.rendezVous = rendezVous;
    }

    public User getPayeur() {
        return payeur;
    }

    public void setPayeur(User payeur) {
        this.payeur = payeur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paiement)) return false;
        return id != null && id.equals(((Paiement) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Paiement{id=" + id + ", reference='" + reference + "', statut=" + statut + ", montant=" + montant + "}";
    }
}
