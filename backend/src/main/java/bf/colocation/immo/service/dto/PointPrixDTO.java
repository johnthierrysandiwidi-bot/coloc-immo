package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.Prix;
import java.io.Serializable;
import java.time.LocalDate;

/** Un point de l'historique de prix, pour affichage d'une courbe. */
public class PointPrixDTO implements Serializable {

    private Double prix;
    private Double charges;
    private String periodicite;
    private LocalDate dateEffet;
    private String description;

    public static PointPrixDTO de(Prix p) {
        PointPrixDTO d = new PointPrixDTO();
        d.prix = p.getPrix();
        d.charges = p.getCharges();
        d.periodicite = p.getPeriodicite() != null ? p.getPeriodicite().name() : null;
        d.dateEffet = p.getDateEffet();
        d.description = p.getDescription();
        return d;
    }

    public Double getPrix() { return prix; }
    public Double getCharges() { return charges; }
    public String getPeriodicite() { return periodicite; }
    public LocalDate getDateEffet() { return dateEffet; }
    public String getDescription() { return description; }
}
