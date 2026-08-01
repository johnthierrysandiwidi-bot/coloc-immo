package bf.colocation.immo.service.criteria;

import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link bf.colocation.immo.domain.RendezVous} entity. This class is used
 * in {@link bf.colocation.immo.web.rest.RendezVousResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /rendez-vous?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RendezVousCriteria implements Serializable, Criteria {

    /**
     * Class for filtering StatutRendezVous
     */
    public static class StatutRendezVousFilter extends Filter<StatutRendezVous> {

        public StatutRendezVousFilter() {}

        public StatutRendezVousFilter(StatutRendezVousFilter filter) {
            super(filter);
        }

        @Override
        public StatutRendezVousFilter copy() {
            return new StatutRendezVousFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter dateHeure;

    private InstantFilter dateReportee;

    private StringFilter lieu;

    private StringFilter contenu;

    private StringFilter motif;

    private StatutRendezVousFilter statut;

    private LongFilter annonceId;

    private LongFilter demandeurId;

    private Boolean distinct;

    public RendezVousCriteria() {}

    public RendezVousCriteria(RendezVousCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.dateHeure = other.optionalDateHeure().map(InstantFilter::copy).orElse(null);
        this.dateReportee = other.optionalDateReportee().map(InstantFilter::copy).orElse(null);
        this.lieu = other.optionalLieu().map(StringFilter::copy).orElse(null);
        this.contenu = other.optionalContenu().map(StringFilter::copy).orElse(null);
        this.motif = other.optionalMotif().map(StringFilter::copy).orElse(null);
        this.statut = other.optionalStatut().map(StatutRendezVousFilter::copy).orElse(null);
        this.annonceId = other.optionalAnnonceId().map(LongFilter::copy).orElse(null);
        this.demandeurId = other.optionalDemandeurId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public RendezVousCriteria copy() {
        return new RendezVousCriteria(this);
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

    public InstantFilter getDateHeure() {
        return dateHeure;
    }

    public Optional<InstantFilter> optionalDateHeure() {
        return Optional.ofNullable(dateHeure);
    }

    public InstantFilter dateHeure() {
        if (dateHeure == null) {
            setDateHeure(new InstantFilter());
        }
        return dateHeure;
    }

    public void setDateHeure(InstantFilter dateHeure) {
        this.dateHeure = dateHeure;
    }

    public InstantFilter getDateReportee() {
        return dateReportee;
    }

    public Optional<InstantFilter> optionalDateReportee() {
        return Optional.ofNullable(dateReportee);
    }

    public InstantFilter dateReportee() {
        if (dateReportee == null) {
            setDateReportee(new InstantFilter());
        }
        return dateReportee;
    }

    public void setDateReportee(InstantFilter dateReportee) {
        this.dateReportee = dateReportee;
    }

    public StringFilter getLieu() {
        return lieu;
    }

    public Optional<StringFilter> optionalLieu() {
        return Optional.ofNullable(lieu);
    }

    public StringFilter lieu() {
        if (lieu == null) {
            setLieu(new StringFilter());
        }
        return lieu;
    }

    public void setLieu(StringFilter lieu) {
        this.lieu = lieu;
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

    public StringFilter getMotif() {
        return motif;
    }

    public Optional<StringFilter> optionalMotif() {
        return Optional.ofNullable(motif);
    }

    public StringFilter motif() {
        if (motif == null) {
            setMotif(new StringFilter());
        }
        return motif;
    }

    public void setMotif(StringFilter motif) {
        this.motif = motif;
    }

    public StatutRendezVousFilter getStatut() {
        return statut;
    }

    public Optional<StatutRendezVousFilter> optionalStatut() {
        return Optional.ofNullable(statut);
    }

    public StatutRendezVousFilter statut() {
        if (statut == null) {
            setStatut(new StatutRendezVousFilter());
        }
        return statut;
    }

    public void setStatut(StatutRendezVousFilter statut) {
        this.statut = statut;
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

    public LongFilter getDemandeurId() {
        return demandeurId;
    }

    public Optional<LongFilter> optionalDemandeurId() {
        return Optional.ofNullable(demandeurId);
    }

    public LongFilter demandeurId() {
        if (demandeurId == null) {
            setDemandeurId(new LongFilter());
        }
        return demandeurId;
    }

    public void setDemandeurId(LongFilter demandeurId) {
        this.demandeurId = demandeurId;
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
        final RendezVousCriteria that = (RendezVousCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(dateHeure, that.dateHeure) &&
            Objects.equals(dateReportee, that.dateReportee) &&
            Objects.equals(lieu, that.lieu) &&
            Objects.equals(contenu, that.contenu) &&
            Objects.equals(motif, that.motif) &&
            Objects.equals(statut, that.statut) &&
            Objects.equals(annonceId, that.annonceId) &&
            Objects.equals(demandeurId, that.demandeurId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateHeure, dateReportee, lieu, contenu, motif, statut, annonceId, demandeurId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RendezVousCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDateHeure().map(f -> "dateHeure=" + f + ", ").orElse("") +
            optionalDateReportee().map(f -> "dateReportee=" + f + ", ").orElse("") +
            optionalLieu().map(f -> "lieu=" + f + ", ").orElse("") +
            optionalContenu().map(f -> "contenu=" + f + ", ").orElse("") +
            optionalMotif().map(f -> "motif=" + f + ", ").orElse("") +
            optionalStatut().map(f -> "statut=" + f + ", ").orElse("") +
            optionalAnnonceId().map(f -> "annonceId=" + f + ", ").orElse("") +
            optionalDemandeurId().map(f -> "demandeurId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
