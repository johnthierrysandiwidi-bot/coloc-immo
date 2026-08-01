package bf.colocation.immo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AlerteCriteriaTest {

    @Test
    void newAlerteCriteriaHasAllFiltersNullTest() {
        var alerteCriteria = new AlerteCriteria();
        assertThat(alerteCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void alerteCriteriaFluentMethodsCreatesFiltersTest() {
        var alerteCriteria = new AlerteCriteria();

        setAllFilters(alerteCriteria);

        assertThat(alerteCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void alerteCriteriaCopyCreatesNullFilterTest() {
        var alerteCriteria = new AlerteCriteria();
        var copy = alerteCriteria.copy();

        assertThat(alerteCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(alerteCriteria)
        );
    }

    @Test
    void alerteCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var alerteCriteria = new AlerteCriteria();
        setAllFilters(alerteCriteria);

        var copy = alerteCriteria.copy();

        assertThat(alerteCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(alerteCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var alerteCriteria = new AlerteCriteria();

        assertThat(alerteCriteria).hasToString("AlerteCriteria{}");
    }

    private static void setAllFilters(AlerteCriteria alerteCriteria) {
        alerteCriteria.id();
        alerteCriteria.titre();
        alerteCriteria.contenu();
        alerteCriteria.typeAnnonce();
        alerteCriteria.prixMin();
        alerteCriteria.prixMax();
        alerteCriteria.surfaceMin();
        alerteCriteria.nombreChambresMin();
        alerteCriteria.meubleUniquement();
        alerteCriteria.active();
        alerteCriteria.frequence();
        alerteCriteria.derniereExecution();
        alerteCriteria.titulaireId();
        alerteCriteria.localiteId();
        alerteCriteria.quartierId();
        alerteCriteria.typeImmobilierId();
        alerteCriteria.notifieesId();
        alerteCriteria.distinct();
    }

    private static Condition<AlerteCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitre()) &&
                condition.apply(criteria.getContenu()) &&
                condition.apply(criteria.getTypeAnnonce()) &&
                condition.apply(criteria.getPrixMin()) &&
                condition.apply(criteria.getPrixMax()) &&
                condition.apply(criteria.getSurfaceMin()) &&
                condition.apply(criteria.getNombreChambresMin()) &&
                condition.apply(criteria.getMeubleUniquement()) &&
                condition.apply(criteria.getActive()) &&
                condition.apply(criteria.getFrequence()) &&
                condition.apply(criteria.getDerniereExecution()) &&
                condition.apply(criteria.getTitulaireId()) &&
                condition.apply(criteria.getLocaliteId()) &&
                condition.apply(criteria.getQuartierId()) &&
                condition.apply(criteria.getTypeImmobilierId()) &&
                condition.apply(criteria.getNotifieesId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AlerteCriteria> copyFiltersAre(AlerteCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitre(), copy.getTitre()) &&
                condition.apply(criteria.getContenu(), copy.getContenu()) &&
                condition.apply(criteria.getTypeAnnonce(), copy.getTypeAnnonce()) &&
                condition.apply(criteria.getPrixMin(), copy.getPrixMin()) &&
                condition.apply(criteria.getPrixMax(), copy.getPrixMax()) &&
                condition.apply(criteria.getSurfaceMin(), copy.getSurfaceMin()) &&
                condition.apply(criteria.getNombreChambresMin(), copy.getNombreChambresMin()) &&
                condition.apply(criteria.getMeubleUniquement(), copy.getMeubleUniquement()) &&
                condition.apply(criteria.getActive(), copy.getActive()) &&
                condition.apply(criteria.getFrequence(), copy.getFrequence()) &&
                condition.apply(criteria.getDerniereExecution(), copy.getDerniereExecution()) &&
                condition.apply(criteria.getTitulaireId(), copy.getTitulaireId()) &&
                condition.apply(criteria.getLocaliteId(), copy.getLocaliteId()) &&
                condition.apply(criteria.getQuartierId(), copy.getQuartierId()) &&
                condition.apply(criteria.getTypeImmobilierId(), copy.getTypeImmobilierId()) &&
                condition.apply(criteria.getNotifieesId(), copy.getNotifieesId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
