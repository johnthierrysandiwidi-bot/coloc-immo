package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.LocaliteDTO;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.Localite}.
 */
public interface LocaliteService {
    /**
     * Save a localite.
     *
     * @param localiteDTO the entity to save.
     * @return the persisted entity.
     */
    LocaliteDTO save(LocaliteDTO localiteDTO);

    /**
     * Updates a localite.
     *
     * @param localiteDTO the entity to update.
     * @return the persisted entity.
     */
    LocaliteDTO update(LocaliteDTO localiteDTO);

    /**
     * Partially updates a localite.
     *
     * @param localiteDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<LocaliteDTO> partialUpdate(LocaliteDTO localiteDTO);

    /**
     * Get all the localites.
     *
     * @return the list of entities.
     */
    List<LocaliteDTO> findAll();

    /**
     * Get the "id" localite.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<LocaliteDTO> findOne(Long id);

    /**
     * Delete the "id" localite.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
