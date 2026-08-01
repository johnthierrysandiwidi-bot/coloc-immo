package bf.colocation.immo.service.criteria;

import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link bf.colocation.immo.domain.Annonce} entity. This class is used
 * in {@link bf.colocation.immo.web.rest.AnnonceResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /annonces?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AnnonceCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TypeAnnonce
     */
    public static class TypeAnnonceFilter extends Filter<TypeAnnonce> {

        public TypeAnnonceFilter() {}

        public TypeAnnonceFilter(TypeAnnonceFilter filter) {
            super(filter);
        }

        @Override
        public TypeAnnonceFilter copy() {
            return new TypeAnnonceFilter(this);
        }
    }

    /**
     * Class for filtering StatutAnnonce
     */
    public static class StatutAnnonceFilter extends Filter<StatutAnnonce> {

        public StatutAnnonceFilter() {}

        public StatutAnnonceFilter(StatutAnnonceFilter filter) {
            super(filter);
        }

        @Override
        public StatutAnnonceFilter copy() {
            return new StatutAnnonceFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter titre;

    private StringFilter contenu;

    private TypeAnnonceFilter type;

    private DoubleFilter prix;

    private IntegerFilter nombreVues;

    private InstantFilter datePublication;

    private InstantFilter dateExpiration;

    private StatutAnnonceFilter statut;

    private LongFilter immobilierId;

    private LongFilter auteurId;

    private LongFilter detailColocationId;

    private LongFilter vuesId;

    private LongFilter rendezVousId;

    private LongFilter favorisId;

    private Boolean distinct;

    public AnnonceCriteria() {}

    public AnnonceCriteria(AnnonceCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.titre = other.optionalTitre().map(StringFilter::copy).orElse(null);
        this.contenu = other.optionalContenu().map(StringFilter::copy).orElse(null);
        this.type = other.optionalType().map(TypeAnnonceFilter::copy).orElse(null);
        this.prix = other.optionalPrix().map(DoubleFilter::copy).orElse(null);
        this.nombreVues = other.optionalNombreVues().map(IntegerFilter::copy).orElse(null);
        this.datePublication = other.optionalDatePublication().map(InstantFilter::copy).orElse(null);
        this.dateExpiration = other.optionalDateExpiration().map(InstantFilter::copy).orElse(null);
        this.statut = other.optionalStatut().map(StatutAnnonceFilter::copy).orElse(null);
        this.immobilierId = other.optionalImmobilierId().map(LongFilter::copy).orElse(null);
        this.auteurId = other.optionalAuteurId().map(LongFilter::copy).orElse(null);
        this.detailColocationId = other.optionalDetailColocationId().map(LongFilter::copy).orElse(null);
        this.vuesId = other.optionalVuesId().map(LongFilter::copy).orElse(null);
        this.rendezVousId = other.optionalRendezVousId().map(LongFilter::copy).orElse(null);
        this.favorisId = other.optionalFavorisId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AnnonceCriteria copy() {
        return new AnnonceCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getTitre() {
        return titre;
    }

    public Optional<StringFilter> optionalTitre() {
        return Optional.ofNullable(titre);
    }

    public StringFilter titre() {
        if (titre == null) {
            setTitre(new StringFilter());
        }
        return titre;
    }

    public void setTitre(StringFilter titre) {
        this.titre = titre;
    }

    public StringFilter getContenu() {
        return contenu;
    }

    public Optional<StringFilter> optionalContenu() {
        return Optional.ofNullable(contenu);
    }

    public StringFilter contenu() {
        if (contenu == null) {
            setContenu(new StringFilter());
        }
        return contenu;
    }

    public void setContenu(StringFilter contenu) {
        this.contenu = contenu;
    }

    public TypeAnnonceFilter getType() {
        return type;
    }

    public Optional<TypeAnnonceFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public TypeAnnonceFilter type() {
        if (type == null) {
            setType(new TypeAnnonceFilter());
        }
        return type;
    }

    public void setType(TypeAnnonceFilter type) {
        this.type = type;
    }

    public DoubleFilter getPrix() {
        return prix;
    }

    public Optional<DoubleFilter> optionalPrix() {
        return Optional.ofNullable(prix);
    }

    public DoubleFilter prix() {
        if (prix == null) {
            setPrix(new DoubleFilter());
        }
        return prix;
    }

    public void setPrix(DoubleFilter prix) {
        this.prix = prix;
    }

    public IntegerFilter getNombreVues() {
        return nombreVues;
    }

    public Optional<IntegerFilter> optionalNombreVues() {
        return Optional.ofNullable(nombreVues);
    }

    public IntegerFilter nombreVues() {
        if (nombreVues == null) {
            setNombreVues(new IntegerFilter());
        }
        return nombreVues;
    }

    public void setNombreVues(IntegerFilter nombreVues) {
        this.nombreVues = nombreVues;
    }

    public InstantFilter getDatePublication() {
        return datePublication;
    }

    public Optional<InstantFilter> optionalDatePublication() {
        return Optional.ofNullable(datePublication);
    }

    public InstantFilter datePublication() {
        if (datePublication == null) {
            setDatePublication(new InstantFilter());
        }
        return datePublication;
    }

    public void setDatePublication(InstantFilter datePublication) {
        this.datePublication = datePublication;
    }

    public InstantFilter getDateExpiration() {
        return dateExpiration;
    }

    public Optional<InstantFilter> optionalDateExpiration() {
        return Optional.ofNullable(dateExpiration);
    }

    public InstantFilter dateExpiration() {
        if (dateExpiration == null) {
            setDateExpiration(new InstantFilter());
        }
        return dateExpiration;
    }

    public void setDateExpiration(InstantFilter dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public StatutAnnonceFilter getStatut() {
        return statut;
    }

    public Optional<StatutAnnonceFilter> optionalStatut() {
        return Optional.ofNullable(statut);
    }

    public StatutAnnonceFilter statut() {
        if (statut == null) {
            setStatut(new StatutAnnonceFilter());
        }
        return statut;
    }

    public void setStatut(StatutAnnonceFilter statut) {
        this.statut = statut;
    }

    public LongFilter getImmobilierId() {
        return immobilierId;
    }

    public Optional<LongFilter> optionalImmobilierId() {
        return Optional.ofNullable(immobilierId);
    }

    public LongFilter immobilierId() {
        if (immobilierId == null) {
            setImmobilierId(new LongFilter());
        }
        return immobilierId;
    }

    public void setImmobilierId(LongFilter immobilierId) {
        this.immobilierId = immobilierId;
    }

    public LongFilter getAuteurId() {
        return auteurId;
    }

    public Optional<LongFilter> optionalAuteurId() {
        return Optional.ofNullable(auteurId);
    }

    public LongFilter auteurId() {
        if (auteurId == null) {
            setAuteurId(new LongFilter());
        }
        return auteurId;
    }

    public void setAuteurId(LongFilter auteurId) {
        this.auteurId = auteurId;
    }

    public LongFilter getDetailColocationId() {
        return detailColocationId;
    }

    public Optional<LongFilter> optionalDetailColocationId() {
        return Optional.ofNullable(detailColocationId);
    }

    public LongFilter detailColocationId() {
        if (detailColocationId == null) {
            setDetailColocationId(new LongFilter());
        }
        return detailColocationId;
    }

    public void setDetailColocationId(LongFilter detailColocationId) {
        this.detailColocationId = detailColocationId;
    }

    public LongFilter getVuesId() {
        return vuesId;
    }

    public Optional<LongFilter> optionalVuesId() {
        return Optional.ofNullable(vuesId);
    }

    public LongFilter vuesId() {
        if (vuesId == null) {
            setVuesId(new LongFilter());
        }
        return vuesId;
    }

    public void setVuesId(LongFilter vuesId) {
        this.vuesId = vuesId;
    }

    public LongFilter getRendezVousId() {
        return rendezVousId;
    }

    public Optional<LongFilter> optionalRendezVousId() {
        return Optional.ofNullable(rendezVousId);
    }

    public LongFilter rendezVousId() {
        if (rendezVousId == null) {
            setRendezVousId(new LongFilter());
        }
        return rendezVousId;
    }

    public void setRendezVousId(LongFilter rendezVousId) {
        this.rendezVousId = rendezVousId;
    }

    public LongFilter getFavorisId() {
        return favorisId;
    }

    public Optional<LongFilter> optionalFavorisId() {
        return Optional.ofNullable(favorisId);
    }

    public LongFilter favorisId() {
        if (favorisId == null) {
            setFavorisId(new LongFilter());
        }
        return favorisId;
    }

    public void setFavorisId(LongFilter favorisId) {
        this.favorisId = favorisId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AnnonceCriteria that = (AnnonceCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(titre, that.titre) &&
            Objects.equals(contenu, that.contenu) &&
            Objects.equals(type, that.type) &&
            Objects.equals(prix, that.prix) &&
            Objects.equals(nombreVues, that.nombreVues) &&
            Objects.equals(datePublication, that.datePublication) &&
            Objects.equals(dateExpiration, that.dateExpiration) &&
            Objects.equals(statut, that.statut) &&
            Objects.equals(immobilierId, that.immobilierId) &&
            Objects.equals(auteurId, that.auteurId) &&
            Objects.equals(detailColocationId, that.detailColocationId) &&
            Objects.equals(vuesId, that.vuesId) &&
            Objects.equals(rendezVousId, that.rendezVousId) &&
            Objects.equals(favorisId, that.favorisId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            titre,
            contenu,
            type,
            prix,
            nombreVues,
            datePublication,
            dateExpiration,
            statut,
            immobilierId,
            auteurId,
            detailColocationId,
            vuesId,
            rendezVousId,
            favorisId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AnnonceCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitre().map(f -> "titre=" + f + ", ").orElse("") +
            optionalContenu().map(f -> "contenu=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalPrix().map(f -> "prix=" + f + ", ").orElse("") +
            optionalNombreVues().map(f -> "nombreVues=" + f + ", ").orElse("") +
            optionalDatePublication().map(f -> "datePublication=" + f + ", ").orElse("") +
            optionalDateExpiration().map(f -> "dateExpiration=" + f + ", ").orElse("") +
            optionalStatut().map(f -> "statut=" + f + ", ").orElse("") +
            optionalImmobilierId().map(f -> "immobilierId=" + f + ", ").orElse("") +
            optionalAuteurId().map(f -> "auteurId=" + f + ", ").orElse("") +
            optionalDetailColocationId().map(f -> "detailColocationId=" + f + ", ").orElse("") +
            optionalVuesId().map(f -> "vuesId=" + f + ", ").orElse("") +
            optionalRendezVousId().map(f -> "rendezVousId=" + f + ", ").orElse("") +
            optionalFavorisId().map(f -> "favorisId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
