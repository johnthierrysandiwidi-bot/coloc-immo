package bf.colocation.immo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class AnnonceCriteriaTest {

    @Test
    void newAnnonceCriteriaHasAllFiltersNullTest() {
        var annonceCriteria = new AnnonceCriteria();
        assertThat(annonceCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void annonceCriteriaFluentMethodsCreatesFiltersTest() {
        var annonceCriteria = new AnnonceCriteria();

        setAllFilters(annonceCriteria);

        assertThat(annonceCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void annonceCriteriaCopyCreatesNullFilterTest() {
        var annonceCriteria = new AnnonceCriteria();
        var copy = annonceCriteria.copy();

        assertThat(annonceCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(annonceCriteria)
        );
    }

    @Test
    void annonceCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var annonceCriteria = new AnnonceCriteria();
        setAllFilters(annonceCriteria);

        var copy = annonceCriteria.copy();

        assertThat(annonceCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(annonceCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var annonceCriteria = new AnnonceCriteria();

        assertThat(annonceCriteria).hasToString("AnnonceCriteria{}");
    }

    private static void setAllFilters(AnnonceCriteria annonceCriteria) {
        annonceCriteria.id();
        annonceCriteria.titre();
        annonceCriteria.contenu();
        annonceCriteria.type();
        annonceCriteria.prix();
        annonceCriteria.nombreVues();
        annonceCriteria.datePublication();
        annonceCriteria.dateExpiration();
        annonceCriteria.statut();
        annonceCriteria.immobilierId();
        annonceCriteria.auteurId();
        annonceCriteria.detailColocationId();
        annonceCriteria.vuesId();
        annonceCriteria.rendezVousId();
        annonceCriteria.favorisId();
        annonceCriteria.distinct();
    }

    private static Condition<AnnonceCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitre()) &&
                condition.apply(criteria.getContenu()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getPrix()) &&
                condition.apply(criteria.getNombreVues()) &&
                condition.apply(criteria.getDatePublication()) &&
                condition.apply(criteria.getDateExpiration()) &&
                condition.apply(criteria.getStatut()) &&
                condition.apply(criteria.getImmobilierId()) &&
                condition.apply(criteria.getAuteurId()) &&
                condition.apply(criteria.getDetailColocationId()) &&
                condition.apply(criteria.getVuesId()) &&
                condition.apply(criteria.getRendezVousId()) &&
                condition.apply(criteria.getFavorisId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<AnnonceCriteria> copyFiltersAre(AnnonceCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitre(), copy.getTitre()) &&
                condition.apply(criteria.getContenu(), copy.getContenu()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getPrix(), copy.getPrix()) &&
                condition.apply(criteria.getNombreVues(), copy.getNombreVues()) &&
                condition.apply(criteria.getDatePublication(), copy.getDatePublication()) &&
                condition.apply(criteria.getDateExpiration(), copy.getDateExpiration()) &&
                condition.apply(criteria.getStatut(), copy.getStatut()) &&
                condition.apply(criteria.getImmobilierId(), copy.getImmobilierId()) &&
                condition.apply(criteria.getAuteurId(), copy.getAuteurId()) &&
                condition.apply(criteria.getDetailColocationId(), copy.getDetailColocationId()) &&
                condition.apply(criteria.getVuesId(), copy.getVuesId()) &&
                condition.apply(criteria.getRendezVousId(), copy.getRendezVousId()) &&
                condition.apply(criteria.getFavorisId(), copy.getFavorisId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
