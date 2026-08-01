package bf.colocation.immo.service;

import bf.colocation.immo.service.dto.FavoriDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link bf.colocation.immo.domain.Favori}.
 */
public interface FavoriService {
    /**
     * Save a favori.
     *
     * @param favoriDTO the entity to save.
     * @return the persisted entity.
     */
    FavoriDTO save(FavoriDTO favoriDTO);

    /**
     * Updates a favori.
     *
     * @param favoriDTO the entity to update.
     * @return the persisted entity.
     */
    FavoriDTO update(FavoriDTO favoriDTO);

    /**
     * Partially updates a favori.
     *
     * @param favoriDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FavoriDTO> partialUpdate(FavoriDTO favoriDTO);

    /**
     * Get all the favoris with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FavoriDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" favori.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FavoriDTO> findOne(Long id);

    /**
     * Delete the "id" favori.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
