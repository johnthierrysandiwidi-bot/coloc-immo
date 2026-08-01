package bf.colocation.immo.service.criteria;

import bf.colocation.immo.domain.enumeration.TypeNotification;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link bf.colocation.immo.domain.Notification} entity. This class is used
 * in {@link bf.colocation.immo.web.rest.NotificationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /notifications?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class NotificationCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TypeNotification
     */
    public static class TypeNotificationFilter extends Filter<TypeNotification> {

        public TypeNotificationFilter() {}

        public TypeNotificationFilter(TypeNotificationFilter filter) {
            super(filter);
        }

        @Override
        public TypeNotificationFilter copy() {
            return new TypeNotificationFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private TypeNotificationFilter type;

    private StringFilter titre;

    private StringFilter message;

    private StringFilter lien;

    private BooleanFilter lue;

    private InstantFilter dateCreation;

    private LongFilter destinataireId;

    private Boolean distinct;

    public NotificationCriteria() {}

    public NotificationCriteria(NotificationCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.type = other.optionalType().map(TypeNotificationFilter::copy).orElse(null);
        this.titre = other.optionalTitre().map(StringFilter::copy).orElse(null);
        this.message = other.optionalMessage().map(StringFilter::copy).orElse(null);
        this.lien = other.optionalLien().map(StringFilter::copy).orElse(null);
        this.lue = other.optionalLue().map(BooleanFilter::copy).orElse(null);
        this.dateCreation = other.optionalDateCreation().map(InstantFilter::copy).orElse(null);
        this.destinataireId = other.optionalDestinataireId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public NotificationCriteria copy() {
        return new NotificationCriteria(this);
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

    public TypeNotificationFilter getType() {
        return type;
    }

    public Optional<TypeNotificationFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public TypeNotificationFilter type() {
        if (type == null) {
            setType(new TypeNotificationFilter());
        }
        return type;
    }

    public void setType(TypeNotificationFilter type) {
        this.type = type;
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

    public StringFilter getMessage() {
        return message;
    }

    public Optional<StringFilter> optionalMessage() {
        return Optional.ofNullable(message);
    }

    public StringFilter message() {
        if (message == null) {
            setMessage(new StringFilter());
        }
        return message;
    }

    public void setMessage(StringFilter message) {
        this.message = message;
    }

    public StringFilter getLien() {
        return lien;
    }

    public Optional<StringFilter> optionalLien() {
        return Optional.ofNullable(lien);
    }

    public StringFilter lien() {
        if (lien == null) {
            setLien(new StringFilter());
        }
        return lien;
    }

    public void setLien(StringFilter lien) {
        this.lien = lien;
    }

    public BooleanFilter getLue() {
        return lue;
    }

    public Optional<BooleanFilter> optionalLue() {
        return Optional.ofNullable(lue);
    }

    public BooleanFilter lue() {
        if (lue == null) {
            setLue(new BooleanFilter());
        }
        return lue;
    }

    public void setLue(BooleanFilter lue) {
        this.lue = lue;
    }

    public InstantFilter getDateCreation() {
        return dateCreation;
    }

    public Optional<InstantFilter> optionalDateCreation() {
        return Optional.ofNullable(dateCreation);
    }

    public InstantFilter dateCreation() {
        if (dateCreation == null) {
            setDateCreation(new InstantFilter());
        }
        return dateCreation;
    }

    public void setDateCreation(InstantFilter dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LongFilter getDestinataireId() {
        return destinataireId;
    }

    public Optional<LongFilter> optionalDestinataireId() {
        return Optional.ofNullable(destinataireId);
    }

    public LongFilter destinataireId() {
        if (destinataireId == null) {
            setDestinataireId(new LongFilter());
        }
        return destinataireId;
    }

    public void setDestinataireId(LongFilter destinataireId) {
        this.destinataireId = destinataireId;
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
        final NotificationCriteria that = (NotificationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(titre, that.titre) &&
            Objects.equals(message, that.message) &&
            Objects.equals(lien, that.lien) &&
            Objects.equals(lue, that.lue) &&
            Objects.equals(dateCreation, that.dateCreation) &&
            Objects.equals(destinataireId, that.destinataireId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, titre, message, lien, lue, dateCreation, destinataireId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "NotificationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalTitre().map(f -> "titre=" + f + ", ").orElse("") +
            optionalMessage().map(f -> "message=" + f + ", ").orElse("") +
            optionalLien().map(f -> "lien=" + f + ", ").orElse("") +
            optionalLue().map(f -> "lue=" + f + ", ").orElse("") +
            optionalDateCreation().map(f -> "dateCreation=" + f + ", ").orElse("") +
            optionalDestinataireId().map(f -> "destinataireId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
