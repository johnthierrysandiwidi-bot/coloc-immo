package bf.colocation.immo.service;

import bf.colocation.immo.domain.*; // for static metamodels
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.service.criteria.AnnonceCriteria;
import bf.colocation.immo.service.dto.AnnonceDTO;
import bf.colocation.immo.service.mapper.AnnonceMapper;
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
 * Service for executing complex queries for {@link Annonce} entities in the database.
 * The main input is a {@link AnnonceCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AnnonceDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AnnonceQueryService extends QueryService<Annonce> {

    private static final Logger LOG = LoggerFactory.getLogger(AnnonceQueryService.class);

    private final AnnonceRepository annonceRepository;

    private final AnnonceMapper annonceMapper;

    public AnnonceQueryService(AnnonceRepository annonceRepository, AnnonceMapper annonceMapper) {
        this.annonceRepository = annonceRepository;
        this.annonceMapper = annonceMapper;
    }

    /**
     * Return a {@link Page} of {@link AnnonceDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AnnonceDTO> findByCriteria(AnnonceCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Annonce> specification = createSpecification(criteria);
        return annonceRepository.findAll(specification, page).map(annonceMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AnnonceCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Annonce> specification = createSpecification(criteria);
        return annonceRepository.count(specification);
    }

    /**
     * Function to convert {@link AnnonceCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Annonce> createSpecification(AnnonceCriteria criteria) {
        Specification<Annonce> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Annonce_.immobilier, JoinType.LEFT);
                root.fetch(Annonce_.auteur, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Annonce_.id),
                    buildStringSpecification(criteria.getTitre(), Annonce_.titre),
                    buildStringSpecification(criteria.getContenu(), Annonce_.contenu),
                    buildSpecification(criteria.getType(), Annonce_.type),
                    buildRangeSpecification(criteria.getPrix(), Annonce_.prix),
                    buildRangeSpecification(criteria.getNombreVues(), Annonce_.nombreVues),
                    buildRangeSpecification(criteria.getDatePublication(), Annonce_.datePublication),
                    buildRangeSpecification(criteria.getDateExpiration(), Annonce_.dateExpiration),
                    buildSpecification(criteria.getStatut(), Annonce_.statut),
                    buildSpecification(criteria.getImmobilierId(), root ->
                        root.join(Annonce_.immobilier, JoinType.LEFT).get(Immobilier_.id)
                    ),
                    buildSpecification(criteria.getAuteurId(), root -> root.join(Annonce_.auteur, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getDetailColocationId(), root ->
                        root.join(Annonce_.detailColocation, JoinType.LEFT).get(DetailColocation_.id)
                    ),
                    buildSpecification(criteria.getVuesId(), root -> root.join(Annonce_.vueses, JoinType.LEFT).get(VueAnnonce_.id)),
                    buildSpecification(criteria.getRendezVousId(), root ->
                        root.join(Annonce_.rendezVouses, JoinType.LEFT).get(RendezVous_.id)
                    ),
                    buildSpecification(criteria.getFavorisId(), root -> root.join(Annonce_.favorises, JoinType.LEFT).get(Favori_.id))
                )
            );
        }
        return specification;
    }
}
