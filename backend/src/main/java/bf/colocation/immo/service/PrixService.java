package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.PrixDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.Prix}.
 */
public interface PrixService {
    /**
     * Save a prix.
     *
     * @param prixDTO the entity to save.
     * @return the persisted entity.
     */
    PrixDTO save(PrixDTO prixDTO);

    /**
     * Updates a prix.
     *
     * @param prixDTO the entity to update.
     * @return the persisted entity.
     */
    PrixDTO update(PrixDTO prixDTO);

    /**
     * Partially updates a prix.
     *
     * @param prixDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PrixDTO> partialUpdate(PrixDTO prixDTO);

    /**
     * Get all the prixes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PrixDTO> findAll(Pageable pageable);

    /**
     * Get all the prixes with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PrixDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" prix.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PrixDTO> findOne(Long id);

    /**
     * Delete the "id" prix.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
