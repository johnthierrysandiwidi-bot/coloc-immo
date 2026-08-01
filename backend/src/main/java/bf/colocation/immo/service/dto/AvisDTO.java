package bf.colocation.immo.service.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * Représentation d'un avis exposée au client.
 * On n'expose que le login de l'auteur — pas l'entité User complète.
 */
public class AvisDTO implements Serializable {

    private Long id;
    private Integer note;
    private String commentaire;
    private Instant dateCreation;
    private String auteurLogin;
    private Long demarcheurId;
    private Long rendezVousId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getAuteurLogin() {
        return auteurLogin;
    }

    public void setAuteurLogin(String auteurLogin) {
        this.auteurLogin = auteurLogin;
    }

    public Long getDemarcheurId() {
        return demarcheurId;
    }

    public void setDemarcheurId(Long demarcheurId) {
        this.demarcheurId = demarcheurId;
    }

    public Long getRendezVousId() {
        return rendezVousId;
    }

    public void setRendezVousId(Long rendezVousId) {
        this.rendezVousId = rendezVousId;
    }
}
