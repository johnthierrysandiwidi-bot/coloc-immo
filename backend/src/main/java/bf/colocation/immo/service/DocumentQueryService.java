package bf.colocation.immo.service;

import bf.colocation.immo.domain.*; // for static metamodels
import bf.colocation.immo.domain.Document;
import bf.colocation.immo.repository.DocumentRepository;
import bf.colocation.immo.service.criteria.DocumentCriteria;
import bf.colocation.immo.service.dto.DocumentDTO;
import bf.colocation.immo.service.mapper.DocumentMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Document} entities in the database.
 * The main input is a {@link DocumentCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link DocumentDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class DocumentQueryService extends QueryService<Document> {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentQueryService.class);

    private final DocumentRepository documentRepository;

    private final DocumentMapper documentMapper;

    public DocumentQueryService(DocumentRepository documentRepository, DocumentMapper documentMapper) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
    }

    /**
     * Return a {@link Page} of {@link DocumentDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<DocumentDTO> findByCriteria(DocumentCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Document> specification = createSpecification(criteria);
        return documentRepository.findAll(specification, page).map(documentMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(DocumentCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Document> specification = createSpecification(criteria);
        return documentRepository.count(specification);
    }

    /**
     * Function to convert {@link DocumentCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Document> createSpecification(DocumentCriteria criteria) {
        Specification<Document> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Document_.typeDocument, JoinType.LEFT);
                root.fetch(Document_.demarcheur, JoinType.LEFT);
                root.fetch(Document_.traitePar, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Document_.id),
                    buildStringSpecification(criteria.getNom(), Document_.nom),
                    buildStringSpecification(criteria.getUrl(), Document_.url),
                    buildSpecification(criteria.getStatut(), Document_.statut),
                    buildStringSpecification(criteria.getMotifRefus(), Document_.motifRefus),
                    buildRangeSpecification(criteria.getDateAjout(), Document_.dateAjout),
                    buildRangeSpecification(criteria.getDateTraitement(), Document_.dateTraitement),
                    buildSpecification(criteria.getTypeDocumentId(), root ->
                        root.join(Document_.typeDocument, JoinType.LEFT).get(TypeDocument_.id)
                    ),
                    buildSpecification(criteria.getDemarcheurId(), root -> root.join(Document_.demarcheur, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getTraiteParId(), root -> root.join(Document_.traitePar, JoinType.LEFT).get(User_.id))
                )
            );
        }
        return specification;
    }
}
