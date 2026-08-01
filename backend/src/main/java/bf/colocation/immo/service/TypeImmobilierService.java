package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.TypeImmobilierDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.TypeImmobilier}.
 */
public interface TypeImmobilierService {
    /**
     * Save a typeImmobilier.
     *
     * @param typeImmobilierDTO the entity to save.
     * @return the persisted entity.
     */
    TypeImmobilierDTO save(TypeImmobilierDTO typeImmobilierDTO);

    /**
     * Updates a typeImmobilier.
     *
     * @param typeImmobilierDTO the entity to update.
     * @return the persisted entity.
     */
    TypeImmobilierDTO update(TypeImmobilierDTO typeImmobilierDTO);

    /**
     * Partially updates a typeImmobilier.
     *
     * @param typeImmobilierDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TypeImmobilierDTO> partialUpdate(TypeImmobilierDTO typeImmobilierDTO);

    /**
     * Get all the typeImmobiliers.
     *
     * @return the list of entities.
     */
    List<TypeImmobilierDTO> findAll();

    /**
     * Get the "id" typeImmobilier.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TypeImmobilierDTO> findOne(Long id);

    /**
     * Delete the "id" typeImmobilier.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
