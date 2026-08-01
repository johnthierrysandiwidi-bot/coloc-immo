package bf.colocation.immo.service.dto;

import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link bf.colocation.immo.domain.Annonce} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnnonceDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 150)
    private String titre;

    @Size(max = 5000)
    private String contenu;

    @NotNull
    private TypeAnnonce type;

    @NotNull
    @DecimalMin(value = "0")
    private Double prix;

    @Min(value = 0)
    private Integer nombreVues;

    private Instant datePublication;

    private Instant dateExpiration;

    @NotNull
    private StatutAnnonce statut;

    @NotNull
    private ImmobilierDTO immobilier;

    @NotNull
    private UserDTO auteur;

    /**
     * Photos du bien, remontées par PhotoAnnonceService.
     * JHipster ne mappe pas les relations « un-à-plusieurs » inverses :
     * sans ces champs, le front n'a aucune image à afficher.
     */
    private String photoUrl;

    private List<String> photos = new ArrayList<>();

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public TypeAnnonce getType() {
        return type;
    }

    public void setType(TypeAnnonce type) {
        this.type = type;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Integer getNombreVues() {
        return nombreVues;
    }

    public void setNombreVues(Integer nombreVues) {
        this.nombreVues = nombreVues;
    }

    public Instant getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(Instant datePublication) {
        this.datePublication = datePublication;
    }

    public Instant getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Instant dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public StatutAnnonce getStatut() {
        return statut;
    }

    public void setStatut(StatutAnnonce statut) {
        this.statut = statut;
    }

    public ImmobilierDTO getImmobilier() {
        return immobilier;
    }

    public void setImmobilier(ImmobilierDTO immobilier) {
        this.immobilier = immobilier;
    }

    public UserDTO getAuteur() {
        return auteur;
    }

    public void setAuteur(UserDTO auteur) {
        this.auteur = auteur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnnonceDTO)) {
            return false;
        }

        AnnonceDTO annonceDTO = (AnnonceDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, annonceDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnnonceDTO{" +
            "id=" + getId() +
            ", titre='" + getTitre() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", type='" + getType() + "'" +
            ", prix=" + getPrix() +
            ", nombreVues=" + getNombreVues() +
            ", datePublication='" + getDatePublication() + "'" +
            ", dateExpiration='" + getDateExpiration() + "'" +
            ", statut='" + getStatut() + "'" +
            ", immobilier=" + getImmobilier() +
            ", auteur=" + getAuteur() +
            "}";
    }
}
