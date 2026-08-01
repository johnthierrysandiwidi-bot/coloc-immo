package bf.colocation.immo.service;

import bf.colocation.immo.domain.*; // for static metamodels
import bf.colocation.immo.domain.Immobilier;
import bf.colocation.immo.repository.ImmobilierRepository;
import bf.colocation.immo.service.criteria.ImmobilierCriteria;
import bf.colocation.immo.service.dto.ImmobilierDTO;
import bf.colocation.immo.service.mapper.ImmobilierMapper;
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
 * Service for executing complex queries for {@link Immobilier} entities in the database.
 * The main input is a {@link ImmobilierCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ImmobilierDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ImmobilierQueryService extends QueryService<Immobilier> {

    private static final Logger LOG = LoggerFactory.getLogger(ImmobilierQueryService.class);

    private final ImmobilierRepository immobilierRepository;

    private final ImmobilierMapper immobilierMapper;

    public ImmobilierQueryService(ImmobilierRepository immobilierRepository, ImmobilierMapper immobilierMapper) {
        this.immobilierRepository = immobilierRepository;
        this.immobilierMapper = immobilierMapper;
    }

    /**
     * Return a {@link Page} of {@link ImmobilierDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ImmobilierDTO> findByCriteria(ImmobilierCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Immobilier> specification = createSpecification(criteria);
        return immobilierRepository.findAll(specification, page).map(immobilierMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ImmobilierCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Immobilier> specification = createSpecification(criteria);
        return immobilierRepository.count(specification);
    }

    /**
     * Function to convert {@link ImmobilierCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Immobilier> createSpecification(ImmobilierCriteria criteria) {
        Specification<Immobilier> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Immobilier_.proprietaire, JoinType.LEFT);
                root.fetch(Immobilier_.demarcheur, JoinType.LEFT);
                root.fetch(Immobilier_.localite, JoinType.LEFT);
                root.fetch(Immobilier_.quartier, JoinType.LEFT);
                root.fetch(Immobilier_.typeImmobilier, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Immobilier_.id),
                    buildStringSpecification(criteria.getNom(), Immobilier_.nom),
                    buildStringSpecification(criteria.getDescription(), Immobilier_.description),
                    buildStringSpecification(criteria.getAdresse(), Immobilier_.adresse),
                    buildRangeSpecification(criteria.getSurface(), Immobilier_.surface),
                    buildRangeSpecification(criteria.getNombrePieces(), Immobilier_.nombrePieces),
                    buildRangeSpecification(criteria.getNombreChambres(), Immobilier_.nombreChambres),
                    buildRangeSpecification(criteria.getNombreSallesBain(), Immobilier_.nombreSallesBain),
                    buildRangeSpecification(criteria.getNombreSalons(), Immobilier_.nombreSalons),
                    buildSpecification(criteria.getGarage(), Immobilier_.garage),
                    buildSpecification(criteria.getPiscine(), Immobilier_.piscine),
                    buildSpecification(criteria.getMeuble(), Immobilier_.meuble),
                    buildRangeSpecification(criteria.getDisponibleA(), Immobilier_.disponibleA),
                    buildSpecification(criteria.getStatut(), Immobilier_.statut),
                    buildRangeSpecification(criteria.getLatitude(), Immobilier_.latitude),
                    buildRangeSpecification(criteria.getLongitude(), Immobilier_.longitude),
                    buildRangeSpecification(criteria.getDateCreation(), Immobilier_.dateCreation),
                    buildSpecification(criteria.getPrixId(), root -> root.join(Immobilier_.prixes, JoinType.LEFT).get(Prix_.id)),
                    buildSpecification(criteria.getImagesId(), root -> root.join(Immobilier_.imageses, JoinType.LEFT).get(Image_.id)),
                    buildSpecification(criteria.getProprietaireId(), root ->
                        root.join(Immobilier_.proprietaire, JoinType.LEFT).get(User_.id)
                    ),
                    buildSpecification(criteria.getDemarcheurId(), root -> root.join(Immobilier_.demarcheur, JoinType.LEFT).get(User_.id)),
                    buildSpecification(criteria.getLocaliteId(), root -> root.join(Immobilier_.localite, JoinType.LEFT).get(Localite_.id)),
                    buildSpecification(criteria.getQuartierId(), root -> root.join(Immobilier_.quartier, JoinType.LEFT).get(Quartier_.id)),
                    buildSpecification(criteria.getTypeImmobilierId(), root ->
                        root.join(Immobilier_.typeImmobilier, JoinType.LEFT).get(TypeImmobilier_.id)
                    ),
                    buildSpecification(criteria.getAnnoncesId(), root -> root.join(Immobilier_.annonceses, JoinType.LEFT).get(Annonce_.id))
                )
            );
        }
        return specification;
    }
}
