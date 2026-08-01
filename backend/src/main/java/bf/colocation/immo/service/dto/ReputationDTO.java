package bf.colocation.immo.service.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Synthèse de réputation d'un démarcheur : moyenne, nombre d'avis, et le détail.
 */
public class ReputationDTO implements Serializable {

    private Long demarcheurId;
    private Double moyenne;
    private long nombreAvis;
    private List<AvisDTO> avis;

    public Long getDemarcheurId() {
        return demarcheurId;
    }

    public void setDemarcheurId(Long demarcheurId) {
        this.demarcheurId = demarcheurId;
    }

    public Double getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(Double moyenne) {
        this.moyenne = moyenne;
    }

    public long getNombreAvis() {
        return nombreAvis;
    }

    public void setNombreAvis(long nombreAvis) {
        this.nombreAvis = nombreAvis;
    }

    public List<AvisDTO> getAvis() {
        return avis;
    }

    public void setAvis(List<AvisDTO> avis) {
        this.avis = avis;
    }
}
