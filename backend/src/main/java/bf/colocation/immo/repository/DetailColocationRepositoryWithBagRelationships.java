package bf.colocation.immo.repository;

import bf.colocation.immo.domain.DetailColocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface DetailColocationRepositoryWithBagRelationships {
    Optional<DetailColocation> fetchBagRelationships(Optional<DetailColocation> detailColocation);

    List<DetailColocation> fetchBagRelationships(List<DetailColocation> detailColocations);

    Page<DetailColocation> fetchBagRelationships(Page<DetailColocation> detailColocations);
}
