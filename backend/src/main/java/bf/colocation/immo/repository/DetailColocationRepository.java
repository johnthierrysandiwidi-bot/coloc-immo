package bf.colocation.immo.repository;

import bf.colocation.immo.domain.DetailColocation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DetailColocation entity.
 *
 * When extending this class, extend DetailColocationRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface DetailColocationRepository extends DetailColocationRepositoryWithBagRelationships, JpaRepository<DetailColocation, Long> {
    // ---- Contrôle de propriété (anti-IDOR) : identifiant du titulaire légitime ----

    @Query("select d.annonce.auteur.id from DetailColocation d where d.id = :id")
    Optional<Long> trouverAuteurId(@Param("id") Long id);

    default Optional<DetailColocation> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<DetailColocation> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<DetailColocation> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select detailColocation from DetailColocation detailColocation left join fetch detailColocation.annonce",
        countQuery = "select count(detailColocation) from DetailColocation detailColocation"
    )
    Page<DetailColocation> findAllWithToOneRelationships(Pageable pageable);

    @Query("select detailColocation from DetailColocation detailColocation left join fetch detailColocation.annonce")
    List<DetailColocation> findAllWithToOneRelationships();

    @Query(
        "select detailColocation from DetailColocation detailColocation left join fetch detailColocation.annonce where detailColocation.id =:id"
    )
    Optional<DetailColocation> findOneWithToOneRelationships(@Param("id") Long id);
}
