package bf.colocation.immo.service.criteria;

import bf.colocation.immo.domain.enumeration.StatutBien;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link bf.colocation.immo.domain.Immobilier} entity. This class is used
 * in {@link bf.colocation.immo.web.rest.ImmobilierResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /immobiliers?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ImmobilierCriteria implements Serializable, Criteria {

    /**
     * Class for filtering StatutBien
     */
    public static class StatutBienFilter extends Filter<StatutBien> {

        public StatutBienFilter() {}

        public StatutBienFilter(StatutBienFilter filter) {
            super(filter);
        }

        @Override
        public StatutBienFilter copy() {
            return new StatutBienFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nom;

    private StringFilter description;

    private StringFilter adresse;

    private DoubleFilter surface;

    private IntegerFilter nombrePieces;

    private IntegerFilter nombreChambres;

    private IntegerFilter nombreSallesBain;

    private IntegerFilter nombreSalons;

    private BooleanFilter garage;

    private BooleanFilter piscine;

    private BooleanFilter meuble;

    private LocalDateFilter disponibleA;

    private StatutBienFilter statut;

    private DoubleFilter latitude;

    private DoubleFilter longitude;

    private InstantFilter dateCreation;

    private LongFilter prixId;

    private LongFilter imagesId;

    private LongFilter proprietaireId;

    private LongFilter demarcheurId;

    private LongFilter localiteId;

    private LongFilter quartierId;

    private LongFilter typeImmobilierId;

    private LongFilter annoncesId;

    private Boolean distinct;

    public ImmobilierCriteria() {}

    public ImmobilierCriteria(ImmobilierCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.nom = other.optionalNom().map(StringFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.adresse = other.optionalAdresse().map(StringFilter::copy).orElse(null);
        this.surface = other.optionalSurface().map(DoubleFilter::copy).orElse(null);
        this.nombrePieces = other.optionalNombrePieces().map(IntegerFilter::copy).orElse(null);
        this.nombreChambres = other.optionalNombreChambres().map(IntegerFilter::copy).orElse(null);
        this.nombreSallesBain = other.optionalNombreSallesBain().map(IntegerFilter::copy).orElse(null);
        this.nombreSalons = other.optionalNombreSalons().map(IntegerFilter::copy).orElse(null);
        this.garage = other.optionalGarage().map(BooleanFilter::copy).orElse(null);
        this.piscine = other.optionalPiscine().map(BooleanFilter::copy).orElse(null);
        this.meuble = other.optionalMeuble().map(BooleanFilter::copy).orElse(null);
        this.disponibleA = other.optionalDisponibleA().map(LocalDateFilter::copy).orElse(null);
        this.statut = other.optionalStatut().map(StatutBienFilter::copy).orElse(null);
        this.latitude = other.optionalLatitude().map(DoubleFilter::copy).orElse(null);
        this.longitude = other.optionalLongitude().map(DoubleFilter::copy).orElse(null);
        this.dateCreation = other.optionalDateCreation().map(InstantFilter::copy).orElse(null);
        this.prixId = other.optionalPrixId().map(LongFilter::copy).orElse(null);
        this.imagesId = other.optionalImagesId().map(LongFilter::copy).orElse(null);
        this.proprietaireId = other.optionalProprietaireId().map(LongFilter::copy).orElse(null);
        this.demarcheurId = other.optionalDemarcheurId().map(LongFilter::copy).orElse(null);
        this.localiteId = other.optionalLocaliteId().map(LongFilter::copy).orElse(null);
        this.quartierId = other.optionalQuartierId().map(LongFilter::copy).orElse(null);
        this.typeImmobilierId = other.optionalTypeImmobilierId().map(LongFilter::copy).orElse(null);
        this.annoncesId = other.optionalAnnoncesId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ImmobilierCriteria copy() {
        return new ImmobilierCriteria(this);
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

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public StringFilter getAdresse() {
        return adresse;
    }

    public Optional<StringFilter> optionalAdresse() {
        return Optional.ofNullable(adresse);
    }

    public StringFilter adresse() {
        if (adresse == null) {
            setAdresse(new StringFilter());
        }
        return adresse;
    }

    public void setAdresse(StringFilter adresse) {
        this.adresse = adresse;
    }

    public DoubleFilter getSurface() {
        return surface;
    }

    public Optional<DoubleFilter> optionalSurface() {
        return Optional.ofNullable(surface);
    }

    public DoubleFilter surface() {
        if (surface == null) {
            setSurface(new DoubleFilter());
        }
        return surface;
    }

    public void setSurface(DoubleFilter surface) {
        this.surface = surface;
    }

    public IntegerFilter getNombrePieces() {
        return nombrePieces;
    }

    public Optional<IntegerFilter> optionalNombrePieces() {
        return Optional.ofNullable(nombrePieces);
    }

    public IntegerFilter nombrePieces() {
        if (nombrePieces == null) {
            setNombrePieces(new IntegerFilter());
        }
        return nombrePieces;
    }

    public void setNombrePieces(IntegerFilter nombrePieces) {
        this.nombrePieces = nombrePieces;
    }

    public IntegerFilter getNombreChambres() {
        return nombreChambres;
    }

    public Optional<IntegerFilter> optionalNombreChambres() {
        return Optional.ofNullable(nombreChambres);
    }

    public IntegerFilter nombreChambres() {
        if (nombreChambres == null) {
            setNombreChambres(new IntegerFilter());
        }
        return nombreChambres;
    }

    public void setNombreChambres(IntegerFilter nombreChambres) {
        this.nombreChambres = nombreChambres;
    }

    public IntegerFilter getNombreSallesBain() {
        return nombreSallesBain;
    }

    public Optional<IntegerFilter> optionalNombreSallesBain() {
        return Optional.ofNullable(nombreSallesBain);
    }

    public IntegerFilter nombreSallesBain() {
        if (nombreSallesBain == null) {
            setNombreSallesBain(new IntegerFilter());
        }
        return nombreSallesBain;
    }

    public void setNombreSallesBain(IntegerFilter nombreSallesBain) {
        this.nombreSallesBain = nombreSallesBain;
    }

    public IntegerFilter getNombreSalons() {
        return nombreSalons;
    }

    public Optional<IntegerFilter> optionalNombreSalons() {
        return Optional.ofNullable(nombreSalons);
    }

    public IntegerFilter nombreSalons() {
        if (nombreSalons == null) {
            setNombreSalons(new IntegerFilter());
        }
        return nombreSalons;
    }

    public void setNombreSalons(IntegerFilter nombreSalons) {
        this.nombreSalons = nombreSalons;
    }

    public BooleanFilter getGarage() {
        return garage;
    }

    public Optional<BooleanFilter> optionalGarage() {
        return Optional.ofNullable(garage);
    }

    public BooleanFilter garage() {
        if (garage == null) {
            setGarage(new BooleanFilter());
        }
        return garage;
    }

    public void setGarage(BooleanFilter garage) {
        this.garage = garage;
    }

    public BooleanFilter getPiscine() {
        return piscine;
    }

    public Optional<BooleanFilter> optionalPiscine() {
        return Optional.ofNullable(piscine);
    }

    public BooleanFilter piscine() {
        if (piscine == null) {
            setPiscine(new BooleanFilter());
        }
        return piscine;
    }

    public void setPiscine(BooleanFilter piscine) {
        this.piscine = piscine;
    }

    public BooleanFilter getMeuble() {
        return meuble;
    }

    public Optional<BooleanFilter> optionalMeuble() {
        return Optional.ofNullable(meuble);
    }

    public BooleanFilter meuble() {
        if (meuble == null) {
            setMeuble(new BooleanFilter());
        }
        return meuble;
    }

    public void setMeuble(BooleanFilter meuble) {
        this.meuble = meuble;
    }

    public LocalDateFilter getDisponibleA() {
        return disponibleA;
    }

    public Optional<LocalDateFilter> optionalDisponibleA() {
        return Optional.ofNullable(disponibleA);
    }

    public LocalDateFilter disponibleA() {
        if (disponibleA == null) {
            setDisponibleA(new LocalDateFilter());
        }
        return disponibleA;
    }

    public void setDisponibleA(LocalDateFilter disponibleA) {
        this.disponibleA = disponibleA;
    }

    public StatutBienFilter getStatut() {
        return statut;
    }

    public Optional<StatutBienFilter> optionalStatut() {
        return Optional.ofNullable(statut);
    }

    public StatutBienFilter statut() {
        if (statut == null) {
            setStatut(new StatutBienFilter());
        }
        return statut;
    }

    public void setStatut(StatutBienFilter statut) {
        this.statut = statut;
    }

    public DoubleFilter getLatitude() {
        return latitude;
    }

    public Optional<DoubleFilter> optionalLatitude() {
        return Optional.ofNullable(latitude);
    }

    public DoubleFilter latitude() {
        if (latitude == null) {
            setLatitude(new DoubleFilter());
        }
        return latitude;
    }

    public void setLatitude(DoubleFilter latitude) {
        this.latitude = latitude;
    }

    public DoubleFilter getLongitude() {
        return longitude;
    }

    public Optional<DoubleFilter> optionalLongitude() {
        return Optional.ofNullable(longitude);
    }

    public DoubleFilter longitude() {
        if (longitude == null) {
            setLongitude(new DoubleFilter());
        }
        return longitude;
    }

    public void setLongitude(DoubleFilter longitude) {
        this.longitude = longitude;
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

    public LongFilter getPrixId() {
        return prixId;
    }

    public Optional<LongFilter> optionalPrixId() {
        return Optional.ofNullable(prixId);
    }

    public LongFilter prixId() {
        if (prixId == null) {
            setPrixId(new LongFilter());
        }
        return prixId;
    }

    public void setPrixId(LongFilter prixId) {
        this.prixId = prixId;
    }

    public LongFilter getImagesId() {
        return imagesId;
    }

    public Optional<LongFilter> optionalImagesId() {
        return Optional.ofNullable(imagesId);
    }

    public LongFilter imagesId() {
        if (imagesId == null) {
            setImagesId(new LongFilter());
        }
        return imagesId;
    }

    public void setImagesId(LongFilter imagesId) {
        this.imagesId = imagesId;
    }

    public LongFilter getProprietaireId() {
        return proprietaireId;
    }

    public Optional<LongFilter> optionalProprietaireId() {
        return Optional.ofNullable(proprietaireId);
    }

    public LongFilter proprietaireId() {
        if (proprietaireId == null) {
            setProprietaireId(new LongFilter());
        }
        return proprietaireId;
    }

    public void setProprietaireId(LongFilter proprietaireId) {
        this.proprietaireId = proprietaireId;
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

    public LongFilter getAnnoncesId() {
        return annoncesId;
    }

    public Optional<LongFilter> optionalAnnoncesId() {
        return Optional.ofNullable(annoncesId);
    }

    public LongFilter annoncesId() {
        if (annoncesId == null) {
            setAnnoncesId(new LongFilter());
        }
        return annoncesId;
    }

    public void setAnnoncesId(LongFilter annoncesId) {
        this.annoncesId = annoncesId;
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
        final ImmobilierCriteria that = (ImmobilierCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nom, that.nom) &&
            Objects.equals(description, that.description) &&
            Objects.equals(adresse, that.adresse) &&
            Objects.equals(surface, that.surface) &&
            Objects.equals(nombrePieces, that.nombrePieces) &&
            Objects.equals(nombreChambres, that.nombreChambres) &&
            Objects.equals(nombreSallesBain, that.nombreSallesBain) &&
            Objects.equals(nombreSalons, that.nombreSalons) &&
            Objects.equals(garage, that.garage) &&
            Objects.equals(piscine, that.piscine) &&
            Objects.equals(meuble, that.meuble) &&
            Objects.equals(disponibleA, that.disponibleA) &&
            Objects.equals(statut, that.statut) &&
            Objects.equals(latitude, that.latitude) &&
            Objects.equals(longitude, that.longitude) &&
            Objects.equals(dateCreation, that.dateCreation) &&
            Objects.equals(prixId, that.prixId) &&
            Objects.equals(imagesId, that.imagesId) &&
            Objects.equals(proprietaireId, that.proprietaireId) &&
            Objects.equals(demarcheurId, that.demarcheurId) &&
            Objects.equals(localiteId, that.localiteId) &&
            Objects.equals(quartierId, that.quartierId) &&
            Objects.equals(typeImmobilierId, that.typeImmobilierId) &&
            Objects.equals(annoncesId, that.annoncesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            nom,
            description,
            adresse,
            surface,
            nombrePieces,
            nombreChambres,
            nombreSallesBain,
            nombreSalons,
            garage,
            piscine,
            meuble,
            disponibleA,
            statut,
            latitude,
            longitude,
            dateCreation,
            prixId,
            imagesId,
            proprietaireId,
            demarcheurId,
            localiteId,
            quartierId,
            typeImmobilierId,
            annoncesId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ImmobilierCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNom().map(f -> "nom=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalAdresse().map(f -> "adresse=" + f + ", ").orElse("") +
            optionalSurface().map(f -> "surface=" + f + ", ").orElse("") +
            optionalNombrePieces().map(f -> "nombrePieces=" + f + ", ").orElse("") +
            optionalNombreChambres().map(f -> "nombreChambres=" + f + ", ").orElse("") +
            optionalNombreSallesBain().map(f -> "nombreSallesBain=" + f + ", ").orElse("") +
            optionalNombreSalons().map(f -> "nombreSalons=" + f + ", ").orElse("") +
            optionalGarage().map(f -> "garage=" + f + ", ").orElse("") +
            optionalPiscine().map(f -> "piscine=" + f + ", ").orElse("") +
            optionalMeuble().map(f -> "meuble=" + f + ", ").orElse("") +
            optionalDisponibleA().map(f -> "disponibleA=" + f + ", ").orElse("") +
            optionalStatut().map(f -> "statut=" + f + ", ").orElse("") +
            optionalLatitude().map(f -> "latitude=" + f + ", ").orElse("") +
            optionalLongitude().map(f -> "longitude=" + f + ", ").orElse("") +
            optionalDateCreation().map(f -> "dateCreation=" + f + ", ").orElse("") +
            optionalPrixId().map(f -> "prixId=" + f + ", ").orElse("") +
            optionalImagesId().map(f -> "imagesId=" + f + ", ").orElse("") +
            optionalProprietaireId().map(f -> "proprietaireId=" + f + ", ").orElse("") +
            optionalDemarcheurId().map(f -> "demarcheurId=" + f + ", ").orElse("") +
            optionalLocaliteId().map(f -> "localiteId=" + f + ", ").orElse("") +
            optionalQuartierId().map(f -> "quartierId=" + f + ", ").orElse("") +
            optionalTypeImmobilierId().map(f -> "typeImmobilierId=" + f + ", ").orElse("") +
            optionalAnnoncesId().map(f -> "annoncesId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
