package bf.colocation.immo.service;

import bf.colocation.immo.domain.*; // for static metamodels
import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.service.criteria.RendezVousCriteria;
import bf.colocation.immo.service.dto.RendezVousDTO;
import bf.colocation.immo.service.mapper.RendezVousMapper;
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
 * Service for executing complex queries for {@link RendezVous} entities in the database.
 * The main input is a {@link RendezVousCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link RendezVousDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RendezVousQueryService extends QueryService<RendezVous> {

    private static final Logger LOG = LoggerFactory.getLogger(RendezVousQueryService.class);

    private final RendezVousRepository rendezVousRepository;

    private final RendezVousMapper rendezVousMapper;

    public RendezVousQueryService(RendezVousRepository rendezVousRepository, RendezVousMapper rendezVousMapper) {
        this.rendezVousRepository = rendezVousRepository;
        this.rendezVousMapper = rendezVousMapper;
    }

    /**
     * Return a {@link Page} of {@link RendezVousDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<RendezVousDTO> findByCriteria(RendezVousCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<RendezVous> specification = createSpecification(criteria);
        return rendezVousRepository.findAll(specification, page).map(rendezVousMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RendezVousCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<RendezVous> specification = createSpecification(criteria);
        return rendezVousRepository.count(specification);
    }

    /**
     * Function to convert {@link RendezVousCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<RendezVous> createSpecification(RendezVousCriteria criteria) {
        Specification<RendezVous> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(RendezVous_.annonce, JoinType.LEFT);
                root.fetch(RendezVous_.demandeur, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), RendezVous_.id),
                    buildRangeSpecification(criteria.getDateHeure(), RendezVous_.dateHeure),
                    buildRangeSpecification(criteria.getDateReportee(), RendezVous_.dateReportee),
                    buildStringSpecification(criteria.getLieu(), RendezVous_.lieu),
                    buildStringSpecification(criteria.getContenu(), RendezVous_.contenu),
                    buildStringSpecification(criteria.getMotif(), RendezVous_.motif),
                    buildSpecification(criteria.getStatut(), RendezVous_.statut),
                    buildSpecification(criteria.getAnnonceId(), root -> root.join(RendezVous_.annonce, JoinType.LEFT).get(Annonce_.id)),
                    buildSpecification(criteria.getDemandeurId(), root -> root.join(RendezVous_.demandeur, JoinType.LEFT).get(User_.id))
                )
            );
        }
        return specification;
    }
}
