package bf.colocation.immo.service.criteria;

import bf.colocation.immo.domain.enumeration.FrequenceAlerte;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link bf.colocation.immo.domain.Alerte} entity. This class is used
 * in {@link bf.colocation.immo.web.rest.AlerteResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /alertes?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlerteCriteria implements Serializable, Criteria {

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
     * Class for filtering FrequenceAlerte
     */
    public static class FrequenceAlerteFilter extends Filter<FrequenceAlerte> {

        public FrequenceAlerteFilter() {}

        public FrequenceAlerteFilter(FrequenceAlerteFilter filter) {
            super(filter);
        }

        @Override
        public FrequenceAlerteFilter copy() {
            return new FrequenceAlerteFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter titre;

    private StringFilter contenu;

    private TypeAnnonceFilter typeAnnonce;

    private DoubleFilter prixMin;

    private DoubleFilter prixMax;

    private DoubleFilter surfaceMin;

    private IntegerFilter nombreChambresMin;

    private BooleanFilter meubleUniquement;

    private BooleanFilter active;

    private FrequenceAlerteFilter frequence;

    private InstantFilter derniereExecution;

    private LongFilter titulaireId;

    private LongFilter localiteId;

    private LongFilter quartierId;

    private LongFilter typeImmobilierId;

    private LongFilter notifieesId;

    private Boolean distinct;

    public AlerteCriteria() {}

    public AlerteCriteria(AlerteCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.titre = other.optionalTitre().map(StringFilter::copy).orElse(null);
        this.contenu = other.optionalContenu().map(StringFilter::copy).orElse(null);
        this.typeAnnonce = other.optionalTypeAnnonce().map(TypeAnnonceFilter::copy).orElse(null);
        this.prixMin = other.optionalPrixMin().map(DoubleFilter::copy).orElse(null);
        this.prixMax = other.optionalPrixMax().map(DoubleFilter::copy).orElse(null);
        this.surfaceMin = other.optionalSurfaceMin().map(DoubleFilter::copy).orElse(null);
        this.nombreChambresMin = other.optionalNombreChambresMin().map(IntegerFilter::copy).orElse(null);
        this.meubleUniquement = other.optionalMeubleUniquement().map(BooleanFilter::copy).orElse(null);
        this.active = other.optionalActive().map(BooleanFilter::copy).orElse(null);
        this.frequence = other.optionalFrequence().map(FrequenceAlerteFilter::copy).orElse(null);
        this.derniereExecution = other.optionalDerniereExecution().map(InstantFilter::copy).orElse(null);
        this.titulaireId = other.optionalTitulaireId().map(LongFilter::copy).orElse(null);
        this.localiteId = other.optionalLocaliteId().map(LongFilter::copy).orElse(null);
        this.quartierId = other.optionalQuartierId().map(LongFilter::copy).orElse(null);
        this.typeImmobilierId = other.optionalTypeImmobilierId().map(LongFilter::copy).orElse(null);
        this.notifieesId = other.optionalNotifieesId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AlerteCriteria copy() {
        return new AlerteCriteria(this);
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

    public TypeAnnonceFilter getTypeAnnonce() {
        return typeAnnonce;
    }

    public Optional<TypeAnnonceFilter> optionalTypeAnnonce() {
        return Optional.ofNullable(typeAnnonce);
    }

    public TypeAnnonceFilter typeAnnonce() {
        if (typeAnnonce == null) {
            setTypeAnnonce(new TypeAnnonceFilter());
        }
        return typeAnnonce;
    }

    public void setTypeAnnonce(TypeAnnonceFilter typeAnnonce) {
        this.typeAnnonce = typeAnnonce;
    }

    public DoubleFilter getPrixMin() {
        return prixMin;
    }

    public Optional<DoubleFilter> optionalPrixMin() {
        return Optional.ofNullable(prixMin);
    }

    public DoubleFilter prixMin() {
        if (prixMin == null) {
            setPrixMin(new DoubleFilter());
        }
        return prixMin;
    }

    public void setPrixMin(DoubleFilter prixMin) {
        this.prixMin = prixMin;
    }

    public DoubleFilter getPrixMax() {
        return prixMax;
    }

    public Optional<DoubleFilter> optionalPrixMax() {
        return Optional.ofNullable(prixMax);
    }

    public DoubleFilter prixMax() {
        if (prixMax == null) {
            setPrixMax(new DoubleFilter());
        }
        return prixMax;
    }

    public void setPrixMax(DoubleFilter prixMax) {
        this.prixMax = prixMax;
    }

    public DoubleFilter getSurfaceMin() {
        return surfaceMin;
    }

    public Optional<DoubleFilter> optionalSurfaceMin() {
        return Optional.ofNullable(surfaceMin);
    }

    public DoubleFilter surfaceMin() {
        if (surfaceMin == null) {
            setSurfaceMin(new DoubleFilter());
        }
        return surfaceMin;
    }

    public void setSurfaceMin(DoubleFilter surfaceMin) {
        this.surfaceMin = surfaceMin;
    }

    public IntegerFilter getNombreChambresMin() {
        return nombreChambresMin;
    }

    public Optional<IntegerFilter> optionalNombreChambresMin() {
        return Optional.ofNullable(nombreChambresMin);
    }

    public IntegerFilter nombreChambresMin() {
        if (nombreChambresMin == null) {
            setNombreChambresMin(new IntegerFilter());
        }
        return nombreChambresMin;
    }

    public void setNombreChambresMin(IntegerFilter nombreChambresMin) {
        this.nombreChambresMin = nombreChambresMin;
    }

    public BooleanFilter getMeubleUniquement() {
        return meubleUniquement;
    }

    public Optional<BooleanFilter> optionalMeubleUniquement() {
        return Optional.ofNullable(meubleUniquement);
    }

    public BooleanFilter meubleUniquement() {
        if (meubleUniquement == null) {
            setMeubleUniquement(new BooleanFilter());
        }
        return meubleUniquement;
    }

    public void setMeubleUniquement(BooleanFilter meubleUniquement) {
        this.meubleUniquement = meubleUniquement;
    }

    public BooleanFilter getActive() {
        return active;
    }

    public Optional<BooleanFilter> optionalActive() {
        return Optional.ofNullable(active);
    }

    public BooleanFilter active() {
        if (active == null) {
            setActive(new BooleanFilter());
        }
        return active;
    }

    public void setActive(BooleanFilter active) {
        this.active = active;
    }

    public FrequenceAlerteFilter getFrequence() {
        return frequence;
    }

    public Optional<FrequenceAlerteFilter> optionalFrequence() {
        return Optional.ofNullable(frequence);
    }

    public FrequenceAlerteFilter frequence() {
        if (frequence == null) {
            setFrequence(new FrequenceAlerteFilter());
        }
        return frequence;
    }

    public void setFrequence(FrequenceAlerteFilter frequence) {
        this.frequence = frequence;
    }

    public InstantFilter getDerniereExecution() {
        return derniereExecution;
    }

    public Optional<InstantFilter> optionalDerniereExecution() {
        return Optional.ofNullable(derniereExecution);
    }

    public InstantFilter derniereExecution() {
        if (derniereExecution == null) {
            setDerniereExecution(new InstantFilter());
        }
        return derniereExecution;
    }

    public void setDerniereExecution(InstantFilter derniereExecution) {
        this.derniereExecution = derniereExecution;
    }

    public LongFilter getTitulaireId() {
        return titulaireId;
    }

    public Optional<LongFilter> optionalTitulaireId() {
        return Optional.ofNullable(titulaireId);
    }

    public LongFilter titulaireId() {
        if (titulaireId == null) {
            setTitulaireId(new LongFilter());
        }
        return titulaireId;
    }

    public void setTitulaireId(LongFilter titulaireId) {
        this.titulaireId = titulaireId;
    }

    public LongFilter getLocaliteId() {
        return localiteId;
    }

    public Optional<LongFilter> optionalLocaliteId() {
        return Optional.ofNullable(localiteId);
    }

    public LongFilter localiteId() {
        if (localiteId == null) {
            setLocaliteId(new LongFilter());
        }
        return localiteId;
    }

    public void setLocaliteId(LongFilter localiteId) {
        this.localiteId = localiteId;
    }

    public LongFilter getQuartierId() {
        return quartierId;
    }

    public Optional<LongFilter> optionalQuartierId() {
        return Optional.ofNullable(quartierId);
    }

    public LongFilter quartierId() {
        if (quartierId == null) {
            setQuartierId(new LongFilter());
        }
        return quartierId;
    }

    public void setQuartierId(LongFilter quartierId) {
        this.quartierId = quartierId;
    }

    public LongFilter getTypeImmobilierId() {
        return typeImmobilierId;
    }

    public Optional<LongFilter> optionalTypeImmobilierId() {
        return Optional.ofNullable(typeImmobilierId);
    }

    public LongFilter typeImmobilierId() {
        if (typeImmobilierId == null) {
            setTypeImmobilierId(new LongFilter());
        }
        return typeImmobilierId;
    }

    public void setTypeImmobilierId(LongFilter typeImmobilierId) {
        this.typeImmobilierId = typeImmobilierId;
    }

    public LongFilter getNotifieesId() {
        return notifieesId;
    }

    public Optional<LongFilter> optionalNotifieesId() {
        return Optional.ofNullable(notifieesId);
    }

    public LongFilter notifieesId() {
        if (notifieesId == null) {
            setNotifieesId(new LongFilter());
        }
        return notifieesId;
    }

    public void setNotifieesId(LongFilter notifieesId) {
        this.notifieesId = notifieesId;
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
        final AlerteCriteria that = (AlerteCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(titre, that.titre) &&
            Objects.equals(contenu, that.contenu) &&
            Objects.equals(typeAnnonce, that.typeAnnonce) &&
            Objects.equals(prixMin, that.prixMin) &&
            Objects.equals(prixMax, that.prixMax) &&
            Objects.equals(surfaceMin, that.surfaceMin) &&
            Objects.equals(nombreChambresMin, that.nombreChambresMin) &&
            Objects.equals(meubleUniquement, that.meubleUniquement) &&
            Objects.equals(active, that.active) &&
            Objects.equals(frequence, that.frequence) &&
            Objects.equals(derniereExecution, that.derniereExecution) &&
            Objects.equals(titulaireId, that.titulaireId) &&
            Objects.equals(localiteId, that.localiteId) &&
            Objects.equals(quartierId, that.quartierId) &&
            Objects.equals(typeImmobilierId, that.typeImmobilierId) &&
            Objects.equals(notifieesId, that.notifieesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            titre,
            contenu,
            typeAnnonce,
            prixMin,
            prixMax,
            surfaceMin,
            nombreChambresMin,
            meubleUniquement,
            active,
            frequence,
            derniereExecution,
            titulaireId,
            localiteId,
            quartierId,
            typeImmobilierId,
            notifieesId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlerteCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitre().map(f -> "titre=" + f + ", ").orElse("") +
            optionalContenu().map(f -> "contenu=" + f + ", ").orElse("") +
            optionalTypeAnnonce().map(f -> "typeAnnonce=" + f + ", ").orElse("") +
            optionalPrixMin().map(f -> "prixMin=" + f + ", ").orElse("") +
            optionalPrixMax().map(f -> "prixMax=" + f + ", ").orElse("") +
            optionalSurfaceMin().map(f -> "surfaceMin=" + f + ", ").orElse("") +
            optionalNombreChambresMin().map(f -> "nombreChambresMin=" + f + ", ").orElse("") +
            optionalMeubleUniquement().map(f -> "meubleUniquement=" + f + ", ").orElse("") +
            optionalActive().map(f -> "active=" + f + ", ").orElse("") +
            optionalFrequence().map(f -> "frequence=" + f + ", ").orElse("") +
            optionalDerniereExecution().map(f -> "derniereExecution=" + f + ", ").orElse("") +
            optionalTitulaireId().map(f -> "titulaireId=" + f + ", ").orElse("") +
            optionalLocaliteId().map(f -> "localiteId=" + f + ", ").orElse("") +
            optionalQuartierId().map(f -> "quartierId=" + f + ", ").orElse("") +
            optionalTypeImmobilierId().map(f -> "typeImmobilierId=" + f + ", ").orElse("") +
            optionalNotifieesId().map(f -> "notifieesId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
