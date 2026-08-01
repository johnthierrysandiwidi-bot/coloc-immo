package bf.colocation.immo.service;

import bf.colocation.immo.domain.*; // for static metamodels
import bf.colocation.immo.domain.Alerte;
import bf.colocation.immo.repository.AlerteRepository;
import bf.colocation.immo.service.criteria.AlerteCriteria;
import bf.colocation.immo.service.dto.AlerteDTO;
import bf.colocation.immo.service.mapper.AlerteMapper;
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
 * Service for executing complex queries for {@link Alerte} entities in the database.
 * The main input is a {@link AlerteCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link AlerteDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class AlerteQueryService extends QueryService<Alerte> {

    private static final Logger LOG = LoggerFactory.getLogger(AlerteQueryService.class);

    private final AlerteRepository alerteRepository;

    private final AlerteMapper alerteMapper;

    public AlerteQueryService(AlerteRepository alerteRepository, AlerteMapper alerteMapper) {
        this.alerteRepository = alerteRepository;
        this.alerteMapper = alerteMapper;
    }

    /**
     * Return a {@link Page} of {@link AlerteDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AlerteDTO> findByCriteria(AlerteCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Alerte> specification = createSpecification(criteria);
        return alerteRepository.findAll(specification, page).map(alerteMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AlerteCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Alerte> specification = createSpecification(criteria);
        return alerteRepository.count(specification);
    }

    /**
     * Function to convert {@link AlerteCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Alerte> createSpecification(AlerteCriteria criteria) {
        Specification<Alerte> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Alerte_.titulaire, JoinType.LEFT);
                root.fetch(Alerte_.localite, JoinType.LEFT);
                root.fetch(Alerte_.quartier, JoinType.LEFT);
                root.fetch(Alerte_.typeImmobilier, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Alerte_.id),
                    buildStringSpecification(criteria.getTitre(), Alerte_.titre),
                    buildStringSpecification(criteria.getContenu(), Alerte_.contenu),
                    buildSpecification(criteria.getTypeAnnonce(), Alerte_.typeAnnonce),
                    buildRangeSpecification(criteria.getPrixMin(), Alerte_.prixMin),
                    buildRangeSpecification(criteria.getPrixMax(), Alerte_.prixMax),
                    buildRangeSpecification(criteria.getSurfaceMin(), Alerte_.surfaceMin),
                    buildRangeSpecification(criteria.getNombreChambresMin(), Alerte_.nombreChambresMin),
                    buildSpecification(criteria.getMeubleUniquement(), Alerte_.meubleUniquement),
                    buildSpecification(criteria.getActive(), Alerte_.active),
                    buildSpecification(criteria.getFrequence(), Alerte_.frequence),
                    buildRangeSpecification(criteria.getDerniereExecution(), Alerte_.derniereExecution),
                    buildSpecification(criteria.getTitulaireId(), root -> root.join(Alerte_.titulaire, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getLocaliteId(), root -> root.join(Alerte_.localite, JoinType.LEFT).get(Localite_.id)),
                    buildSpecification(criteria.getQuartierId(), root -> root.join(Alerte_.quartier, JoinType.LEFT).get(Quartier_.id)),
                    buildSpecification(criteria.getTypeImmobilierId(), root ->
                        root.join(Alerte_.typeImmobilier, JoinType.LEFT).get(TypeImmobilier_.id)
                    ),
                    buildSpecification(criteria.getNotifieesId(), root ->
                        root.join(Alerte_.notifieeses, JoinType.LEFT).get(AlerteNotifiee_.id)
                    )
                )
            );
        }
        return specification;
    }
}
