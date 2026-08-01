package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.StatutValidation;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Document} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DocumentDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 150)
    private String nom;

    @NotNull
    @Size(max = 255)
    private String url;

    @NotNull
    private StatutValidation statut;

    @Size(max = 500)
    private String motifRefus;

    private Instant dateAjout;

    private Instant dateTraitement;

    @NotNull
    private TypeDocumentDTO typeDocument;

    @NotNull
    private UserDTO demarcheur;

    private UserDTO traitePar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public StatutValidation getStatut() {
        return statut;
    }

    public void setStatut(StatutValidation statut) {
        this.statut = statut;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public Instant getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(Instant dateAjout) {
        this.dateAjout = dateAjout;
    }

    public Instant getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(Instant dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public TypeDocumentDTO getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(TypeDocumentDTO typeDocument) {
        this.typeDocument = typeDocument;
    }

    public UserDTO getDemarcheur() {
        return demarcheur;
    }

    public void setDemarcheur(UserDTO demarcheur) {
        this.demarcheur = demarcheur;
    }

    public UserDTO getTraitePar() {
        return traitePar;
    }

    public void setTraitePar(UserDTO traitePar) {
        this.traitePar = traitePar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentDTO)) {
            return false;
        }

        DocumentDTO documentDTO = (DocumentDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, documentDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DocumentDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", url='" + getUrl() + "'" +
            ", statut='" + getStatut() + "'" +
            ", motifRefus='" + getMotifRefus() + "'" +
            ", dateAjout='" + getDateAjout() + "'" +
            ", dateTraitement='" + getDateTraitement() + "'" +
            ", typeDocument=" + getTypeDocument() +
            ", demarcheur=" + getDemarcheur() +
            ", traitePar=" + getTraitePar() +
            "}";
    }
}
