package bf.colocation.immo.repository;

import bf.colocation.immo.domain.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Document entity.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    @Query("select document from Document document where document.demarcheur.login = ?#{authentication.name}")
    List<Document> findByDemarcheurIsCurrentUser();

    /** Retrouve la pièce justificative associée à une URL de fichier, pour contrôler qui peut la télécharger. */
    Optional<Document> findFirstByUrl(String url);

    @Query("select document from Document document where document.traitePar.login = ?#{authentication.name}")
    List<Document> findByTraiteParIsCurrentUser();

    default Optional<Document> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Document> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Document> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select document from Document document left join fetch document.typeDocument left join fetch document.demarcheur left join fetch document.traitePar",
        countQuery = "select count(document) from Document document"
    )
    Page<Document> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select document from Document document left join fetch document.typeDocument left join fetch document.demarcheur left join fetch document.traitePar"
    )
    List<Document> findAllWithToOneRelationships();

    @Query(
        "select document from Document document left join fetch document.typeDocument left join fetch document.demarcheur left join fetch document.traitePar where document.id =:id"
    )
    Optional<Document> findOneWithToOneRelationships(@Param("id") Long id);
}
