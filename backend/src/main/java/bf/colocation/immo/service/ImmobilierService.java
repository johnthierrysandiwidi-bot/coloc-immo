package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.ImmobilierDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.Immobilier}.
 */
public interface ImmobilierService {
    /**
     * Save a immobilier.
     *
     * @param immobilierDTO the entity to save.
     * @return the persisted entity.
     */
    ImmobilierDTO save(ImmobilierDTO immobilierDTO);

    /**
     * Updates a immobilier.
     *
     * @param immobilierDTO the entity to update.
     * @return the persisted entity.
     */
    ImmobilierDTO update(ImmobilierDTO immobilierDTO);

    /**
     * Partially updates a immobilier.
     *
     * @param immobilierDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ImmobilierDTO> partialUpdate(ImmobilierDTO immobilierDTO);

    /**
     * Get all the immobiliers with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<ImmobilierDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" immobilier.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ImmobilierDTO> findOne(Long id);

    /**
     * Delete the "id" immobilier.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
