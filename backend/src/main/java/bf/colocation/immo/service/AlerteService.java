package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.AlerteDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.Alerte}.
 */
public interface AlerteService {
    /**
     * Save a alerte.
     *
     * @param alerteDTO the entity to save.
     * @return the persisted entity.
     */
    AlerteDTO save(AlerteDTO alerteDTO);

    /**
     * Updates a alerte.
     *
     * @param alerteDTO the entity to update.
     * @return the persisted entity.
     */
    AlerteDTO update(AlerteDTO alerteDTO);

    /**
     * Partially updates a alerte.
     *
     * @param alerteDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AlerteDTO> partialUpdate(AlerteDTO alerteDTO);

    /**
     * Get all the alertes with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AlerteDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" alerte.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AlerteDTO> findOne(Long id);

    /**
     * Delete the "id" alerte.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
