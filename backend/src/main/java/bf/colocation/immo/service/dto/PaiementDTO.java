package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.MoyenPaiement;
import bf.colocation.immo.domain.enumeration.StatutPaiement;
import java.io.Serializable;
import java.time.Instant;

public class PaiementDTO implements Serializable {

    private Long id;
    private String reference;
    private Double montant;
    private StatutPaiement statut;
    private MoyenPaiement moyen;
    private Instant dateCreation;
    private Instant dateSequestre;
    private Instant dateDenouement;
    private Long rendezVousId;
    private String annonceTitre;
    private String payeurLogin;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }
    public StatutPaiement getStatut() { return statut; }
    public void setStatut(StatutPaiement statut) { this.statut = statut; }
    public MoyenPaiement getMoyen() { return moyen; }
    public void setMoyen(MoyenPaiement moyen) { this.moyen = moyen; }
    public Instant getDateCreation() { return dateCreation; }
    public void setDateCreation(Instant d) { this.dateCreation = d; }
    public Instant getDateSequestre() { return dateSequestre; }
    public void setDateSequestre(Instant d) { this.dateSequestre = d; }
    public Instant getDateDenouement() { return dateDenouement; }
    public void setDateDenouement(Instant d) { this.dateDenouement = d; }
    public Long getRendezVousId() { return rendezVousId; }
    public void setRendezVousId(Long id) { this.rendezVousId = id; }
    public String getAnnonceTitre() { return annonceTitre; }
    public void setAnnonceTitre(String t) { this.annonceTitre = t; }
    public String getPayeurLogin() { return payeurLogin; }
    public void setPayeurLogin(String l) { this.payeurLogin = l; }

    /** Conversion depuis l'entité, sans MapStruct pour éviter tout souci de lazy loading. */
    public static PaiementDTO de(bf.colocation.immo.domain.Paiement p) {
        PaiementDTO d = new PaiementDTO();
        d.setId(p.getId());
        d.setReference(p.getReference());
        d.setMontant(p.getMontant());
        d.setStatut(p.getStatut());
        d.setMoyen(p.getMoyen());
        d.setDateCreation(p.getDateCreation());
        d.setDateSequestre(p.getDateSequestre());
        d.setDateDenouement(p.getDateDenouement());
        if (p.getRendezVous() != null) {
            d.setRendezVousId(p.getRendezVous().getId());
            if (p.getRendezVous().getAnnonce() != null) {
                d.setAnnonceTitre(p.getRendezVous().getAnnonce().getTitre());
            }
        }
        if (p.getPayeur() != null) {
            d.setPayeurLogin(p.getPayeur().getLogin());
        }
        return d;
    }
}
