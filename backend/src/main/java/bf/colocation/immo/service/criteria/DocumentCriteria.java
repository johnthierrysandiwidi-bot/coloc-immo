package bf.colocation.immo.service.criteria;

import bf.colocation.immo.domain.enumeration.StatutValidation;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link bf.colocation.immo.domain.Document} entity. This class is used
 * in {@link bf.colocation.immo.web.rest.DocumentResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /documents?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DocumentCriteria implements Serializable, Criteria {

    /**
     * Class for filtering StatutValidation
     */
    public static class StatutValidationFilter extends Filter<StatutValidation> {

        public StatutValidationFilter() {}

        public StatutValidationFilter(StatutValidationFilter filter) {
            super(filter);
        }

        @Override
        public StatutValidationFilter copy() {
            return new StatutValidationFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nom;

    private StringFilter url;

    private StatutValidationFilter statut;

    private StringFilter motifRefus;

    private InstantFilter dateAjout;

    private InstantFilter dateTraitement;

    private LongFilter typeDocumentId;

    private LongFilter demarcheurId;

    private LongFilter traiteParId;

    private Boolean distinct;

    public DocumentCriteria() {}

    public DocumentCriteria(DocumentCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nom = other.optionalNom().map(StringFilter::copy).orElse(null);
        this.url = other.optionalUrl().map(StringFilter::copy).orElse(null);
        this.statut = other.optionalStatut().map(StatutValidationFilter::copy).orElse(null);
        this.motifRefus = other.optionalMotifRefus().map(StringFilter::copy).orElse(null);
        this.dateAjout = other.optionalDateAjout().map(InstantFilter::copy).orElse(null);
        this.dateTraitement = other.optionalDateTraitement().map(InstantFilter::copy).orElse(null);
        this.typeDocumentId = other.optionalTypeDocumentId().map(LongFilter::copy).orElse(null);
        this.demarcheurId = other.optionalDemarcheurId().map(LongFilter::copy).orElse(null);
        this.traiteParId = other.optionalTraiteParId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public DocumentCriteria copy() {
        return new DocumentCriteria(this);
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

    public StringFilter getNom() {
        return nom;
    }

    public Optional<StringFilter> optionalNom() {
        return Optional.ofNullable(nom);
    }

    public StringFilter nom() {
        if (nom == null) {
            setNom(new StringFilter());
        }
        return nom;
    }

    public void setNom(StringFilter nom) {
        this.nom = nom;
    }

    public StringFilter getUrl() {
        return url;
    }

    public Optional<StringFilter> optionalUrl() {
        return Optional.ofNullable(url);
    }

    public StringFilter url() {
        if (url == null) {
            setUrl(new StringFilter());
        }
        return url;
    }

    public void setUrl(StringFilter url) {
        this.url = url;
    }

    public StatutValidationFilter getStatut() {
        return statut;
    }

    public Optional<StatutValidationFilter> optionalStatut() {
        return Optional.ofNullable(statut);
    }

    public StatutValidationFilter statut() {
        if (statut == null) {
            setStatut(new StatutValidationFilter());
        }
        return statut;
    }

    public void setStatut(StatutValidationFilter statut) {
        this.statut = statut;
    }

    public StringFilter getMotifRefus() {
        return motifRefus;
    }

    public Optional<StringFilter> optionalMotifRefus() {
        return Optional.ofNullable(motifRefus);
    }

    public StringFilter motifRefus() {
        if (motifRefus == null) {
            setMotifRefus(new StringFilter());
        }
        return motifRefus;
    }

    public void setMotifRefus(StringFilter motifRefus) {
        this.motifRefus = motifRefus;
    }

    public InstantFilter getDateAjout() {
        return dateAjout;
    }

    public Optional<InstantFilter> optionalDateAjout() {
        return Optional.ofNullable(dateAjout);
    }

    public InstantFilter dateAjout() {
        if (dateAjout == null) {
            setDateAjout(new InstantFilter());
        }
        return dateAjout;
    }

    public void setDateAjout(InstantFilter dateAjout) {
        this.dateAjout = dateAjout;
    }

    public InstantFilter getDateTraitement() {
        return dateTraitement;
    }

    public Optional<InstantFilter> optionalDateTraitement() {
        return Optional.ofNullable(dateTraitement);
    }

    public InstantFilter dateTraitement() {
        if (dateTraitement == null) {
            setDateTraitement(new InstantFilter());
        }
        return dateTraitement;
    }

    public void setDateTraitement(InstantFilter dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public LongFilter getTypeDocumentId() {
        return typeDocumentId;
    }

    public Optional<LongFilter> optionalTypeDocumentId() {
        return Optional.ofNullable(typeDocumentId);
    }

    public LongFilter typeDocumentId() {
        if (typeDocumentId == null) {
            setTypeDocumentId(new LongFilter());
        }
        return typeDocumentId;
    }

    public void setTypeDocumentId(LongFilter typeDocumentId) {
        this.typeDocumentId = typeDocumentId;
    }

    public LongFilter getDemarcheurId() {
        return demarcheurId;
    }

    public Optional<LongFilter> optionalDemarcheurId() {
        return Optional.ofNullable(demarcheurId);
    }

    public LongFilter demarcheurId() {
        if (demarcheurId == null) {
            setDemarcheurId(new LongFilter());
        }
        return demarcheurId;
    }

    public void setDemarcheurId(LongFilter demarcheurId) {
        this.demarcheurId = demarcheurId;
    }

    public LongFilter getTraiteParId() {
        return traiteParId;
    }

    public Optional<LongFilter> optionalTraiteParId() {
        return Optional.ofNullable(traiteParId);
    }

    public LongFilter traiteParId() {
        if (traiteParId == null) {
            setTraiteParId(new LongFilter());
        }
        return traiteParId;
    }

    public void setTraiteParId(LongFilter traiteParId) {
        this.traiteParId = traiteParId;
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
        final DocumentCriteria that = (DocumentCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nom, that.nom) &&
            Objects.equals(url, that.url) &&
            Objects.equals(statut, that.statut) &&
            Objects.equals(motifRefus, that.motifRefus) &&
            Objects.equals(dateAjout, that.dateAjout) &&
            Objects.equals(dateTraitement, that.dateTraitement) &&
            Objects.equals(typeDocumentId, that.typeDocumentId) &&
            Objects.equals(demarcheurId, that.demarcheurId) &&
            Objects.equals(traiteParId, that.traiteParId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            nom,
            url,
            statut,
            motifRefus,
            dateAjout,
            dateTraitement,
            typeDocumentId,
            demarcheurId,
            traiteParId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DocumentCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNom().map(f -> "nom=" + f + ", ").orElse("") +
            optionalUrl().map(f -> "url=" + f + ", ").orElse("") +
            optionalStatut().map(f -> "statut=" + f + ", ").orElse("") +
            optionalMotifRefus().map(f -> "motifRefus=" + f + ", ").orElse("") +
            optionalDateAjout().map(f -> "dateAjout=" + f + ", ").orElse("") +
            optionalDateTraitement().map(f -> "dateTraitement=" + f + ", ").orElse("") +
            optionalTypeDocumentId().map(f -> "typeDocumentId=" + f + ", ").orElse("") +
            optionalDemarcheurId().map(f -> "demarcheurId=" + f + ", ").orElse("") +
            optionalTraiteParId().map(f -> "traiteParId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
