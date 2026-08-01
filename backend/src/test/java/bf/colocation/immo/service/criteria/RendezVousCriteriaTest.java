package bf.colocation.immo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class RendezVousCriteriaTest {

    @Test
    void newRendezVousCriteriaHasAllFiltersNullTest() {
        var rendezVousCriteria = new RendezVousCriteria();
        assertThat(rendezVousCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void rendezVousCriteriaFluentMethodsCreatesFiltersTest() {
        var rendezVousCriteria = new RendezVousCriteria();

        setAllFilters(rendezVousCriteria);

        assertThat(rendezVousCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void rendezVousCriteriaCopyCreatesNullFilterTest() {
        var rendezVousCriteria = new RendezVousCriteria();
        var copy = rendezVousCriteria.copy();

        assertThat(rendezVousCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(rendezVousCriteria)
        );
    }

    @Test
    void rendezVousCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var rendezVousCriteria = new RendezVousCriteria();
        setAllFilters(rendezVousCriteria);

        var copy = rendezVousCriteria.copy();

        assertThat(rendezVousCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(rendezVousCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var rendezVousCriteria = new RendezVousCriteria();

        assertThat(rendezVousCriteria).hasToString("RendezVousCriteria{}");
    }

    private static void setAllFilters(RendezVousCriteria rendezVousCriteria) {
        rendezVousCriteria.id();
        rendezVousCriteria.dateHeure();
        rendezVousCriteria.dateReportee();
        rendezVousCriteria.lieu();
        rendezVousCriteria.contenu();
        rendezVousCriteria.motif();
        rendezVousCriteria.statut();
        rendezVousCriteria.annonceId();
        rendezVousCriteria.demandeurId();
        rendezVousCriteria.distinct();
    }

    private static Condition<RendezVousCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDateHeure()) &&
                condition.apply(criteria.getDateReportee()) &&
                condition.apply(criteria.getLieu()) &&
                condition.apply(criteria.getContenu()) &&
                condition.apply(criteria.getMotif()) &&
                condition.apply(criteria.getStatut()) &&
                condition.apply(criteria.getAnnonceId()) &&
                condition.apply(criteria.getDemandeurId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<RendezVousCriteria> copyFiltersAre(RendezVousCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDateHeure(), copy.getDateHeure()) &&
                condition.apply(criteria.getDateReportee(), copy.getDateReportee()) &&
                condition.apply(criteria.getLieu(), copy.getLieu()) &&
                condition.apply(criteria.getContenu(), copy.getContenu()) &&
                condition.apply(criteria.getMotif(), copy.getMotif()) &&
                condition.apply(criteria.getStatut(), copy.getStatut()) &&
                condition.apply(criteria.getAnnonceId(), copy.getAnnonceId()) &&
                condition.apply(criteria.getDemandeurId(), copy.getDemandeurId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
