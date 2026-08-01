package bf.colocation.immo.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link bf.colocation.immo.domain.Favori} entity. This class is used
 * in {@link bf.colocation.immo.web.rest.FavoriResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /favoris?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FavoriCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter dateAjout;

    private LongFilter annonceId;

    private LongFilter utilisateurId;

    private Boolean distinct;

    public FavoriCriteria() {}

    public FavoriCriteria(FavoriCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.dateAjout = other.optionalDateAjout().map(InstantFilter::copy).orElse(null);
        this.annonceId = other.optionalAnnonceId().map(LongFilter::copy).orElse(null);
        this.utilisateurId = other.optionalUtilisateurId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public FavoriCriteria copy() {
        return new FavoriCriteria(this);
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

    public LongFilter getAnnonceId() {
        return annonceId;
    }

    public Optional<LongFilter> optionalAnnonceId() {
        return Optional.ofNullable(annonceId);
    }

    public LongFilter annonceId() {
        if (annonceId == null) {
            setAnnonceId(new LongFilter());
        }
        return annonceId;
    }

    public void setAnnonceId(LongFilter annonceId) {
        this.annonceId = annonceId;
    }

    public LongFilter getUtilisateurId() {
        return utilisateurId;
    }

    public Optional<LongFilter> optionalUtilisateurId() {
        return Optional.ofNullable(utilisateurId);
    }

    public LongFilter utilisateurId() {
        if (utilisateurId == null) {
            setUtilisateurId(new LongFilter());
        }
        return utilisateurId;
    }

    public void setUtilisateurId(LongFilter utilisateurId) {
        this.utilisateurId = utilisateurId;
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
        final FavoriCriteria that = (FavoriCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(dateAjout, that.dateAjout) &&
            Objects.equals(annonceId, that.annonceId) &&
            Objects.equals(utilisateurId, that.utilisateurId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateAjout, annonceId, utilisateurId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FavoriCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDateAjout().map(f -> "dateAjout=" + f + ", ").orElse("") +
            optionalAnnonceId().map(f -> "annonceId=" + f + ", ").orElse("") +
            optionalUtilisateurId().map(f -> "utilisateurId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
