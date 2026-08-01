package bf.colocation.immo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ImmobilierCriteriaTest {

    @Test
    void newImmobilierCriteriaHasAllFiltersNullTest() {
        var immobilierCriteria = new ImmobilierCriteria();
        assertThat(immobilierCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void immobilierCriteriaFluentMethodsCreatesFiltersTest() {
        var immobilierCriteria = new ImmobilierCriteria();

        setAllFilters(immobilierCriteria);

        assertThat(immobilierCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void immobilierCriteriaCopyCreatesNullFilterTest() {
        var immobilierCriteria = new ImmobilierCriteria();
        var copy = immobilierCriteria.copy();

        assertThat(immobilierCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(immobilierCriteria)
        );
    }

    @Test
    void immobilierCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var immobilierCriteria = new ImmobilierCriteria();
        setAllFilters(immobilierCriteria);

        var copy = immobilierCriteria.copy();

        assertThat(immobilierCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(immobilierCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var immobilierCriteria = new ImmobilierCriteria();

        assertThat(immobilierCriteria).hasToString("ImmobilierCriteria{}");
    }

    private static void setAllFilters(ImmobilierCriteria immobilierCriteria) {
        immobilierCriteria.id();
        immobilierCriteria.nom();
        immobilierCriteria.description();
        immobilierCriteria.adresse();
        immobilierCriteria.surface();
        immobilierCriteria.nombrePieces();
        immobilierCriteria.nombreChambres();
        immobilierCriteria.nombreSallesBain();
        immobilierCriteria.nombreSalons();
        immobilierCriteria.garage();
        immobilierCriteria.piscine();
        immobilierCriteria.meuble();
        immobilierCriteria.disponibleA();
        immobilierCriteria.statut();
        immobilierCriteria.latitude();
        immobilierCriteria.longitude();
        immobilierCriteria.dateCreation();
        immobilierCriteria.prixId();
        immobilierCriteria.imagesId();
        immobilierCriteria.proprietaireId();
        immobilierCriteria.demarcheurId();
        immobilierCriteria.localiteId();
        immobilierCriteria.quartierId();
        immobilierCriteria.typeImmobilierId();
        immobilierCriteria.annoncesId();
        immobilierCriteria.distinct();
    }

    private static Condition<ImmobilierCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNom()) &&
                condition.apply(criteria.getDescription()) &&
                condition.apply(criteria.getAdresse()) &&
                condition.apply(criteria.getSurface()) &&
                condition.apply(criteria.getNombrePieces()) &&
                condition.apply(criteria.getNombreChambres()) &&
                condition.apply(criteria.getNombreSallesBain()) &&
                condition.apply(criteria.getNombreSalons()) &&
                condition.apply(criteria.getGarage()) &&
                condition.apply(criteria.getPiscine()) &&
                condition.apply(criteria.getMeuble()) &&
                condition.apply(criteria.getDisponibleA()) &&
                condition.apply(criteria.getStatut()) &&
                condition.apply(criteria.getLatitude()) &&
                condition.apply(criteria.getLongitude()) &&
                condition.apply(criteria.getDateCreation()) &&
                condition.apply(criteria.getPrixId()) &&
                condition.apply(criteria.getImagesId()) &&
                condition.apply(criteria.getProprietaireId()) &&
                condition.apply(criteria.getDemarcheurId()) &&
                condition.apply(criteria.getLocaliteId()) &&
                condition.apply(criteria.getQuartierId()) &&
                condition.apply(criteria.getTypeImmobilierId()) &&
                condition.apply(criteria.getAnnoncesId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ImmobilierCriteria> copyFiltersAre(ImmobilierCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNom(), copy.getNom()) &&
                condition.apply(criteria.getDescription(), copy.getDescription()) &&
                condition.apply(criteria.getAdresse(), copy.getAdresse()) &&
                condition.apply(criteria.getSurface(), copy.getSurface()) &&
                condition.apply(criteria.getNombrePieces(), copy.getNombrePieces()) &&
                condition.apply(criteria.getNombreChambres(), copy.getNombreChambres()) &&
                condition.apply(criteria.getNombreSallesBain(), copy.getNombreSallesBain()) &&
                condition.apply(criteria.getNombreSalons(), copy.getNombreSalons()) &&
                condition.apply(criteria.getGarage(), copy.getGarage()) &&
                condition.apply(criteria.getPiscine(), copy.getPiscine()) &&
                condition.apply(criteria.getMeuble(), copy.getMeuble()) &&
                condition.apply(criteria.getDisponibleA(), copy.getDisponibleA()) &&
                condition.apply(criteria.getStatut(), copy.getStatut()) &&
                condition.apply(criteria.getLatitude(), copy.getLatitude()) &&
                condition.apply(criteria.getLongitude(), copy.getLongitude()) &&
                condition.apply(criteria.getDateCreation(), copy.getDateCreation()) &&
                condition.apply(criteria.getPrixId(), copy.getPrixId()) &&
                condition.apply(criteria.getImagesId(), copy.getImagesId()) &&
                condition.apply(criteria.getProprietaireId(), copy.getProprietaireId()) &&
                condition.apply(criteria.getDemarcheurId(), copy.getDemarcheurId()) &&
                condition.apply(criteria.getLocaliteId(), copy.getLocaliteId()) &&
                condition.apply(criteria.getQuartierId(), copy.getQuartierId()) &&
                condition.apply(criteria.getTypeImmobilierId(), copy.getTypeImmobilierId()) &&
                condition.apply(criteria.getAnnoncesId(), copy.getAnnoncesId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
