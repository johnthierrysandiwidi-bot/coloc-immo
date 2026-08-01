package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.DetailColocationDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.DetailColocation}.
 */
public interface DetailColocationService {
    /**
     * Save a detailColocation.
     *
     * @param detailColocationDTO the entity to save.
     * @return the persisted entity.
     */
    DetailColocationDTO save(DetailColocationDTO detailColocationDTO);

    /**
     * Updates a detailColocation.
     *
     * @param detailColocationDTO the entity to update.
     * @return the persisted entity.
     */
    DetailColocationDTO update(DetailColocationDTO detailColocationDTO);

    /**
     * Partially updates a detailColocation.
     *
     * @param detailColocationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<DetailColocationDTO> partialUpdate(DetailColocationDTO detailColocationDTO);

    /**
     * Get all the detailColocations.
     *
     * @return the list of entities.
     */
    List<DetailColocationDTO> findAll();

    /**
     * Get all the detailColocations with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<DetailColocationDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" detailColocation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<DetailColocationDTO> findOne(Long id);

    /**
     * Delete the "id" detailColocation.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
