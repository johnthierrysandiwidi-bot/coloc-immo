package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.DeviceTokenDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.DeviceToken}.
 */
public interface DeviceTokenService {
    /**
     * Save a deviceToken.
     *
     * @param deviceTokenDTO the entity to save.
     * @return the persisted entity.
     */
    DeviceTokenDTO save(DeviceTokenDTO deviceTokenDTO);

    /**
     * Updates a deviceToken.
     *
     * @param deviceTokenDTO the entity to update.
     * @return the persisted entity.
     */
    DeviceTokenDTO update(DeviceTokenDTO deviceTokenDTO);

    /**
     * Partially updates a deviceToken.
     *
     * @param deviceTokenDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DeviceTokenDTO> partialUpdate(DeviceTokenDTO deviceTokenDTO);

    /**
     * Get all the deviceTokens.
     *
     * @return the list of entities.
     */
    List<DeviceTokenDTO> findAll();

    /**
     * Get all the deviceTokens with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DeviceTokenDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" deviceToken.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DeviceTokenDTO> findOne(Long id);

    /**
     * Delete the "id" deviceToken.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
