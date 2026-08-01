package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.AlerteNotifieeDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.AlerteNotifiee}.
 */
public interface AlerteNotifieeService {
    /**
     * Save a alerteNotifiee.
     *
     * @param alerteNotifieeDTO the entity to save.
     * @return the persisted entity.
     */
    AlerteNotifieeDTO save(AlerteNotifieeDTO alerteNotifieeDTO);

    /**
     * Updates a alerteNotifiee.
     *
     * @param alerteNotifieeDTO the entity to update.
     * @return the persisted entity.
     */
    AlerteNotifieeDTO update(AlerteNotifieeDTO alerteNotifieeDTO);

    /**
     * Partially updates a alerteNotifiee.
     *
     * @param alerteNotifieeDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AlerteNotifieeDTO> partialUpdate(AlerteNotifieeDTO alerteNotifieeDTO);

    /**
     * Get all the alerteNotifiees.
     *
     * @return the list of entities.
     */
    List<AlerteNotifieeDTO> findAll();

    /**
     * Get all the alerteNotifiees with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AlerteNotifieeDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" alerteNotifiee.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AlerteNotifieeDTO> findOne(Long id);

    /**
     * Delete the "id" alerteNotifiee.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
