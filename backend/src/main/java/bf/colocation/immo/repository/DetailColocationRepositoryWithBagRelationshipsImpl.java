package bf.colocation.immo.repository;

import bf.colocation.immo.domain.DetailColocation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class DetailColocationRepositoryWithBagRelationshipsImpl implements DetailColocationRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String DETAILCOLOCATIONS_PARAMETER = "detailColocations";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<DetailColocation> fetchBagRelationships(Optional<DetailColocation> detailColocation) {
        return detailColocation.map(this::fetchEquipementses);
    }

    @Override
    public Page<DetailColocation> fetchBagRelationships(Page<DetailColocation> detailColocations) {
        return new PageImpl<>(
            fetchBagRelationships(detailColocations.getContent()),
            detailColocations.getPageable(),
            detailColocations.getTotalElements()
        );
    }

    @Override
    public List<DetailColocation> fetchBagRelationships(List<DetailColocation> detailColocations) {
        return Optional.of(detailColocations).map(this::fetchEquipementses).orElse(List.of());
    }

    DetailColocation fetchEquipementses(DetailColocation result) {
        return entityManager
            .createQuery(
                "select detailColocation from DetailColocation detailColocation left join fetch detailColocation.equipementses where detailColocation.id = :id",
                DetailColocation.class
            )
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<DetailColocation> fetchEquipementses(List<DetailColocation> detailColocations) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, detailColocations.size()).forEach(index -> order.put(detailColocations.get(index).getId(), index));
        List<DetailColocation> result = entityManager
            .createQuery(
                "select detailColocation from DetailColocation detailColocation left join fetch detailColocation.equipementses where detailColocation in :detailColocations",
                DetailColocation.class
            )
            .setParameter(DETAILCOLOCATIONS_PARAMETER, detailColocations)
            .getResultList();
        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
