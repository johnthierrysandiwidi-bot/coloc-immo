package bf.colocation.immo.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class FavoriCriteriaTest {

    @Test
    void newFavoriCriteriaHasAllFiltersNullTest() {
        var favoriCriteria = new FavoriCriteria();
        assertThat(favoriCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void favoriCriteriaFluentMethodsCreatesFiltersTest() {
        var favoriCriteria = new FavoriCriteria();

        setAllFilters(favoriCriteria);

        assertThat(favoriCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void favoriCriteriaCopyCreatesNullFilterTest() {
        var favoriCriteria = new FavoriCriteria();
        var copy = favoriCriteria.copy();

        assertThat(favoriCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(favoriCriteria)
        );
    }

    @Test
    void favoriCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var favoriCriteria = new FavoriCriteria();
        setAllFilters(favoriCriteria);

        var copy = favoriCriteria.copy();

        assertThat(favoriCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(favoriCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var favoriCriteria = new FavoriCriteria();

        assertThat(favoriCriteria).hasToString("FavoriCriteria{}");
    }

    private static void setAllFilters(FavoriCriteria favoriCriteria) {
        favoriCriteria.id();
        favoriCriteria.dateAjout();
        favoriCriteria.annonceId();
        favoriCriteria.utilisateurId();
        favoriCriteria.distinct();
    }

    private static Condition<FavoriCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDateAjout()) &&
                condition.apply(criteria.getAnnonceId()) &&
                condition.apply(criteria.getUtilisateurId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<FavoriCriteria> copyFiltersAre(FavoriCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDateAjout(), copy.getDateAjout()) &&
                condition.apply(criteria.getAnnonceId(), copy.getAnnonceId()) &&
                condition.apply(criteria.getUtilisateurId(), copy.getUtilisateurId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
